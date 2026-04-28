/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.telecom.billing.dao;

import com.mycompany.telecom.billing.model.Contract;
import com.mycompany.telecom.billing.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ali
 */
public class ContractDAO {

    private static final String SELECT_COLS
            = "c.id, c.user_id, c.rateplan_id, c.msisdn, c.status, "
            + "c.credit_limit, c.available_credit, c.activation_date, c.billing_cycle_day, "
            + "u.name AS user_name, r.plan_name";

    private static final String BASE_JOIN
            = " FROM contract c"
            + " JOIN users u ON u.id = c.user_id"
            + " JOIN rateplan r ON r.id = c.rateplan_id";

    // ── Portal: all contracts for a logged-in user ────────────────────────────
    public List<Contract> findByUserId(String userId) throws SQLException {
        List<Contract> list = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLS + BASE_JOIN
                + " WHERE c.user_id = ? ORDER BY c.id";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    // ── Admin CRUD ────────────────────────────────────────────────────────────
    public List<Contract> findAll() throws SQLException {
        List<Contract> list = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLS + BASE_JOIN + " ORDER BY c.id";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Contract findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + BASE_JOIN + " WHERE c.id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void insert(Contract ct) throws SQLException {
        String sql = "INSERT INTO contract"
                + " (user_id, rateplan_id, msisdn, status, credit_limit, available_credit, activation_date, billing_cycle_day)"
                + " VALUES (?,?,?,?::contract_status,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ct.getUserId());
            ps.setInt(2, ct.getRatePlanId());
            ps.setString(3, ct.getMsisdn());
            ps.setString(4, ct.getStatus());
            ps.setBigDecimal(5, ct.getCreditLimit());
            ps.setBigDecimal(6, ct.getAvailableCredit());
            ps.setDate(7, ct.getActivationDate() != null
                    ? Date.valueOf(ct.getActivationDate())
                    : Date.valueOf(java.time.LocalDate.now()));
            ps.setInt(8, ct.getBillingCycleDay());
            ps.executeUpdate();
        }
    }

    public void update(Contract ct) throws SQLException {
        String sql = "UPDATE contract SET user_id=?, rateplan_id=?, msisdn=?, status=?::contract_status,"
                + " credit_limit=?, available_credit=?, activation_date=?, billing_cycle_day=?"
                + " WHERE id=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ct.getUserId());
            ps.setInt(2, ct.getRatePlanId());
            ps.setString(3, ct.getMsisdn());
            ps.setString(4, ct.getStatus());
            ps.setBigDecimal(5, ct.getCreditLimit());
            ps.setBigDecimal(6, ct.getAvailableCredit());
            ps.setDate(7, ct.getActivationDate() != null
                    ? Date.valueOf(ct.getActivationDate())
                    : Date.valueOf(java.time.LocalDate.now()));
            ps.setInt(8, ct.getBillingCycleDay());
            ps.setInt(9, ct.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM contract WHERE id = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Contract map(ResultSet rs) throws SQLException {
        Contract ct = new Contract();
        ct.setId(rs.getInt("id"));
        ct.setUserId(rs.getString("user_id"));
        ct.setRatePlanId(rs.getInt("rateplan_id"));
        ct.setMsisdn(rs.getString("msisdn"));
        ct.setStatus(rs.getString("status"));
        ct.setCreditLimit(rs.getBigDecimal("credit_limit"));
        ct.setAvailableCredit(rs.getBigDecimal("available_credit"));
        Date ad = rs.getDate("activation_date");
        if (ad != null) {
            ct.setActivationDate(ad.toLocalDate());
        }
        ct.setBillingCycleDay(rs.getInt("billing_cycle_day"));
        ct.setUserName(rs.getString("user_name"));
        ct.setPlanName(rs.getString("plan_name"));
        return ct;
    }
}
