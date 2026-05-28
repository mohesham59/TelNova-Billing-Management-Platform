package com.mycompany.telecom.billing.servlet;

import com.mycompany.telecom.billing.util.DBConnection;
import com.mycompany.telecom.billing.util.HtmlLayout;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Ali
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession s = req.getSession(false);
        if (s != null && Boolean.TRUE.equals(s.getAttribute("loggedIn"))) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        render(resp, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        // -------------------------------------------------------
        // 1. Admin hardcoded check
        // -------------------------------------------------------
        if (ADMIN_USER.equals(username) && ADMIN_PASS.equals(password)) {
            HttpSession s = req.getSession(true);
            s.setAttribute("loggedIn", true);
            s.setAttribute("role", "admin");
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        // -------------------------------------------------------
        // 2. DB check للـ customers (بيسرش بالـ email)
        // -------------------------------------------------------
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT id, name FROM users WHERE email = ? AND password = ?")) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    HttpSession s = req.getSession(true);
                    s.setAttribute("loggedIn", true);
                    s.setAttribute("role", "customer");
                    s.setAttribute("userId", rs.getString("id"));
                    s.setAttribute("userName", rs.getString("name"));
                    resp.sendRedirect(req.getContextPath() + "/dashboard");
                } else {
                    render(resp, "Invalid username or password.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            render(resp, "Database error: " + e.getMessage());
        }
    }

    private void render(HttpServletResponse resp, String error) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print("""
            <!DOCTYPE html><html lang='en'><head>
            <meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>
            <title>Admin Login — Telnova</title>
            <link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap' rel='stylesheet'>
            <style>
            *{box-sizing:border-box;margin:0;padding:0}
            :root{--bg:#0a0e27;--sf:#1a1f3a;--s2:#1f2547;--bd:#2d3561;
                  --cyan:#00d4ff;--indigo:#6366f1;--red:#ef4444;--text:#e8eaf6;--muted:#9ca3af;}
            body{font-family:'Inter',system-ui,sans-serif;background:var(--bg);color:var(--text);
                 min-height:100vh;display:flex;align-items:center;justify-content:center;}
            .orbs{position:fixed;inset:0;pointer-events:none;overflow:hidden;}
            .orb{position:absolute;border-radius:50%;filter:blur(100px);opacity:.12;}
            .o1{width:450px;height:450px;background:var(--indigo);top:-140px;left:-100px;}
            .o2{width:400px;height:400px;background:var(--cyan);bottom:-120px;right:-100px;}
            .box{position:relative;z-index:1;width:100%;max-width:400px;background:var(--sf);
                 border:1px solid var(--bd);border-radius:16px;padding:40px;
                 box-shadow:0 8px 48px rgba(0,0,0,.6);}
            .logo{text-align:center;margin-bottom:30px;}
            .logo-icon{width:64px;height:64px;border-radius:18px;margin:0 auto 14px;
              background:linear-gradient(135deg,var(--indigo),var(--cyan));
              display:flex;align-items:center;justify-content:center;}
            .logo h1{font-size:21px;font-weight:700;}
            .logo h1 span{background:linear-gradient(135deg,var(--cyan),var(--indigo));
              -webkit-background-clip:text;-webkit-text-fill-color:transparent;}
            .logo p{font-size:13px;color:var(--muted);margin-top:5px;}
            .fg{margin-bottom:18px;}
            label{display:block;font-size:11px;font-weight:600;color:var(--muted);
                  letter-spacing:.6px;text-transform:uppercase;margin-bottom:7px;}
            input{width:100%;background:var(--s2);border:1px solid var(--bd);
                  border-radius:8px;color:var(--text);padding:11px 14px;
                  font-size:14px;font-family:inherit;}
            input:focus{outline:none;border-color:var(--indigo);
                        box-shadow:0 0 0 3px rgba(99,102,241,.15);}
            .err{padding:11px 14px;border-radius:8px;margin-bottom:18px;
                 background:rgba(239,68,68,.1);border:1px solid rgba(239,68,68,.3);
                 color:#f87171;font-size:13px;display:flex;align-items:center;gap:8px;}
            .btn{width:100%;padding:12px;margin-top:4px;
                 background:linear-gradient(135deg,var(--indigo),var(--cyan));
                 color:#fff;border:none;border-radius:8px;font-size:14px;
                 font-weight:600;cursor:pointer;font-family:inherit;transition:opacity .15s;}
            .btn:hover{opacity:.85;}
            .hint{text-align:center;margin-top:18px;font-size:12px;color:var(--muted);}
            code{background:var(--s2);border:1px solid var(--bd);
                 padding:2px 7px;border-radius:5px;font-size:11.5px;}
            .portal-link{text-align:center;margin-top:14px;font-size:12px;color:var(--muted);}
            .portal-link a{color:var(--cyan);text-decoration:none;}
            .portal-link a:hover{text-decoration:underline;}
            </style></head><body>
            <div class='orbs'><div class='orb o1'></div><div class='orb o2'></div></div>
            <div class='box'>
              <div class='logo'>
                <div class='logo-icon'>
                  <svg viewBox='0 0 100 100' style='width:36px;height:36px;'>
                    <path d='M 15 60 L 35 45 L 50 50 L 65 30 L 80 25 L 70 45 L 85 50 L 55 60 L 50 75 L 35 60 Z'
                          fill='white'/>
                  </svg>
                </div>
                <h1>Tel<span>nova</span> Admin</h1>
                <p>Sign in to manage the billing platform</p>
              </div>
            """);
        if (error != null)
            out.print("<div class='err'>⚠️ " + HtmlLayout.e(error) + "</div>");
        out.print("""
              <form method='post'>
                <div class='fg'><label>Username / Email</label>
                  <input type='text' name='username' placeholder='admin or user@example.com'
                         autofocus autocomplete='username'></div>
                <div class='fg'><label>Password</label>
                  <input type='password' name='password' placeholder='••••••••'
                         autocomplete='current-password'></div>
                <button class='btn'>Sign In →</button>
              </form>
              <p class='hint'>Admin: <code>admin</code> / <code>admin123</code></p>
              <p class='portal-link'>Customer? <a href='portal/login'>Go to My Account →</a></p>
            </div></body></html>
            """);
    }
}