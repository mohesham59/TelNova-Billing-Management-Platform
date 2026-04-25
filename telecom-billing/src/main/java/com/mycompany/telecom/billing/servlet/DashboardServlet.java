/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.telecom.billing.servlet;

import com.mycompany.telecom.billing.dao.*;
import com.mycompany.telecom.billing.util.HtmlLayout;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author Ali
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final UserDAO           userDAO    = new UserDAO();
    private final ContractDAO       contractDAO= new ContractDAO();
    private final RatePlanDAO       planDAO    = new RatePlanDAO();
    private final ServicePackageDAO pkgDAO     = new ServicePackageDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        int users = 0, contracts = 0, plans = 0, packages = 0;
        try {
            users     = userDAO.findAll().size();
            contracts = contractDAO.findAll().size();
            plans     = planDAO.findAll().size();
            packages  = pkgDAO.findAll().size();
        } catch (Exception ignored) {}

        String ctx = req.getContextPath();
        out.print(HtmlLayout.header("Dashboard", "dashboard", ctx));

        out.print("""
            <div style='margin-bottom:24px;'>
              <h2 style='font-size:22px;font-weight:700;margin-bottom:6px;'>Welcome back, Admin 👋</h2>
              <p style='color:var(--muted);font-size:14px;'>Here's a summary of your telecom billing platform.</p>
            </div>
            <div style='display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:16px;margin-bottom:28px;'>
            """);

        stat(out, "👤", "purple", "Total Customers",     users,     ctx + "/users/");
        stat(out, "📋", "teal",   "Active Contracts",    contracts, ctx + "/contracts/");
        stat(out, "💳", "orange", "Rate Plans",          plans,     ctx + "/rateplans/");
        stat(out, "📦", "red",    "Service Packages",    packages,  ctx + "/packages/");

        out.print("</div>");

        // Quick-access cards
        out.print("""
            <div style='display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:16px;'>
            """);
        quickCard(out, "👤", "Manage Customers",
            "Add, update, and remove customer records.",
            ctx + "/users/new", "Add Customer", ctx + "/users/", "View All");
        quickCard(out, "📋", "Manage Contracts",
            "Link customers to rate plans and phone numbers.",
            ctx + "/contracts/new", "New Contract", ctx + "/contracts/", "View All");
        quickCard(out, "💳", "Rate Plans",
            "Define pricing, ROR rates, and monthly fees.",
            ctx + "/rateplans/new", "New Plan", ctx + "/rateplans/", "View All");
        quickCard(out, "📦", "Service Packages",
            "Create voice, data, and SMS quota bundles.",
            ctx + "/packages/new", "New Package", ctx + "/packages/", "View All");
        out.print("</div>");

        out.print(HtmlLayout.footer());
    }

    private void stat(PrintWriter out, String icon, String cls, String label, int val, String href) {
        out.printf("""
            <a href='%s' style='text-decoration:none;color:inherit;'>
              <div class='stat-card' style='transition:border-color .15s;cursor:pointer;'>
                <div class='stat-icon %s'>%s</div>
                <div>
                  <div class='stat-label'>%s</div>
                  <div class='stat-value'>%d</div>
                </div>
              </div>
            </a>
            """, href, cls, icon, label, val);
    }

    private void quickCard(PrintWriter out, String icon, String title, String desc,
                           String href1, String btn1, String href2, String btn2) {
        out.printf("""
            <div class='card'>
              <div class='card-body'>
                <div style='font-size:28px;margin-bottom:10px;'>%s</div>
                <h3 style='font-size:15px;font-weight:600;margin-bottom:6px;'>%s</h3>
                <p style='font-size:13px;color:var(--muted);margin-bottom:16px;'>%s</p>
                <div style='display:flex;gap:8px;'>
                  <a href='%s' class='btn btn-primary btn-sm'>%s</a>
                  <a href='%s' class='btn btn-outline btn-sm'>%s</a>
                </div>
              </div>
            </div>
            """, icon, title, desc, href1, btn1, href2, btn2);
    }
}