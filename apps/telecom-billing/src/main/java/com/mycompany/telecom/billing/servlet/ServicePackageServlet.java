package com.mycompany.telecom.billing.servlet;

import com.mycompany.telecom.billing.dao.ServicePackageDAO;
import com.mycompany.telecom.billing.model.ServicePackage;
import com.mycompany.telecom.billing.util.HtmlLayout;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author Ali
 */
@WebServlet("/packages/*")
public class ServicePackageServlet extends HttpServlet {
 
    private final ServicePackageDAO dao = new ServicePackageDAO();
 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/"))  renderList(req, resp);
            else if (path.equals("/new"))          renderForm(req, resp, new ServicePackage(), false);
            else if (path.startsWith("/edit/")) {
                ServicePackage sp = dao.findById(Integer.parseInt(path.substring(6)));
                if (sp == null) { resp.sendError(404); return; }
                renderForm(req, resp, sp, true);
            } else if (path.startsWith("/delete/")) {
                dao.delete(Integer.parseInt(path.substring(8)));
                resp.sendRedirect(req.getContextPath() + "/packages/?success=deleted");
            } else resp.sendError(404);
        } catch (Exception e) { throw new ServletException(e); }
    }
 
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        try {
            ServicePackage sp = new ServicePackage();
            sp.setName(req.getParameter("name"));
            sp.setType(req.getParameter("type"));
            String amt  = req.getParameter("amount");
            String prio = req.getParameter("priority");
            sp.setAmount((amt  != null && !amt.isBlank())  ? new BigDecimal(amt) : BigDecimal.ZERO);
            sp.setPriority((prio != null && !prio.isBlank()) ? Integer.parseInt(prio) : 1);
            if ("/new".equals(path)) dao.insert(sp);
            else if (path != null && path.startsWith("/edit/")) {
                sp.setId(Integer.parseInt(path.substring(6)));
                dao.update(sp);
            }
            resp.sendRedirect(req.getContextPath() + "/packages/?success=saved");
        } catch (Exception e) { throw new ServletException(e); }
    }
 
    private void renderList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        List<ServicePackage> pkgs = dao.findAll();
        String ctx = req.getContextPath();
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(HtmlLayout.header("Service Packages", "packages", ctx));
        out.print(HtmlLayout.toast(req.getParameter("success")));
        out.print(HtmlLayout.breadcrumb("Dashboard",ctx+"/dashboard","Service Packages",null));
 
        out.printf("""
            <div class='card'>
              <div class='card-header'>
                <span class='card-title'>📦 Service Packages (%d)</span>
                <a href='%s/packages/new' class='btn btn-primary btn-sm'>＋ New Package</a>
              </div><div class='table-wrap'>
            """, pkgs.size(), ctx);
 
        if (pkgs.isEmpty()) {
            out.print("<div class='empty'><div class='empty-icon'>📦</div><p>No packages yet.</p></div>");
        } else {
            out.print("""
                <table><thead><tr>
                  <th>#</th><th>Name</th><th>Type</th><th>Amount</th><th>Priority</th><th>Actions</th>
                </tr></thead><tbody>
                """);
            for (ServicePackage sp : pkgs) {
                String typeBadge = switch (sp.getType()) {
                    case "voice" -> "<span class='badge badge-voice'>🎙 Voice</span>";
                    case "data"  -> "<span class='badge badge-data'>📶 Data</span>";
                    case "sms"   -> "<span class='badge badge-sms'>💬 SMS</span>";
                    default      -> HtmlLayout.e(sp.getType());
                };
                String unit = switch (sp.getType()) {
                    case "voice" -> " min"; case "data" -> " MB"; case "sms" -> " msg"; default -> "";
                };
                out.printf("""
                    <tr>
                      <td style='color:var(--muted);'>%d</td>
                      <td><strong>%s</strong></td><td>%s</td>
                      <td><strong>%s</strong><span style='color:var(--muted);font-size:12px;'>%s</span></td>
                      <td><span style='color:var(--muted);'>P%d</span></td>
                      <td><div style='display:flex;gap:6px;'>
                        <a href='%s/packages/edit/%d' class='btn btn-outline btn-sm'>✏️ Edit</a>
                        <a href='%s/packages/delete/%d' class='btn btn-danger btn-sm delete-link'>🗑️ Delete</a>
                      </div></td>
                    </tr>
                    """,
                        sp.getId(), HtmlLayout.e(sp.getName()), typeBadge,
                        sp.getAmount(), unit, sp.getPriority(),
                        ctx, sp.getId(), ctx, sp.getId());
            }
            out.print("</tbody></table>");
        }
        out.print("</div></div>");
        out.print(HtmlLayout.footer());
    }
 
    private void renderForm(HttpServletRequest req, HttpServletResponse resp,
                            ServicePackage sp, boolean editing) throws Exception {
        String ctx    = req.getContextPath();
        String action = editing ? ctx+"/packages/edit/"+sp.getId() : ctx+"/packages/new";
        String title  = editing ? "Edit Package" : "New Service Package";
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(HtmlLayout.header(title, "packages", ctx));
        out.print(HtmlLayout.breadcrumb("Dashboard",ctx+"/dashboard","Service Packages",ctx+"/packages/",title,null));
 
        String vs = "voice".equals(sp.getType()) ? " selected" : "";
        String ds = "data".equals(sp.getType())  ? " selected" : "";
        String ss = "sms".equals(sp.getType())   ? " selected" : "";
 
        out.printf("""
            <div class='card' style='max-width:720px;'>
              <div class='card-header'><span class='card-title'>%s %s</span></div>
              <div class='card-body'>
                <form method='post' action='%s'>
                  <div class='form-grid'>
                    <div class='form-group full'>
                      <label>Package Name *</label>
                      <input type='text' name='name' value='%s' placeholder='e.g. 10 GB Data Bundle' required>
                    </div>
                    <div class='form-group'>
                      <label>Service Type *</label>
                      <select name='type' required>
                        <option value=''>-- Select Type --</option>
                        <option value='voice'%s>🎙 Voice (minutes)</option>
                        <option value='data'%s>📶 Data (MB)</option>
                        <option value='sms'%s>💬 SMS (messages)</option>
                      </select>
                    </div>
                    <div class='form-group'>
                      <label>Amount (min / MB / msg)</label>
                      <input type='number' step='0.0001' name='amount'
                             value='%s' placeholder='e.g. 1000' min='0'>
                    </div>
                    <div class='form-group'>
                      <label>Priority (lower = consumed first)</label>
                      <input type='number' name='priority'
                             value='%s' placeholder='1' min='1' max='99'>
                    </div>
                  </div>
                  <div class='form-actions'>
                    <button type='submit' class='btn btn-primary'>💾 Save Package</button>
                    <a href='%s/packages/' class='btn btn-outline'>Cancel</a>
                  </div>
                </form>
              </div>
            </div>
            """,
                editing ? "✏️" : "➕", title, action,
                HtmlLayout.e(sp.getName()), vs, ds, ss,
                sp.getAmount() != null ? sp.getAmount() : "",
                sp.getPriority() > 0   ? sp.getPriority() : 1,
                ctx);
 
        out.print(HtmlLayout.footer());
    }
}
