/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.telecom.billing.dao;

import com.mycompany.telecom.billing.model.RatePlan;
import com.mycompany.telecom.billing.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ali
 */
public class RatePlanDAO {

    public List<RatePlan> findAll() throws SQLException {
        List<RatePlan> list = new ArrayList<>();
        String sql = "SELECT id, plan_name, ror_data, ror_voice, ror_sms, monthly_fee FROM rateplan ORDER BY id";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
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

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM rateplan WHERE id = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
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
