package com.mycompany.telecom.billing.dao;

import com.mycompany.telecom.billing.model.ServicePackage;
import com.mycompany.telecom.billing.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ali
 */
public class ServicePackageDAO {

    public List<ServicePackage> findAll() throws SQLException {
        List<ServicePackage> list = new ArrayList<>();
        String sql = "SELECT id, name, type, amount, priority FROM service_package ORDER BY id";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public ServicePackage findById(int id) throws SQLException {
        String sql = "SELECT id, name, type, amount, priority FROM service_package WHERE id = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void insert(ServicePackage sp) throws SQLException {
        String sql = "INSERT INTO service_package (name, type, amount, priority) VALUES (?, ?::service_type, ?, ?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sp.getName());
            ps.setString(2, sp.getType());
            ps.setBigDecimal(3, sp.getAmount());
            ps.setInt(4, sp.getPriority());
            ps.executeUpdate();
        }
    }

    public void update(ServicePackage sp) throws SQLException {
        String sql = "UPDATE service_package SET name=?, type=?::service_type, amount=?, priority=? WHERE id=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sp.getName());
            ps.setString(2, sp.getType());
            ps.setBigDecimal(3, sp.getAmount());
            ps.setInt(4, sp.getPriority());
            ps.setInt(5, sp.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM service_package WHERE id = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private ServicePackage map(ResultSet rs) throws SQLException {
        ServicePackage sp = new ServicePackage();
        sp.setId(rs.getInt("id"));
        sp.setName(rs.getString("name"));
        sp.setType(rs.getString("type"));
        sp.setAmount(rs.getBigDecimal("amount"));
        sp.setPriority(rs.getInt("priority"));
        return sp;
    }
}
