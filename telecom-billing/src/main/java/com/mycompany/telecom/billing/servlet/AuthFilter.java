/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Filter.java to edit this template
 */
package com.mycompany.telecom.billing.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;

/**
 *
 * @author Ali
 */
@WebFilter("/*")
public class AuthFilter implements Filter {
 
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
 
        String uri = req.getRequestURI();
        String ctx = req.getContextPath();
 
        // ── Always allow static assets ────────────────────────────────────────
        if (uri.startsWith(ctx + "/css/") || uri.startsWith(ctx + "/js/")) {
            chain.doFilter(request, response);
            return;
        }
 
        // ── Admin login — no session needed ───────────────────────────────────
        if (uri.equals(ctx + "/login") || uri.equals(ctx + "/login/")) {
            chain.doFilter(request, response);
            return;
        }
 
        // ── Customer portal login — no session needed ─────────────────────────
        if (uri.equals(ctx + "/portal/login") || uri.equals(ctx + "/portal/login/")) {
            chain.doFilter(request, response);
            return;
        }
 
        // ── Customer portal routes — require portal session ───────────────────
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
