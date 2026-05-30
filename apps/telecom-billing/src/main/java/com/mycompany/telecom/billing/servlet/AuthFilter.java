package com.mycompany.telecom.billing.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String ctx = req.getContextPath();

        // ── Static assets ─────────────────────────────────────────────────────
        if (uri.startsWith(ctx + "/css/") || uri.startsWith(ctx + "/js/")) {
            chain.doFilter(request, response); return;
        }

        // ── Landing page (index.html) — public, no auth needed ────────────────
        if (uri.equals(ctx + "/")
                || uri.equals(ctx + "/index.html")
                || uri.equals(ctx + "/index.htm")) {
            chain.doFilter(request, response); return;
        }

        // ── Admin login page ──────────────────────────────────────────────────
        if (uri.equals(ctx + "/login") || uri.equals(ctx + "/login/")) {
            chain.doFilter(request, response); return;
        }

        // ── Customer portal login page ────────────────────────────────────────
        if (uri.equals(ctx + "/portal/login") || uri.equals(ctx + "/portal/login/")) {
            chain.doFilter(request, response); return;
        }

        // ── All /portal/* routes — require portal session ─────────────────────
        if (uri.startsWith(ctx + "/portal/")) {
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("portalUserId") != null) {
                chain.doFilter(request, response);
            } else {
                resp.sendRedirect(ctx + "/portal/login");
            }
            return;
        }

        // ── All other routes — require admin session ──────────────────────────
        HttpSession session = req.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute("loggedIn"))) {
            chain.doFilter(request, response);
        } else {
            resp.sendRedirect(ctx + "/login");
        }
    }
}