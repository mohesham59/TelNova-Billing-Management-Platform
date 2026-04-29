package com.a3m.billing.invoice;

import com.telecom.db.DBConnection;
import java.sql.Connection;
import java.util.List;

public class InvoiceMain {

    public static void main(String[] args) {
        Connection conn = DBConnection.getConnection();

        if (conn == null) {
            System.err.println("Failed to connect to database!");
            return;
        }

        try {
            InvoiceDataLoader loader       = new InvoiceDataLoader(conn);
            InvoicePdfGenerator generator  = new InvoicePdfGenerator();

            // ── Find all bills without PDFs ──
            List<Integer> billIds = loader.getUnprocessedBillIds();

            if (billIds.isEmpty()) {
                System.out.println("No unprocessed bills found.");
                return;
            }

            System.out.println("Found " + billIds.size() + " bills to process.\n");

            int success = 0;
            int failed  = 0;

            // ── Generate PDF for each bill ──
            for (int billId : billIds) {
                System.out.println("Processing Bill #" + billId + "...");

                try {
                    InvoiceData data = loader.load(billId);
                    String pdfPath   = generator.generate(data);
                    loader.updatePdfPath(billId, pdfPath);

                    System.out.println("  ✓ Customer: " + data.getCustomerName());
                    System.out.println("  ✓ Total:    " + data.getTotalAmount() + " EGP");
                    System.out.println("  ✓ PDF:      " + pdfPath);
                    System.out.println();
                    success++;

                } catch (Exception e) {
                    System.err.println("  ✗ FAILED: " + e.getMessage());
                    System.err.println();
                    failed++;
                }
            }

            System.out.println("=================================");
            System.out.println("Success: " + success);
            System.out.println("Failed:  " + failed);
            System.out.println("=================================");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { conn.close(); } catch (Exception ignored) {}
        }
    }
}