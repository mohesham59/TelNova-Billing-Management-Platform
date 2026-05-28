/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.a3m.billing.job;

import com.a3m.billing.invoice.InvoiceData;
import com.a3m.billing.invoice.InvoiceDataLoader;
import com.a3m.billing.invoice.InvoicePdfGenerator;
import com.telecom.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
/**
 *
 * @author mohesham
 */


/**
 * Monthly billing batch job.
 *
 * Steps:
 *   1. Rate all unrated CDRs
 *   2. Generate bills for all active contracts
 *   3. Generate PDF invoices for each new bill
 *   4. Reset the billing cycle counters
 *
 * Run manually:
 *   java -cp "target/classes:target/dependency/*" com.a3m.billing.job.BillingCycleJob
 *
 * Crontab (1st of every month at 00:30):
 *   30 0 1 * * /home/eissa/biilig_project/TeleMeter-Billing-Management-Platform/scripts/run_billing.sh
 */
public class BillingCycleJob {

    public static void main(String[] args) {

        System.out.println("=== TeleMeter Billing Cycle Job ===");
        System.out.println("Started at: " + java.time.LocalDateTime.now());

        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            System.err.println("FATAL: Could not connect to the database. Aborting.");
            System.exit(1);
        }

        try {
            // Billing date = first day of the current month
            LocalDate billingDate = LocalDate.now().withDayOfMonth(1);
            System.out.println("Billing date: " + billingDate);

            // ── Step 1: Rate CDRs ─────────────────────────────────────────────
            System.out.println("\n[Step 1] Rating CDRs...");
            try (Statement st = conn.createStatement()) {
                st.execute("SELECT * FROM rate_cdrs()");
            }
            System.out.println("[Step 1] Done.");

            // ── Step 2: Generate bills ────────────────────────────────────────
            System.out.println("\n[Step 2] Generating bills...");
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM bill_all_active_contracts(?::DATE)")) {
                ps.setString(1, billingDate.toString());
                ps.execute();
            }
            System.out.println("[Step 2] Done.");

            // ── Step 3: Generate PDF invoices ─────────────────────────────────
            System.out.println("\n[Step 3] Generating PDF invoices...");
            InvoiceDataLoader loader    = new InvoiceDataLoader(conn);
            InvoicePdfGenerator generator = new InvoicePdfGenerator();

            List<Integer> billIds = loader.getUnprocessedBillIds();
            System.out.println("         Found " + billIds.size() + " unprocessed bill(s).");

            int success = 0;
            int failed  = 0;
            for (int billId : billIds) {
                try {
                    InvoiceData data   = loader.load(billId);
                    String pdfPath     = generator.generate(data);
                    loader.updatePdfPath(billId, pdfPath);
                    System.out.println("         [OK] Bill #" + billId + " → " + pdfPath);
                    success++;
                } catch (Exception e) {
                    System.err.println("         [FAIL] Bill #" + billId + ": " + e.getMessage());
                    failed++;
                }
            }
            System.out.println("[Step 3] Done. Success: " + success + "  Failed: " + failed);

            // ── Step 4: Reset billing cycle ───────────────────────────────────
            System.out.println("\n[Step 4] Resetting billing cycle...");
            try (Statement st = conn.createStatement()) {
                st.execute("SELECT * FROM reset_billing_cycle()");
            }
            System.out.println("[Step 4] Done.");

            System.out.println("\n=== Billing cycle completed successfully ===");

        } catch (Exception e) {
            System.err.println("\nERROR during billing cycle: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);

        } finally {
            try { conn.close(); } catch (Exception ignored) {}
        }
    }
}