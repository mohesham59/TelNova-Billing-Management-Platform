/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
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
        // Already logged in → go straight to portal
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("portalUserId") != null) {
            resp.sendRedirect(req.getContextPath() + "/portal/home");
            return;
        }
        render(resp, null);
    }
 
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email    = req.getParameter("email");
        String password = req.getParameter("password");
 
        try {
            User user = userDAO.findByEmailAndPassword(email, password);
 
            if (user == null) {
                render(resp, "Invalid email or password. Please try again.");
                return;
            }
 
            // Store user info in session — never store the password
            HttpSession session = req.getSession(true);
            session.setAttribute("portalUserId",   user.getId());
            session.setAttribute("portalUserName", user.getName());
            session.setAttribute("portalEmail",    user.getEmail());
            resp.sendRedirect(req.getContextPath() + "/portal/home");
 
        } catch (Exception e) {
            render(resp, "A system error occurred. Please try again later.");
        }
    }
 
    private void render(HttpServletResponse resp, String error) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print("""
            <!DOCTYPE html><html lang='en'><head>
            <meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>
            <title>My Account — TeleMeter</title>
            <link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'>
            <style>
            *{box-sizing:border-box;margin:0;padding:0}
            :root{
              --bg:#0d1117;--surface:#161b22;--surface2:#21262d;--border:#30363d;
              --green:#2ea44f;--blue:#58a6ff;--danger:#f85149;--text:#e6edf3;--muted:#8b949e;
            }
            body{font-family:'Inter',system-ui,sans-serif;background:var(--bg);color:var(--text);
                 min-height:100vh;display:flex;align-items:center;justify-content:center;}
            .orb{position:fixed;border-radius:50%;filter:blur(100px);opacity:.11;pointer-events:none;}
            .o1{width:500px;height:500px;background:var(--green);top:-160px;right:-120px;}
            .o2{width:420px;height:420px;background:var(--blue);bottom:-120px;left:-120px;}
            .card{position:relative;z-index:1;width:100%;max-width:420px;background:var(--surface);
                  border:1px solid var(--border);border-radius:16px;padding:40px;
                  box-shadow:0 8px 48px rgba(0,0,0,.6);}
            .logo{text-align:center;margin-bottom:30px;}
            .logo-icon{width:64px;height:64px;border-radius:18px;margin:0 auto 14px;
              background:linear-gradient(135deg,var(--green),var(--blue));
              display:flex;align-items:center;justify-content:center;font-size:30px;}
            .logo h1{font-size:21px;font-weight:700;}
            .logo p{font-size:13px;color:var(--muted);margin-top:5px;}
            .fg{margin-bottom:18px;}
            label{display:block;font-size:11px;font-weight:600;color:var(--muted);
                  letter-spacing:.6px;text-transform:uppercase;margin-bottom:7px;}
            input{width:100%;background:var(--surface2);border:1px solid var(--border);
                  border-radius:8px;color:var(--text);padding:11px 14px;
                  font-size:14px;font-family:inherit;}
            input:focus{outline:none;border-color:var(--blue);
                        box-shadow:0 0 0 3px rgba(88,166,255,.12);}
            .err{padding:12px 14px;border-radius:8px;margin-bottom:18px;
                 background:rgba(248,81,73,.1);border:1px solid rgba(248,81,73,.3);
                 color:#f85149;font-size:13px;display:flex;align-items:center;gap:8px;}
            .btn{width:100%;padding:12px;margin-top:4px;
                 background:linear-gradient(135deg,var(--green),#3fb950);color:#fff;
                 border:none;border-radius:8px;font-size:14px;font-weight:600;
                 cursor:pointer;font-family:inherit;transition:opacity .15s;}
            .btn:hover{opacity:.85;}
            .footer-link{text-align:center;margin-top:18px;font-size:12px;color:var(--muted);}
            .footer-link a{color:var(--blue);text-decoration:none;}
            .footer-link a:hover{text-decoration:underline;}
            </style></head><body>
            <div class='orb o1'></div><div class='orb o2'></div>
            <div class='card'>
              <div class='logo'>
                <div class='logo-icon'>📱</div>
                <h1>My Account</h1>
                <p>Sign in to view your contracts and usage</p>
              </div>
            """);
 
        if (error != null) {
            out.printf("<div class='err'>⚠️ %s</div>", escHtml(error));
        }
 
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
 
    private String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
