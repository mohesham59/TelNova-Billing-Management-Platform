package com.mycompany.telecom.billing.dao;

import com.mycompany.telecom.billing.model.RatePlan;
import com.mycompany.telecom.billing.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RatePlanDAO {

    public List<RatePlan> findAll() throws SQLException {
        List<RatePlan> list = new ArrayList<>();
        String sql = "SELECT id, plan_name, ror_data, ror_voice, ror_sms, monthly_fee FROM rateplan ORDER BY id";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public RatePlan findById(int id) throws SQLException {
        String sql = "SELECT id, plan_name, ror_data, ror_voice, ror_sms, monthly_fee FROM rateplan WHERE id = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void insert(RatePlan rp) throws SQLException {
        String sql = "INSERT INTO rateplan (plan_name, ror_data, ror_voice, ror_sms, monthly_fee) VALUES (?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, rp.getPlanName());
            ps.setBigDecimal(2, rp.getRorData());
            ps.setBigDecimal(3, rp.getRorVoice());
            ps.setBigDecimal(4, rp.getRorSms());
            ps.setBigDecimal(5, rp.getMonthlyFee());
            ps.executeUpdate();
        }
    }

    public void update(RatePlan rp) throws SQLException {
        String sql = "UPDATE rateplan SET plan_name=?, ror_data=?, ror_voice=?, ror_sms=?, monthly_fee=? WHERE id=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, rp.getPlanName());
            ps.setBigDecimal(2, rp.getRorData());
            ps.setBigDecimal(3, rp.getRorVoice());
            ps.setBigDecimal(4, rp.getRorSms());
            ps.setBigDecimal(5, rp.getMonthlyFee());
            ps.setInt(6, rp.getId());
            ps.executeUpdate();
        }
    }

    // ── CASCADE DELETE: يمسح الـ rateplan وكل الـ contracts المرتبطة ────────
    public void delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                // 1. امسح الـ invoices المرتبطة بالـ contracts بتاعت الـ rateplan
                try (PreparedStatement ps = c.prepareStatement("""
                        DELETE FROM invoice WHERE bill_id IN (
                            SELECT b.id FROM bill b
                            JOIN contract ct ON b.contract_id = ct.id
                            WHERE ct.rateplan_id = ?
                        )""")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                // 2. امسح الـ bills
                try (PreparedStatement ps = c.prepareStatement("""
                        DELETE FROM bill WHERE contract_id IN (
                            SELECT id FROM contract WHERE rateplan_id = ?
                        )""")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                // 3. امسح الـ contract_consumption
                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM contract_consumption WHERE rateplan_id = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                // 4. امسح الـ ror_contract
                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM ror_contract WHERE rateplan_id = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                // 5. امسح الـ contract_one_time
                try (PreparedStatement ps = c.prepareStatement("""
                        DELETE FROM contract_one_time WHERE contract_id IN (
                            SELECT id FROM contract WHERE rateplan_id = ?
                        )""")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                // 6. امسح الـ contracts
                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM contract WHERE rateplan_id = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                // 7. امسح الـ rateplan_packages
                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM rateplan_packages WHERE rateplan_id = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                // 8. امسح الـ rateplan نفسه
                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM rateplan WHERE id = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private RatePlan map(ResultSet rs) throws SQLException {
        RatePlan rp = new RatePlan();
        rp.setId(rs.getInt("id"));
        rp.setPlanName(rs.getString("plan_name"));
        rp.setRorData(rs.getBigDecimal("ror_data"));
        rp.setRorVoice(rs.getBigDecimal("ror_voice"));
        rp.setRorSms(rs.getBigDecimal("ror_sms"));
        rp.setMonthlyFee(rs.getBigDecimal("monthly_fee"));
        return rp;
    }
}