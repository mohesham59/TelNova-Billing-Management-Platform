package com.mycompany.telecom.billing.servlet;

import com.mycompany.telecom.billing.dao.BillDAO;
import com.mycompany.telecom.billing.dao.ContractDAO;
import com.mycompany.telecom.billing.dao.UserDAO;
import com.mycompany.telecom.billing.model.BillSummary;
import com.mycompany.telecom.billing.model.Contract;
import com.mycompany.telecom.billing.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.ByteArrayOutputStream;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.*;

import java.io.InputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Ali
 */
@WebServlet("/portal/invoice/*")
public class InvoiceServlet extends HttpServlet {

    private final BillDAO billDAO = new BillDAO();
    private final ContractDAO contractDAO = new ContractDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // ── Resolve bill ID from path ─────────────────────────────────────────
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            resp.sendError(400, "Bill ID required");
            return;
        }

        int billId;
        try {
            billId = Integer.parseInt(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            resp.sendError(400, "Invalid bill ID");
            return;
        }

        // ── Session check ─────────────────────────────────────────────────────
        HttpSession session = req.getSession(false);
        String userId = (session != null) ? (String) session.getAttribute("portalUserId") : null;
        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/portal/login");
            return;
        }

        try {
            BillSummary bill = billDAO.findByIdAndUserId(billId, userId);
            if (bill == null) {
                resp.sendError(404, "Invoice not found");
                return;
            }

            User user = userDAO.findById(userId);
            Contract contract = contractDAO.findByUserId(userId).stream()
                    .findFirst().orElse(null);

            // ── Parameters ─────────────────────────────────────────────────────
            Map<String, Object> params = new HashMap<>();
            params.put("BILL_ID", bill.getId());
            params.put("CUSTOMER_NAME", user != null ? user.getName() : "");
            params.put("CUSTOMER_EMAIL", user != null ? user.getEmail() : "");
            params.put("MSISDN", contract != null ? contract.getMsisdn() : "");
            params.put("PLAN_NAME", contract != null ? contract.getPlanName() : "");
            params.put("BILLING_DATE", bill.getBillingDate() != null ? bill.getBillingDate().toString() : "");
            params.put("PERIOD_START", bill.getPeriodStart() != null ? bill.getPeriodStart().toString() : "");
            params.put("PERIOD_END", bill.getPeriodEnd() != null ? bill.getPeriodEnd().toString() : "");
            params.put("RECURRING_FEES", fmt(bill.getRecurringFees()));
            params.put("ONE_TIME_FEES", fmt(bill.getOneTimeFees()));
            params.put("TAXES", fmt(bill.getTaxes()));
            params.put("SUBTOTAL", fmt(bill.getSubtotal()));
            params.put("TOTAL_AMOUNT", fmt(bill.getTotalAmount()));
            params.put("VOICE_USAGE", bill.getVoiceFormatted());
            params.put("DATA_USAGE", String.valueOf(bill.getDataUsage()));
            params.put("SMS_USAGE", String.valueOf(bill.getSmsUsage()));

            // ── Load report ────────────────────────────────────────────────────
            InputStream jrxml = getServletContext()
                    .getResourceAsStream("/WEB-INF/classes/reports/invoice.jrxml");

            if (jrxml == null) {
                jrxml = getClass().getClassLoader()
                        .getResourceAsStream("reports/invoice.jrxml");
            }

            if (jrxml == null) {
                resp.sendError(500, "Report template not found");
                return;
            }

            // ── Compile & fill ────────────────────────────────────────────────
            JasperReport jasperReport = JasperCompileManager.compileReport(jrxml);
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, params, new JREmptyDataSource()
            );

            // ── Export to PDF (SAFE METHOD) ───────────────────────────────────
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, baos);

            byte[] pdfBytes = baos.toByteArray();

            // ── Response headers ──────────────────────────────────────────────
            resp.setContentType("application/pdf");
            resp.setContentLength(pdfBytes.length);
            resp.setHeader("Content-Disposition",
                    "attachment; filename=\"Telnova-Invoice-" + billId + ".pdf\"");

            // ── Write response ────────────────────────────────────────────────
            ServletOutputStream out = resp.getOutputStream();
            out.write(pdfBytes);
            out.flush();
            out.close();

        } catch (Exception e) {
            throw new ServletException("Failed to generate invoice PDF", e);
        }
    }

    private String fmt(java.math.BigDecimal v) {
        if (v == null) {
            return "0.00";
        }
        return v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
