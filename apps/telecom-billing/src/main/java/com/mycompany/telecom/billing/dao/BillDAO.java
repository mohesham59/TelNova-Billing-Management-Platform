package com.mycompany.telecom.billing.dao;

import com.mycompany.telecom.billing.model.BillSummary;
import com.mycompany.telecom.billing.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ali
 */
public class BillDAO {

    /**
     * Returns the 6 most recent bills for the given contract, newest first.
     *
     * @return
     */
    public List<BillSummary> findRecentByContractId(int contractId) throws SQLException {
        List<BillSummary> list = new ArrayList<>();
        String sql = "SELECT id,billing_date,period_start,period_end,recurring_fees,one_time_fees,taxes,subtotal,total_amount,voice_usage,data_usage,sms_usage FROM bill WHERE contract_id=? ORDER BY billing_date DESC LIMIT 6";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BillSummary b = new BillSummary();
                    b.setId(rs.getInt("id"));
                    Date bd = rs.getDate("billing_date"); if (bd != null) b.setBillingDate(bd.toLocalDate());
                    Date ps2 = rs.getDate("period_start"); if (ps2 != null) b.setPeriodStart(ps2.toLocalDate());
                    Date pe = rs.getDate("period_end"); if (pe != null) b.setPeriodEnd(pe.toLocalDate());
                    b.setRecurringFees(rs.getBigDecimal("recurring_fees"));
                    b.setOneTimeFees(rs.getBigDecimal("one_time_fees"));
                    b.setTaxes(rs.getBigDecimal("taxes"));
                    b.setSubtotal(rs.getBigDecimal("subtotal"));
                    b.setTotalAmount(rs.getBigDecimal("total_amount"));
                    b.setVoiceUsage(rs.getInt("voice_usage"));
                    b.setDataUsage(rs.getInt("data_usage"));
                    b.setSmsUsage(rs.getInt("sms_usage"));
                    list.add(b);
                }
            }
        }
        return list;
    }
 
    /** All bills for a user (across all their contracts) — for the invoices page */
    public List<BillSummary> findAllByUserId(String userId) throws SQLException {
        List<BillSummary> list = new ArrayList<>();
        String sql = "SELECT b.id,b.billing_date,b.period_start,b.period_end," +
                     "b.recurring_fees,b.one_time_fees,b.taxes,b.subtotal,b.total_amount," +
                     "b.voice_usage,b.data_usage,b.sms_usage " +
                     "FROM bill b JOIN contract c ON c.id=b.contract_id " +
                     "WHERE c.user_id=? ORDER BY b.billing_date DESC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BillSummary b = new BillSummary();
                    b.setId(rs.getInt("id"));
                    Date bd = rs.getDate("billing_date"); if (bd != null) b.setBillingDate(bd.toLocalDate());
                    Date ps2 = rs.getDate("period_start"); if (ps2 != null) b.setPeriodStart(ps2.toLocalDate());
                    Date pe = rs.getDate("period_end"); if (pe != null) b.setPeriodEnd(pe.toLocalDate());
                    b.setRecurringFees(rs.getBigDecimal("recurring_fees"));
                    b.setOneTimeFees(rs.getBigDecimal("one_time_fees"));
                    b.setTaxes(rs.getBigDecimal("taxes"));
                    b.setSubtotal(rs.getBigDecimal("subtotal"));
                    b.setTotalAmount(rs.getBigDecimal("total_amount"));
                    b.setVoiceUsage(rs.getInt("voice_usage"));
                    b.setDataUsage(rs.getInt("data_usage"));
                    b.setSmsUsage(rs.getInt("sms_usage"));
                    list.add(b);
                }
            }
        }
        return list;
    }
 
    /** Single bill by ID (with ownership check via userId) */
    public BillSummary findByIdAndUserId(int billId, String userId) throws SQLException {
        String sql = "SELECT b.id,b.billing_date,b.period_start,b.period_end," +
                     "b.recurring_fees,b.one_time_fees,b.taxes,b.subtotal,b.total_amount," +
                     "b.voice_usage,b.data_usage,b.sms_usage " +
                     "FROM bill b JOIN contract c ON c.id=b.contract_id " +
                     "WHERE b.id=? AND c.user_id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId); ps.setString(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                BillSummary b = new BillSummary();
                b.setId(rs.getInt("id"));
                Date bd = rs.getDate("billing_date"); if (bd != null) b.setBillingDate(bd.toLocalDate());
                Date ps2 = rs.getDate("period_start"); if (ps2 != null) b.setPeriodStart(ps2.toLocalDate());
                Date pe = rs.getDate("period_end"); if (pe != null) b.setPeriodEnd(pe.toLocalDate());
                b.setRecurringFees(rs.getBigDecimal("recurring_fees"));
                b.setOneTimeFees(rs.getBigDecimal("one_time_fees"));
                b.setTaxes(rs.getBigDecimal("taxes"));
                b.setSubtotal(rs.getBigDecimal("subtotal"));
                b.setTotalAmount(rs.getBigDecimal("total_amount"));
                b.setVoiceUsage(rs.getInt("voice_usage"));
                b.setDataUsage(rs.getInt("data_usage"));
                b.setSmsUsage(rs.getInt("sms_usage"));
                return b;
            }
        }
    }
}
