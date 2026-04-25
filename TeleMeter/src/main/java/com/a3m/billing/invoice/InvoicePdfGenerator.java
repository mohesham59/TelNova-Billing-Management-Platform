package com.a3m.billing.invoice;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.constants.StandardFonts;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class InvoicePdfGenerator {

    // ── Colors ──
    private static final DeviceRgb PRIMARY_COLOR   = new DeviceRgb(0, 102, 153);   // dark teal
    private static final DeviceRgb HEADER_BG       = new DeviceRgb(0, 102, 153);   // dark teal
    private static final DeviceRgb HEADER_TEXT      = new DeviceRgb(255, 255, 255); // white
    private static final DeviceRgb LIGHT_BG        = new DeviceRgb(240, 248, 255); // light blue
    private static final DeviceRgb TOTAL_BG        = new DeviceRgb(0, 77, 115);    // darker teal
    private static final DeviceRgb BORDER_COLOR    = new DeviceRgb(200, 200, 200); // light gray

    private static final String OUTPUT_DIR = "/tmp/invoice_pdfs";
    private static final String COMPANY_NAME = "A3M";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private PdfFont fontBold;
    private PdfFont fontRegular;

      /**
     * Generate PDF invoice for the given data.
     * Returns the file path of the generated PDF.
     */
    public String generate(InvoiceData data) throws IOException {
        // ── Ensure output directory exists ──
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // ── Build filename: INV_001_Ahmed_2026-05.pdf ──
        String firstName = data.getCustomerName().split(" ")[0];
        String monthStr  = data.getPeriodStart().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String fileName  = String.format("INV_%03d_%s_%s.pdf",
                data.getBillId(), firstName, monthStr);
        String filePath  = OUTPUT_DIR + File.separator + fileName;

        // ── Delete old file if exists (in case of re-generation) ──
        File pdfFile = new File(filePath);
        if (pdfFile.exists()) {
            pdfFile.delete();
        }

        // ── Create PDF with proper resource management ──
        PdfWriter writer     = null;
        PdfDocument pdfDoc   = null;
        Document doc         = null;

        try {
            writer  = new PdfWriter(filePath);
            pdfDoc  = new PdfDocument(writer);
            doc     = new Document(pdfDoc, PageSize.A4);
            doc.setMargins(36, 36, 36, 36);

            // ── Load fonts ──
            fontBold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // ── Build sections ──
            addCompanyHeader(doc, data);
            addCustomerInfo(doc, data);
            addPackagesTable(doc, data);
            addUsageSummary(doc, data);
            addChargesTable(doc, data);
            addFooter(doc);

            doc.close();

        } catch (Exception e) {
            // ── If anything fails, close and delete the corrupted file ──
            System.err.println("Error generating PDF for bill #" + data.getBillId()
                    + ": " + e.getMessage());
            e.printStackTrace();

            try { if (doc != null) doc.close(); }       catch (Exception ignored) {}
            try { if (pdfDoc != null) pdfDoc.close(); }  catch (Exception ignored) {}
            try { if (writer != null) writer.close(); }  catch (Exception ignored) {}

            // Delete corrupted file
            File corrupted = new File(filePath);
            if (corrupted.exists()) {
                corrupted.delete();
                System.err.println("Deleted corrupted file: " + filePath);
            }

            throw new IOException("Failed to generate PDF for bill #" + data.getBillId(), e);
        }

        return filePath;
    }
    // ================================================================
    //  SECTION: Company Header + Invoice Info
    // ================================================================
    private void addCompanyHeader(Document doc, InvoiceData data) {
        // ── Company name ──
        Paragraph company = new Paragraph(COMPANY_NAME)
                .setFont(fontBold)
                .setFontSize(28)
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(2);
        doc.add(company);

        Paragraph tagline = new Paragraph("Telecommunications")
                .setFont(fontRegular)
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(10);
        doc.add(tagline);

        // ── Invoice details bar ──
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(15);

        infoTable.addCell(createInfoCell("Invoice #",
                String.format("INV-%03d", data.getBillId())));
        infoTable.addCell(createInfoCell("Billing Date",
                data.getBillingDate().format(DATE_FMT)));
        infoTable.addCell(createInfoCell("Billing Period",
                data.getPeriodStart().format(DATE_FMT) + "  →  " +
                data.getPeriodEnd().format(DATE_FMT)));

        doc.add(infoTable);
    }

    private Cell createInfoCell(String label, String value) {
        Paragraph p = new Paragraph()
                .add(new com.itextpdf.layout.element.Text(label + "\n")
                        .setFont(fontRegular).setFontSize(8)
                        .setFontColor(ColorConstants.GRAY))
                .add(new com.itextpdf.layout.element.Text(value)
                        .setFont(fontBold).setFontSize(11)
                        .setFontColor(PRIMARY_COLOR));
        return new Cell().add(p)
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(LIGHT_BG)
                .setPadding(8);
    }

    // ================================================================
    //  SECTION: Customer Info
    // ================================================================
    private void addCustomerInfo(Document doc, InvoiceData data) {
        addSectionTitle(doc, "CUSTOMER INFORMATION");

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(15);

        addDetailRow(table, "Customer", data.getCustomerName());
        addDetailRow(table, "MSISDN",   data.getMsisdn());
        addDetailRow(table, "Address",  data.getCustomerAddress() != null
                ? data.getCustomerAddress() : "—");
        addDetailRow(table, "Rate Plan", data.getPlanName());

        doc.add(table);
    }

    // ================================================================
    //  SECTION: Packages / Services Table
    // ================================================================
    private void addPackagesTable(Document doc, InvoiceData data) {
        if (data.getPackages() == null || data.getPackages().isEmpty()) return;

        addSectionTitle(doc, "SERVICES & PACKAGES");

        Table table = new Table(UnitValue.createPercentArray(
                new float[]{3, 1.5f, 1.5f, 1.5f, 1.5f}))
                .useAllAvailableWidth()
                .setMarginBottom(15);

        // Header row
        addTableHeader(table, "Package");
        addTableHeader(table, "Type");
        addTableHeader(table, "Included");
        addTableHeader(table, "Used");
        addTableHeader(table, "Remaining");

        // Data rows
        boolean alternate = false;
        for (InvoiceData.PackageDetail pkg : data.getPackages()) {
            DeviceRgb rowBg = alternate ? LIGHT_BG : null;

            String label = (pkg.getPriority() == 1 ? "★ " : "") + pkg.getName();
            String included = pkg.getTotalAmount().stripTrailingZeros().toPlainString()
                    + " " + pkg.getUnit();
            String used = pkg.getConsumed().stripTrailingZeros().toPlainString()
                    + " " + pkg.getUnit();
            String remaining = pkg.getRemaining().stripTrailingZeros().toPlainString()
                    + " " + pkg.getUnit();

            addTableCell(table, label, rowBg, TextAlignment.LEFT);
            addTableCell(table, capitalize(pkg.getType()), rowBg, TextAlignment.CENTER);
            addTableCell(table, included, rowBg, TextAlignment.CENTER);
            addTableCell(table, used, rowBg, TextAlignment.CENTER);
            addTableCell(table, remaining, rowBg, TextAlignment.CENTER);

            alternate = !alternate;
        }

        doc.add(table);
    }

    // ================================================================
    //  SECTION: Usage Summary
    // ================================================================
    private void addUsageSummary(Document doc, InvoiceData data) {
        addSectionTitle(doc, "USAGE SUMMARY");

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(15);

        // Convert seconds to minutes for display
        int voiceMinutes = (int) Math.ceil(data.getVoiceUsageSeconds() / 60.0);

        addUsageBox(table, "Voice", voiceMinutes + " minutes");
        addUsageBox(table, "SMS",   data.getSmsUsageCount() + " messages");
        addUsageBox(table, "Data",  data.getDataUsageMB() + " MB");

        doc.add(table);
    }

    private void addUsageBox(Table table, String label, String value) {
        Paragraph p = new Paragraph()
                .add(new com.itextpdf.layout.element.Text(label + "\n")
                        .setFont(fontRegular).setFontSize(9)
                        .setFontColor(ColorConstants.GRAY))
                .add(new com.itextpdf.layout.element.Text(value)
                        .setFont(fontBold).setFontSize(14)
                        .setFontColor(PRIMARY_COLOR));
        table.addCell(new Cell().add(p)
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(LIGHT_BG)
                .setPadding(12)
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f)));
    }

    // ================================================================
    //  SECTION: Charges Breakdown
    // ================================================================
    private void addChargesTable(Document doc, InvoiceData data) {
        addSectionTitle(doc, "CHARGES");

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(15);

        // Monthly fee
        addChargeRow(table, "Monthly Fee (" + data.getPlanName() + ")",
                data.getRecurringFees(), false);

        // Usage charges
        addChargeRow(table, "Usage Charges (Out-of-Bundle)",
                data.getUsageCost(), false);

        // One-time fees (itemized)
        if (data.getOneTimeFeeDetails() != null && !data.getOneTimeFeeDetails().isEmpty()) {
            for (InvoiceData.OneTimeFeeDetail otf : data.getOneTimeFeeDetails()) {
                addChargeRow(table, "   • " + otf.getName(), otf.getPrice(), false);
            }
        }

        // Separator
        table.addCell(new Cell(1, 2)
                .add(new Paragraph(""))
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderTop(new SolidBorder(BORDER_COLOR, 1))
                .setBorderBottom(Border.NO_BORDER)
                .setHeight(5));

        // Subtotal
        addChargeRow(table, "Subtotal", data.getSubtotal(), false);

        // Tax
        addChargeRow(table, "Tax (10%)", data.getTaxes(), false);

        // ── TOTAL (highlighted) ──
        Cell totalLabel = new Cell().add(new Paragraph("TOTAL")
                        .setFont(fontBold).setFontSize(14)
                        .setFontColor(HEADER_TEXT))
                .setBackgroundColor(TOTAL_BG)
                .setPadding(10)
                .setBorder(Border.NO_BORDER);

        Cell totalValue = new Cell().add(new Paragraph(
                        formatMoney(data.getTotalAmount()) + " EGP")
                        .setFont(fontBold).setFontSize(14)
                        .setFontColor(HEADER_TEXT))
                .setBackgroundColor(TOTAL_BG)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(10)
                .setBorder(Border.NO_BORDER);

        table.addCell(totalLabel);
        table.addCell(totalValue);

        doc.add(table);
    }

    private void addChargeRow(Table table, String label, BigDecimal amount,
                              boolean highlight) {
        DeviceRgb bg = highlight ? LIGHT_BG : null;

        Cell labelCell = new Cell().add(new Paragraph(label)
                        .setFont(fontRegular).setFontSize(10))
                .setPadding(6)
                .setBorder(Border.NO_BORDER);

        Cell valueCell = new Cell().add(new Paragraph(formatMoney(amount) + " EGP")
                        .setFont(fontBold).setFontSize(10))
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(6)
                .setBorder(Border.NO_BORDER);

        if (bg != null) {
            labelCell.setBackgroundColor(bg);
            valueCell.setBackgroundColor(bg);
        }

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    // ================================================================
    //  SECTION: Footer
    // ================================================================
    private void addFooter(Document doc) {
        doc.add(new Paragraph("\n"));

        Paragraph footer = new Paragraph("Thank you for choosing " + COMPANY_NAME + "!")
                .setFont(fontRegular)
                .setFontSize(10)
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        doc.add(footer);

        Paragraph disclaimer = new Paragraph(
                "This is a computer-generated invoice. No signature is required.")
                .setFont(fontRegular)
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER);
        doc.add(disclaimer);
    }

    // ================================================================
    //  Helper Methods
    // ================================================================
    private void addSectionTitle(Document doc, String title) {
        Paragraph p = new Paragraph(title)
                .setFont(fontBold)
                .setFontSize(12)
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(5)
                .setBorderBottom(new SolidBorder(PRIMARY_COLOR, 1))
                .setPaddingBottom(3);
        doc.add(p);
    }

    private void addTableHeader(Table table, String text) {
        table.addCell(new Cell()
                .add(new Paragraph(text).setFont(fontBold).setFontSize(9)
                        .setFontColor(HEADER_TEXT))
                .setBackgroundColor(HEADER_BG)
                .setPadding(6)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f)));
    }

    private void addTableCell(Table table, String text, DeviceRgb bg,
                              TextAlignment align) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setFont(fontRegular).setFontSize(9))
                .setPadding(5)
                .setTextAlignment(align)
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f));
        if (bg != null) cell.setBackgroundColor(bg);
        table.addCell(cell);
    }

    private void addDetailRow(Table table, String label, String value) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setFont(fontBold).setFontSize(9)
                        .setFontColor(ColorConstants.GRAY))
                .setBorder(Border.NO_BORDER).setPadding(4));
        table.addCell(new Cell()
                .add(new Paragraph(value).setFont(fontRegular).setFontSize(10))
                .setBorder(Border.NO_BORDER).setPadding(4));
    }

    private String formatMoney(BigDecimal amount) {
        return String.format("%.2f", amount);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}