package com.mycompany.telecom.billing.servlet;

import com.mycompany.telecom.billing.dao.RatePlanDAO;
import com.mycompany.telecom.billing.model.RatePlan;
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
@WebServlet("/rateplans/*")
public class RatePlanServlet extends HttpServlet {
 
    private final RatePlanDAO dao = new RatePlanDAO();
 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/"))  renderList(req, resp);
            else if (path.equals("/new"))          renderForm(req, resp, new RatePlan(), false);
            else if (path.startsWith("/edit/")) {
                RatePlan rp = dao.findById(Integer.parseInt(path.substring(6)));
                if (rp == null) { resp.sendError(404); return; }
                renderForm(req, resp, rp, true);
            } else if (path.startsWith("/delete/")) {
                dao.delete(Integer.parseInt(path.substring(8)));
                resp.sendRedirect(req.getContextPath() + "/rateplans/?success=deleted");
            } else resp.sendError(404);
        } catch (Exception e) { throw new ServletException(e); }
    }
 
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        try {
            RatePlan rp = new RatePlan();
            rp.setPlanName(req.getParameter("planName"));
            rp.setRorData(bd(req.getParameter("rorData")));
            rp.setRorVoice(bd(req.getParameter("rorVoice")));
            rp.setRorSms(bd(req.getParameter("rorSms")));
            rp.setMonthlyFee(bd(req.getParameter("monthlyFee")));
            if ("/new".equals(path)) dao.insert(rp);
            else if (path != null && path.startsWith("/edit/")) {
                rp.setId(Integer.parseInt(path.substring(6)));
                dao.update(rp);
            }
            resp.sendRedirect(req.getContextPath() + "/rateplans/?success=saved");
        } catch (Exception e) { throw new ServletException(e); }
    }
 
    private void renderList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        List<RatePlan> plans = dao.findAll();
        String ctx = req.getContextPath();
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(HtmlLayout.header("Rate Plans", "rateplans", ctx));
        out.print(HtmlLayout.toast(req.getParameter("success")));
        out.print(HtmlLayout.breadcrumb("Dashboard",ctx+"/dashboard","Rate Plans",null));
 
        out.printf("""
            <div class='card'>
              <div class='card-header'>
                <span class='card-title'>💳 Rate Plans (%d)</span>
                <a href='%s/rateplans/new' class='btn btn-primary btn-sm'>＋ New Plan</a>
              </div><div class='table-wrap'>
            """, plans.size(), ctx);
 
        if (plans.isEmpty()) {
            out.print("<div class='empty'><div class='empty-icon'>💳</div><p>No rate plans yet.</p></div>");
        } else {
            out.print("""
                <table><thead><tr>
                  <th>#</th><th>Plan Name</th><th>Monthly Fee</th>
                  <th>ROR Data</th><th>ROR Voice</th><th>ROR SMS</th><th>Actions</th>
                </tr></thead><tbody>
                """);
            for (RatePlan rp : plans) {
                out.printf("""
                    <tr>
                      <td style='color:var(--muted);'>%d</td>
                      <td><strong>%s</strong></td>
                      <td><span style='color:var(--cyan);font-weight:600;'>%s %s</span></td>
                      <td>%s%%</td><td>%s%%</td><td>%s%%</td>
                      <td><div style='display:flex;gap:6px;'>
                        <a href='%s/rateplans/edit/%d' class='btn btn-outline btn-sm'>✏️ Edit</a>
                        <a href='%s/rateplans/delete/%d' class='btn btn-danger btn-sm delete-link'>🗑️ Delete</a>
                      </div></td>
                    </tr>
                    """,
                        rp.getId(), HtmlLayout.e(rp.getPlanName()),
                        HtmlLayout.EGP, rp.getMonthlyFee(),
                        rp.getRorData(), rp.getRorVoice(), rp.getRorSms(),
                        ctx, rp.getId(), ctx, rp.getId());
            }
            out.print("</tbody></table>");
        }
        out.print("</div></div>");
        out.print(HtmlLayout.footer());
    }
 
    private void renderForm(HttpServletRequest req, HttpServletResponse resp,
                            RatePlan rp, boolean editing) throws Exception {
        String ctx    = req.getContextPath();
        String action = editing ? ctx+"/rateplans/edit/"+rp.getId() : ctx+"/rateplans/new";
        String title  = editing ? "Edit Rate Plan" : "New Rate Plan";
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(HtmlLayout.header(title, "rateplans", ctx));
        out.print(HtmlLayout.breadcrumb("Dashboard",ctx+"/dashboard","Rate Plans",ctx+"/rateplans/",title,null));
 
        out.printf("""
            <div class='card' style='max-width:720px;'>
              <div class='card-header'><span class='card-title'>%s %s</span></div>
              <div class='card-body'>
                <form method='post' action='%s'>
                  <div class='form-grid'>
                    <div class='form-group full'>
                      <label>Plan Name *</label>
                      <input type='text' name='planName' value='%s' placeholder='e.g. Gold Plan' required>
                    </div>
                    <div class='form-group'>
                      <label>Monthly Fee (%s)</label>
                      <input type='number' step='0.01' name='monthlyFee' value='%s' placeholder='0.00'>
                    </div>
                    <div class='form-group'>
                      <label>ROR Data Rate (%%)</label>
                      <input type='number' step='0.01' name='rorData' value='%s' placeholder='0.00'>
                    </div>
                    <div class='form-group'>
                      <label>ROR Voice Rate (%%)</label>
                      <input type='number' step='0.01' name='rorVoice' value='%s' placeholder='0.00'>
                    </div>
                    <div class='form-group'>
                      <label>ROR SMS Rate (%%)</label>
                      <input type='number' step='0.01' name='rorSms' value='%s' placeholder='0.00'>
                    </div>
                  </div>
                  <div class='form-actions'>
                    <button type='submit' class='btn btn-primary'>💾 Save Plan</button>
                    <a href='%s/rateplans/' class='btn btn-outline'>Cancel</a>
                  </div>
                </form>
              </div>
            </div>
            """,
                editing ? "✏️" : "➕", title, action,
                HtmlLayout.e(rp.getPlanName()),
                HtmlLayout.EGP, rp.getMonthlyFee(),
                rp.getRorData(), rp.getRorVoice(), rp.getRorSms(), ctx);
 
        out.print(HtmlLayout.footer());
    }
 
    private BigDecimal bd(String s) {
        return (s != null && !s.isBlank()) ? new BigDecimal(s) : null;
    }
}
