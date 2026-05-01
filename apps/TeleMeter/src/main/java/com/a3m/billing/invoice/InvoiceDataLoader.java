package com.a3m.billing.invoice;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDataLoader {

    private final Connection conn;

    public InvoiceDataLoader(Connection conn) {
        this.conn = conn;
    }

    /**
     * Load all invoice data for a given bill ID.
     * Joins bill → contract → users → rateplan,
     * plus package consumption and one-time fee details.
     */
    public InvoiceData load(int billId) throws SQLException {
        InvoiceData data = new InvoiceData();

        // ── 1. Load bill + customer + plan info ──────────────────
        String billQuery = """
            SELECT
                b.id              AS bill_id,
                b.billing_date,
                b.period_start,
                b.period_end,
                b.usage_cost,
                b.recurring_fees,
                b.one_time_fees,
                b.voice_usage,
                b.data_usage,
                b.sms_usage,
                b.subtotal,
                b.taxes,
                b.total_amount,
                u.name            AS customer_name,
                u.address         AS customer_address,
                ct.msisdn,
                rp.plan_name,
                rp.monthly_fee
            FROM bill b
            JOIN contract ct ON b.contract_id = ct.id
            JOIN users u     ON ct.user_id    = u.id
            JOIN rateplan rp ON ct.rateplan_id = rp.id
            WHERE b.id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(billQuery)) {
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Bill not found: " + billId);
            }

            data.setBillId(rs.getInt("bill_id"));
            data.setBillingDate(rs.getDate("billing_date").toLocalDate());
            data.setPeriodStart(rs.getDate("period_start").toLocalDate());
            data.setPeriodEnd(rs.getDate("period_end").toLocalDate());
            data.setUsageCost(rs.getBigDecimal("usage_cost"));
            data.setRecurringFees(rs.getBigDecimal("recurring_fees"));
            data.setOneTimeFees(rs.getBigDecimal("one_time_fees"));
            data.setVoiceUsageSeconds(rs.getInt("voice_usage"));
            data.setDataUsageMB(rs.getInt("data_usage"));
            data.setSmsUsageCount(rs.getInt("sms_usage"));
            data.setSubtotal(rs.getBigDecimal("subtotal"));
            data.setTaxes(rs.getBigDecimal("taxes"));
            data.setTotalAmount(rs.getBigDecimal("total_amount"));
            data.setCustomerName(rs.getString("customer_name"));
            data.setCustomerAddress(rs.getString("customer_address"));
            data.setMsisdn(rs.getString("msisdn"));
            data.setPlanName(rs.getString("plan_name"));
            data.setMonthlyFee(rs.getBigDecimal("monthly_fee"));
        }

        // ── 2. Load package consumption details ──────────────────
        String pkgQuery = """
            SELECT
                sp.name       AS pkg_name,
                sp.type::TEXT AS pkg_type,
                sp.priority,
                sp.amount     AS total_amount,
                cc.consumption AS consumed,
                GREATEST(sp.amount - cc.consumption, 0) AS remaining
            FROM contract_consumption cc
            JOIN service_package sp ON cc.service_package_id = sp.id
            JOIN bill b             ON b.contract_id = cc.contract_id
            WHERE b.id = ?
              AND cc.starting_date = b.period_start
            ORDER BY sp.type, sp.priority ASC, sp.id ASC
        """;

        List<InvoiceData.PackageDetail> packages = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(pkgQuery)) {
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                packages.add(new InvoiceData.PackageDetail(
                    rs.getString("pkg_name"),
                    rs.getString("pkg_type"),
                    rs.getInt("priority"),
                    rs.getBigDecimal("total_amount"),
                    rs.getBigDecimal("consumed"),
                    rs.getBigDecimal("remaining")
                ));
            }
        }
        data.setPackages(packages);

        // ── 3. Load one-time fee details ─────────────────────────
        String otfQuery = """
            SELECT
                otf.name  AS fee_name,
                otf.price AS fee_price
            FROM contract_one_time cot
            JOIN one_time_fee otf ON cot.fee_id = otf.id
            WHERE cot.bill_id = ?
        """;

        List<InvoiceData.OneTimeFeeDetail> otfDetails = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(otfQuery)) {
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                otfDetails.add(new InvoiceData.OneTimeFeeDetail(
                    rs.getString("fee_name"),
                    rs.getBigDecimal("fee_price")
                ));
            }
        }
        data.setOneTimeFeeDetails(otfDetails);

        return data;
    }

    /**
     * Get all bill IDs that don't have a PDF yet.
     */
    public List<Integer> getUnprocessedBillIds() throws SQLException {
        String query = """
            SELECT b.id
            FROM bill b
            JOIN invoice i ON i.bill_id = b.id
            WHERE i.pdf_path IS NULL
            ORDER BY b.id
        """;

        List<Integer> ids = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        }
        return ids;
    }

    /**
     * Update the invoice table with the generated PDF path.
     */
    public void updatePdfPath(int billId, String pdfPath) throws SQLException {
        String update = "UPDATE invoice SET pdf_path = ? WHERE bill_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setString(1, pdfPath);
            ps.setInt(2, billId);
            ps.executeUpdate();
        }
    }
}