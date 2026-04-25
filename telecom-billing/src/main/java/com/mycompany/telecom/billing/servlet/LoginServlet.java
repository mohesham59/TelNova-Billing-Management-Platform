/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.telecom.billing.servlet;

import com.mycompany.telecom.billing.util.HtmlLayout;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

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
        HttpSession session = req.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute("loggedIn"))) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        render(resp, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (ADMIN_USER.equals(req.getParameter("username"))
                && ADMIN_PASS.equals(req.getParameter("password"))) {
            HttpSession s = req.getSession(true);
            s.setAttribute("loggedIn", true);
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } else {
            render(resp, "Invalid username or password. Please try again.");
        }
    }

    private void render(HttpServletResponse resp, String error) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print("""
            <!DOCTYPE html><html lang='en'><head>
            <meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>
            <title>Sign In — Telecom Admin</title>
            <link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap' rel='stylesheet'>
            <style>
            *{box-sizing:border-box;margin:0;padding:0}
            :root{--bg:#0f1117;--surface:#1a1d27;--surface2:#22263a;--border:#2e3248;
                  --accent:#6c63ff;--accent2:#48cfad;--danger:#ff5c6c;--text:#e8eaf6;--muted:#8b8fa8;}
            body{font-family:'Inter',system-ui,sans-serif;background:var(--bg);color:var(--text);
                 min-height:100vh;display:flex;align-items:center;justify-content:center;}
            .orbs{position:fixed;inset:0;pointer-events:none;overflow:hidden;}
            .orb{position:absolute;border-radius:50%;filter:blur(90px);opacity:.15;}
            .o1{width:420px;height:420px;background:var(--accent);top:-120px;left:-120px;}
            .o2{width:380px;height:380px;background:var(--accent2);bottom:-100px;right:-100px;}
            .box{position:relative;z-index:1;width:100%;max-width:400px;background:var(--surface);
                 border:1px solid var(--border);border-radius:16px;padding:40px;
                 box-shadow:0 8px 48px rgba(0,0,0,.55);}
            .logo{text-align:center;margin-bottom:30px;}
            .licon{width:64px;height:64px;border-radius:18px;margin:0 auto 14px;
                   background:linear-gradient(135deg,var(--accent),var(--accent2));
                   display:flex;align-items:center;justify-content:center;font-size:30px;}
            .logo h1{font-size:21px;font-weight:700;}
            .logo p{font-size:13px;color:var(--muted);margin-top:5px;}
            .fg{margin-bottom:18px;}
            label{display:block;font-size:11px;font-weight:600;color:var(--muted);
                  letter-spacing:.6px;text-transform:uppercase;margin-bottom:7px;}
            input{width:100%;background:var(--surface2);border:1px solid var(--border);
                  border-radius:8px;color:var(--text);padding:11px 14px;font-size:14px;font-family:inherit;}
            input:focus{outline:none;border-color:var(--accent);box-shadow:0 0 0 3px rgba(108,99,255,.15);}
            .err{padding:11px 14px;border-radius:8px;margin-bottom:18px;
                 background:rgba(255,92,108,.12);border:1px solid rgba(255,92,108,.3);
                 color:#ff8a94;font-size:13px;display:flex;align-items:center;gap:8px;}
            .btn{width:100%;padding:12px;margin-top:4px;
                 background:linear-gradient(135deg,var(--accent),#8b84ff);
                 color:#fff;border:none;border-radius:8px;font-size:14px;
                 font-weight:600;cursor:pointer;font-family:inherit;transition:opacity .15s;}
            .btn:hover{opacity:.85;}
            .hint{text-align:center;margin-top:18px;font-size:12px;color:var(--muted);}
            code{background:var(--surface2);border:1px solid var(--border);
                 padding:2px 7px;border-radius:5px;font-size:11.5px;}
            </style></head><body>
            <div class='orbs'><div class='orb o1'></div><div class='orb o2'></div></div>
            <div class='box'>
              <div class='logo'>
                <div class='licon'>📡</div>
                <h1>TelecomBill Admin</h1>
                <p>Sign in to manage your billing platform</p>
              </div>
            """);

        if (error != null)
            out.print("<div class='err'>⚠️ " + HtmlLayout.e(error) + "</div>");

        out.print("""
              <form method='post'>
                <div class='fg'><label>Username</label>
                  <input type='text' name='username' placeholder='admin' autofocus autocomplete='username'></div>
                <div class='fg'><label>Password</label>
                  <input type='password' name='password' placeholder='••••••••' autocomplete='current-password'></div>
                <button class='btn'>Sign In →</button>
              </form>
              <p class='hint'>Default: <code>admin</code> / <code>admin123</code></p>
            </div></body></html>
            """);
    }
}
