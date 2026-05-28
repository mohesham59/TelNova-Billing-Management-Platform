package com.mycompany.telecom.billing.servlet;

import com.mycompany.telecom.billing.dao.*;
import com.mycompany.telecom.billing.model.*;
import com.mycompany.telecom.billing.util.HtmlLayout;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

/**
 *
 * @author Ali
 */
@WebServlet("/contracts/*")
public class ContractServlet extends HttpServlet {
 
    private final ContractDAO contractDAO = new ContractDAO();
    private final UserDAO     userDAO     = new UserDAO();
    private final RatePlanDAO planDAO     = new RatePlanDAO();
 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/"))  renderList(req, resp);
            else if (path.equals("/new"))          renderForm(req, resp, new Contract(), false);
            else if (path.startsWith("/edit/")) {
                Contract ct = contractDAO.findById(Integer.parseInt(path.substring(6)));
                if (ct == null) { resp.sendError(404); return; }
                renderForm(req, resp, ct, true);
            } else if (path.startsWith("/delete/")) {
                contractDAO.delete(Integer.parseInt(path.substring(8)));
                resp.sendRedirect(req.getContextPath() + "/contracts/?success=deleted");
            } else resp.sendError(404);
        } catch (Exception e) { throw new ServletException(e); }
    }
 
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        try {
            Contract ct = new Contract();
            ct.setUserId(req.getParameter("userId"));
            ct.setRatePlanId(Integer.parseInt(req.getParameter("ratePlanId")));
            ct.setMsisdn(req.getParameter("msisdn"));
            ct.setStatus(req.getParameter("status"));
            ct.setCreditLimit(bd(req.getParameter("creditLimit")));
            ct.setAvailableCredit(bd(req.getParameter("availableCredit")));
            String ad = req.getParameter("activationDate");
            ct.setActivationDate((ad != null && !ad.isBlank()) ? LocalDate.parse(ad) : LocalDate.now());
            String bcd = req.getParameter("billingCycleDay");
            ct.setBillingCycleDay((bcd != null && !bcd.isBlank()) ? Integer.parseInt(bcd) : 1);
 
            if ("/new".equals(path)) contractDAO.insert(ct);
            else if (path != null && path.startsWith("/edit/")) {
                ct.setId(Integer.parseInt(path.substring(6)));
                contractDAO.update(ct);
            }
            resp.sendRedirect(req.getContextPath() + "/contracts/?success=saved");
        } catch (Exception e) { throw new ServletException(e); }
    }
 
    private void renderList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        List<Contract> contracts = contractDAO.findAll();
        String ctx = req.getContextPath();
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(HtmlLayout.header("Contracts", "contracts", ctx));
        out.print(HtmlLayout.toast(req.getParameter("success")));
        out.print(HtmlLayout.breadcrumb("Dashboard",ctx+"/dashboard","Contracts",null));
 
        out.printf("""
            <div class='card'>
              <div class='card-header'>
                <span class='card-title'>📋 All Contracts (%d)</span>
                <a href='%s/contracts/new' class='btn btn-primary btn-sm'>＋ New Contract</a>
              </div><div class='table-wrap'>
            """, contracts.size(), ctx);
 
        if (contracts.isEmpty()) {
            out.print("<div class='empty'><div class='empty-icon'>📋</div><p>No contracts yet.</p></div>");
        } else {
            out.print("""
                <table><thead><tr>
                  <th>#</th><th>Customer</th><th>MSISDN</th><th>Rate Plan</th>
                  <th>Status</th><th>Credit Limit</th><th>Activated</th><th>Actions</th>
                </tr></thead><tbody>
                """);
            for (Contract ct : contracts) {
                String badge = switch (ct.getStatus()) {
                    case "active"    -> "<span class='badge badge-active'>● Active</span>";
                    case "suspended" -> "<span class='badge badge-suspended'>⏸ Suspended</span>";
                    case "de-active" -> "<span class='badge badge-deactive'>✕ De-active</span>";
                    case "on-hold"   -> "<span class='badge badge-onhold'>⏳ On-Hold</span>";
                    default          -> HtmlLayout.e(ct.getStatus());
                };
                out.printf("""
                    <tr>
                      <td style='color:var(--muted);'>%d</td>
                      <td><div style='font-weight:600;'>%s</div>
                          <div style='font-size:12px;color:var(--muted);'>%s</div></td>
                      <td><code style='background:var(--surface2);padding:3px 8px;border-radius:5px;font-size:12px;'>%s</code></td>
                      <td>%s</td><td>%s</td>
                      <td style='color:var(--cyan);font-weight:600;'>%s %s</td>
                      <td style='color:var(--muted);font-size:13px;'>%s</td>
                      <td><div style='display:flex;gap:6px;'>
                        <a href='%s/contracts/edit/%d' class='btn btn-outline btn-sm'>✏️ Edit</a>
                        <a href='%s/contracts/delete/%d' class='btn btn-danger btn-sm delete-link'>🗑️ Delete</a>
                      </div></td>
                    </tr>
                    """,
                        ct.getId(),
                        HtmlLayout.e(ct.getUserName()), HtmlLayout.e(ct.getUserId()),
                        HtmlLayout.e(ct.getMsisdn()), HtmlLayout.e(ct.getPlanName()),
                        badge, HtmlLayout.EGP, ct.getCreditLimit(),
                        ct.getActivationDate(),
                        ctx, ct.getId(), ctx, ct.getId());
            }
            out.print("</tbody></table>");
        }
        out.print("</div></div>");
        out.print(HtmlLayout.footer());
    }
 
    private void renderForm(HttpServletRequest req, HttpServletResponse resp,
                            Contract ct, boolean editing) throws Exception {
        String ctx    = req.getContextPath();
        String action = editing ? ctx+"/contracts/edit/"+ct.getId() : ctx+"/contracts/new";
        String title  = editing ? "Edit Contract" : "New Contract";
        List<User>     users = userDAO.findAll();
        List<RatePlan> plans = planDAO.findAll();
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(HtmlLayout.header(title, "contracts", ctx));
        out.print(HtmlLayout.breadcrumb("Dashboard",ctx+"/dashboard","Contracts",ctx+"/contracts/",title,null));
 
        out.printf("""
            <div class='card' style='max-width:860px;'>
              <div class='card-header'><span class='card-title'>%s %s</span></div>
              <div class='card-body'>
                <form method='post' action='%s'><div class='form-grid'>
            """, editing ? "✏️" : "➕", title, action);
 
        // Customer dropdown
        out.print("<div class='form-group'><label>Customer *</label><select name='userId' required><option value=''>-- Select Customer --</option>");
        for (User u : users) {
            String sel = u.getId().equals(ct.getUserId()) ? " selected" : "";
            out.printf("<option value='%s'%s>%s (%s)</option>",
                    HtmlLayout.e(u.getId()), sel, HtmlLayout.e(u.getName()), HtmlLayout.e(u.getId()));
        }
        out.print("</select></div>");
 
        // Rate plan dropdown
        out.print("<div class='form-group'><label>Rate Plan *</label><select name='ratePlanId' required><option value=''>-- Select Rate Plan --</option>");
        for (RatePlan rp : plans) {
            String sel = rp.getId() == ct.getRatePlanId() ? " selected" : "";
            out.printf("<option value='%d'%s>%s</option>", rp.getId(), sel, HtmlLayout.e(rp.getPlanName()));
        }
        out.print("</select></div>");
 
        // Status dropdown
        out.print("<div class='form-group'><label>Status *</label><select name='status' required>");
        for (String s : new String[]{"active","suspended","de-active","on-hold"}) {
            String sel = s.equals(ct.getStatus()) ? " selected" : "";
            out.printf("<option value='%s'%s>%s</option>", s, sel,
                    Character.toUpperCase(s.charAt(0)) + s.substring(1));
        }
        out.print("</select></div>");
 
        out.printf("""
                    <div class='form-group'>
                      <label>MSISDN (Phone Number) *</label>
                      <input type='text' name='msisdn' value='%s' placeholder='+201012345678' required maxlength='20'>
                    </div>
                    <div class='form-group'>
                      <label>Credit Limit (%s)</label>
                      <input type='number' step='0.01' name='creditLimit' value='%s' placeholder='0.00' min='0'>
                    </div>
                    <div class='form-group'>
                      <label>Available Credit (%s)</label>
                      <input type='number' step='0.01' name='availableCredit' value='%s' placeholder='0.00' min='0'>
                    </div>
                    <div class='form-group'>
                      <label>Activation Date</label>
                      <input type='date' name='activationDate' value='%s'>
                    </div>
                    <div class='form-group'>
                      <label>Billing Cycle Day (1–28)</label>
                      <input type='number' name='billingCycleDay' value='%d' min='1' max='28' placeholder='1'>
                    </div>
                  </div>
                  <div class='form-actions'>
                    <button type='submit' class='btn btn-primary'>💾 Save Contract</button>
                    <a href='%s/contracts/' class='btn btn-outline'>Cancel</a>
                  </div></form></div></div>
                """,
                HtmlLayout.e(ct.getMsisdn()),
                HtmlLayout.EGP, ct.getCreditLimit()     != null ? ct.getCreditLimit()     : "0",
                HtmlLayout.EGP, ct.getAvailableCredit() != null ? ct.getAvailableCredit() : "0",
                ct.getActivationDate()  != null ? ct.getActivationDate()  : LocalDate.now(),
                ct.getBillingCycleDay() > 0      ? ct.getBillingCycleDay() : 1,
                ctx);
 
        out.print(HtmlLayout.footer());
    }
 
    private BigDecimal bd(String s) {
        return (s != null && !s.isBlank()) ? new BigDecimal(s) : BigDecimal.ZERO;
    }
}
