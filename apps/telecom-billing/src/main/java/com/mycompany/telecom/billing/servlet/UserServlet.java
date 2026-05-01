/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.telecom.billing.servlet;

import com.mycompany.telecom.billing.dao.UserDAO;
import com.mycompany.telecom.billing.model.User;
import com.mycompany.telecom.billing.util.HtmlLayout;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author Ali
 */
@WebServlet("/users/*")
public class UserServlet extends HttpServlet {

    private final UserDAO dao = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/")) {
                renderList(req, resp);
            } else if (path.equals("/new")) {
                renderForm(req, resp, new User(), false);
            } else if (path.startsWith("/edit/")) {
                String id = path.substring(6);
                User u = dao.findById(id);
                if (u == null) {
                    resp.sendError(404);
                    return;
                }
                renderForm(req, resp, u, true);
            } else if (path.startsWith("/delete/")) {
                dao.delete(path.substring(8));
                resp.sendRedirect(req.getContextPath() + "/users/?success=deleted");
            } else {
                resp.sendError(404);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        try {
            User u = new User();
            u.setId(req.getParameter("id"));
            u.setName(req.getParameter("name"));
            u.setAddress(req.getParameter("address"));
            String bd = req.getParameter("birthdate");
            if (bd != null && !bd.isBlank()) {
                u.setBirthdate(LocalDate.parse(bd));
            }

            if ("/new".equals(path)) {
                dao.insert(u);
            } else if (path != null && path.startsWith("/edit/")) {
                dao.update(u);
            }
            resp.sendRedirect(req.getContextPath() + "/users/?success=saved");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    // ── LIST ─────────────────────────────────────────────────────────────────
    private void renderList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        List<User> users = dao.findAll();
        String ctx = req.getContextPath();
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(HtmlLayout.header("Customers", "users", ctx));
        out.print(HtmlLayout.toast(req.getParameter("success")));
        out.print(HtmlLayout.breadcrumb("Dashboard", ctx + "/dashboard", "Customers", null));

        out.printf("""
            <div class='card'>
              <div class='card-header'>
                <span class='card-title'>👤 All Customers (%d)</span>
                <a href='%s/users/new' class='btn btn-primary btn-sm'>＋ Add Customer</a>
              </div>
              <div class='table-wrap'>
            """, users.size(), ctx);

        if (users.isEmpty()) {
            out.print("<div class='empty'><div class='empty-icon'>👤</div><p>No customers yet. Add your first one!</p></div>");
        } else {
            out.print("""
                <table>
                  <thead><tr>
                    <th>ID</th><th>Name</th><th>Address</th><th>Birthdate</th><th>Actions</th>
                  </tr></thead><tbody>
                """);
            for (User u : users) {
                out.printf("""
                    <tr>
                      <td><code style='background:var(--surface2);padding:3px 8px;border-radius:5px;font-size:12px;'>%s</code></td>
                      <td><strong>%s</strong></td>
                      <td style='color:var(--muted);'>%s</td>
                      <td style='color:var(--muted);'>%s</td>
                      <td>
                        <div style='display:flex;gap:6px;'>
                          <a href='%s/users/edit/%s' class='btn btn-outline btn-sm'>✏️ Edit</a>
                          <a href='%s/users/delete/%s' class='btn btn-danger btn-sm delete-link'>🗑️ Delete</a>
                        </div>
                      </td>
                    </tr>
                    """,
                        HtmlLayout.e(u.getId()), HtmlLayout.e(u.getName()),
                        HtmlLayout.e(u.getAddress()), HtmlLayout.e(u.getBirthdate()),
                        ctx, HtmlLayout.e(u.getId()), ctx, HtmlLayout.e(u.getId()));
            }
            out.print("</tbody></table>");
        }
        out.print("</div></div>");
        out.print(HtmlLayout.footer());
    }

    // ── FORM (create / edit) ─────────────────────────────────────────────────
    private void renderForm(HttpServletRequest req, HttpServletResponse resp, User u, boolean editing)
            throws Exception {
        String ctx = req.getContextPath();
        String action = editing ? ctx + "/users/edit/" + u.getId() : ctx + "/users/new";
        String title = editing ? "Edit Customer" : "New Customer";
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(HtmlLayout.header(title, "users", ctx));
        out.print(HtmlLayout.breadcrumb(
                "Dashboard", ctx + "/dashboard",
                "Customers", ctx + "/users/",
                title, null));

        out.printf("""
            <div class='card' style='max-width:720px;'>
              <div class='card-header'><span class='card-title'>%s %s</span></div>
              <div class='card-body'>
                <form method='post' action='%s'>
                  <div class='form-grid'>
                    <div class='form-group'>
                      <label>Customer ID *</label>
                      <input type='text' name='id' value='%s' %s
                             maxlength='14' placeholder='e.g. CUST0001' required>
                    </div>
                    <div class='form-group'>
                      <label>Full Name *</label>
                      <input type='text' name='name' value='%s' placeholder='e.g. Ahmed Hassan' required>
                    </div>
                    <div class='form-group'>
                      <label>Birthdate</label>
                      <input type='date' name='birthdate' value='%s'>
                    </div>
                    <div class='form-group full'>
                      <label>Address</label>
                      <textarea name='address' placeholder='Street, City, Country'>%s</textarea>
                    </div>
                  </div>
                  <div class='form-actions'>
                    <button type='submit' class='btn btn-primary'>💾 Save Customer</button>
                    <a href='%s/users/' class='btn btn-outline'>Cancel</a>
                  </div>
                </form>
              </div>
            </div>
            """,
                editing ? "✏️" : "➕", title, action,
                HtmlLayout.e(u.getId()), editing ? "readonly style='opacity:.6'" : "",
                HtmlLayout.e(u.getName()),
                HtmlLayout.e(u.getBirthdate()),
                HtmlLayout.e(u.getAddress()),
                ctx);

        out.print(HtmlLayout.footer());
    }
}
