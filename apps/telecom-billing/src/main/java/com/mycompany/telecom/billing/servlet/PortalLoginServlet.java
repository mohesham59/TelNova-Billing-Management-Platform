package com.mycompany.telecom.billing.servlet;

import com.mycompany.telecom.billing.dao.UserDAO;
import com.mycompany.telecom.billing.model.User;
 
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
 
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author Ali
 */
@WebServlet("/portal/login")
public class PortalLoginServlet extends HttpServlet {
 
    private final UserDAO userDAO = new UserDAO();
 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession s = req.getSession(false);
        if (s != null && s.getAttribute("portalUserId") != null) {
            resp.sendRedirect(req.getContextPath() + "/portal/home"); return;
        }
        render(resp, null);
    }
 
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email    = req.getParameter("email");
        String password = req.getParameter("password");
        try {
            User user = userDAO.findByEmailAndPassword(email, password);
            if (user == null) { render(resp, "Invalid email or password."); return; }
            HttpSession s = req.getSession(true);
            s.setAttribute("portalUserId",   user.getId());
            s.setAttribute("portalUserName", user.getName());
            s.setAttribute("portalEmail",    user.getEmail());
            resp.sendRedirect(req.getContextPath() + "/portal/home");
        } catch (Exception e) {
            render(resp, "A system error occurred. Please try again.");
        }
    }
 
    private void render(HttpServletResponse resp, String error) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print("""
            <!DOCTYPE html><html lang='en'><head>
            <meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>
            <title>My Account — Telnova</title>
            <link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'>
            <style>
            *{box-sizing:border-box;margin:0;padding:0}
            :root{--bg:#0a0e27;--sf:#1a1f3a;--s2:#1f2547;--bd:#2d3561;
                  --cyan:#00d4ff;--indigo:#6366f1;--red:#ef4444;--text:#e8eaf6;--muted:#9ca3af;}
            body{font-family:'Inter',system-ui,sans-serif;background:var(--bg);color:var(--text);
                 min-height:100vh;display:flex;align-items:center;justify-content:center;}
            .orb{position:fixed;border-radius:50%;filter:blur(100px);opacity:.11;pointer-events:none;}
            .o1{width:500px;height:500px;background:var(--indigo);top:-160px;right:-120px;}
            .o2{width:420px;height:420px;background:var(--cyan);bottom:-120px;left:-120px;}
            .card{position:relative;z-index:1;width:100%;max-width:420px;background:var(--sf);
                  border:1px solid var(--bd);border-radius:16px;padding:40px;
                  box-shadow:0 8px 48px rgba(0,0,0,.6);}
            .logo{text-align:center;margin-bottom:30px;}
            .logo-icon{width:64px;height:64px;border-radius:18px;margin:0 auto 14px;
              background:linear-gradient(135deg,var(--indigo),var(--cyan));
              display:flex;align-items:center;justify-content:center;}
            .logo h1{font-size:21px;font-weight:700;}
            .logo h1 .tel{color:#fff;}
            .logo h1 .nova{background:linear-gradient(135deg,var(--cyan),var(--indigo));
              -webkit-background-clip:text;-webkit-text-fill-color:transparent;}
            .logo p{font-size:13px;color:var(--muted);margin-top:5px;}
            .fg{margin-bottom:18px;}
            label{display:block;font-size:11px;font-weight:600;color:var(--muted);
                  letter-spacing:.6px;text-transform:uppercase;margin-bottom:7px;}
            input{width:100%;background:var(--s2);border:1px solid var(--bd);border-radius:8px;
                  color:var(--text);padding:11px 14px;font-size:14px;font-family:inherit;}
            input:focus{outline:none;border-color:var(--cyan);box-shadow:0 0 0 3px rgba(0,212,255,.1);}
            .err{padding:12px 14px;border-radius:8px;margin-bottom:18px;
                 background:rgba(239,68,68,.1);border:1px solid rgba(239,68,68,.3);
                 color:#f87171;font-size:13px;}
            .btn{width:100%;padding:12px;margin-top:4px;
                 background:linear-gradient(135deg,var(--indigo),var(--cyan));
                 color:#fff;border:none;border-radius:8px;font-size:14px;
                 font-weight:600;cursor:pointer;font-family:inherit;transition:opacity .15s;}
            .btn:hover{opacity:.85;}
            .footer-link{text-align:center;margin-top:18px;font-size:12px;color:var(--muted);}
            .footer-link a{color:var(--cyan);text-decoration:none;}
            .footer-link a:hover{text-decoration:underline;}
            </style></head><body>
            <div class='orb o1'></div><div class='orb o2'></div>
            <div class='card'>
              <div class='logo'>
                <div class='logo-icon'>
                  <svg viewBox='0 0 100 100' style='width:36px;height:36px;'>
                    <path d='M 15 60 L 35 45 L 50 50 L 65 30 L 80 25 L 70 45 L 85 50 L 55 60 L 50 75 L 35 60 Z' fill='white'/>
                  </svg>
                </div>
                <h1><span class='tel'>Tel</span><span class='nova'>nova</span></h1>
                <p>Sign in to view your account and invoices</p>
              </div>
            """);
        if (error != null) out.printf("<div class='err'>⚠️ %s</div>", error.replace("<","&lt;"));
        out.print("""
              <form method='post'>
                <div class='fg'>
                  <label>Email Address</label>
                  <input type='email' name='email' placeholder='you@example.com'
                         autofocus autocomplete='username'>
                </div>
                <div class='fg'>
                  <label>Password</label>
                  <input type='password' name='password' placeholder='••••••••'
                         autocomplete='current-password'>
                </div>
                <button class='btn'>Sign In →</button>
              </form>
              <p class='footer-link'>Admin? <a href='../login'>Go to Admin Panel</a></p>
            </div></body></html>
            """);
    }
}
