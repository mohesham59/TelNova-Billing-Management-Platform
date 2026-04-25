/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
     * @return 
     */
    public List<BillSummary> findRecentByContractId(int contractId) throws SQLException {
        List<BillSummary> list = new ArrayList<>();
        String sql = """
                SELECT id, billing_date, period_start, period_end,
                       recurring_fees, one_time_fees, taxes,
                       subtotal, total_amount,
                       voice_usage, data_usage, sms_usage
                  FROM bill
                 WHERE contract_id = ?
                 ORDER BY billing_date DESC
                 LIMIT 6
                """;
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BillSummary b = new BillSummary();
                    b.setId(rs.getInt("id"));
                    Date bd = rs.getDate("billing_date");
                    if (bd != null) {
                        b.setBillingDate(bd.toLocalDate());
                    }
                    Date ps2 = rs.getDate("period_start");
                    if (ps2 != null) {
                        b.setPeriodStart(ps2.toLocalDate());
                    }
                    Date pe = rs.getDate("period_end");
                    if (pe != null) {
                        b.setPeriodEnd(pe.toLocalDate());
                    }
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
}
