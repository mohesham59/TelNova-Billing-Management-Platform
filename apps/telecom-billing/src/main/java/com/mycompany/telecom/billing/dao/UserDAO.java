package com.mycompany.telecom.billing.dao;

import com.mycompany.telecom.billing.util.DBConnection;
import com.mycompany.telecom.billing.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ali
 */
public class UserDAO {

    // ── Portal authentication ─────────────────────────────────────────────────
    public User findByEmailAndPassword(String email, String password) throws SQLException {
        String sql = "SELECT id, name, address, birthdate, email, password"
                + " FROM users WHERE email = ? AND password = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    // ── Admin CRUD ────────────────────────────────────────────────────────────
    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, name, address, birthdate, email, password FROM users ORDER BY id";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public User findById(String id) throws SQLException {
        String sql = "SELECT id, name, address, birthdate, email, password FROM users WHERE id = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void insert(User u) throws SQLException {
        String sql = "INSERT INTO users (id, name, address, birthdate, email, password)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getId());
            ps.setString(2, u.getName());
            ps.setString(3, u.getAddress());
            ps.setDate(4, u.getBirthdate() != null ? Date.valueOf(u.getBirthdate()) : null);
            ps.setString(5, u.getEmail());
            ps.setString(6, u.getPassword());
            ps.executeUpdate();
        }
    }

    public void update(User u) throws SQLException {
        // If password field was left blank on edit, keep the existing one
        if (u.getPassword() != null && !u.getPassword().isBlank()) {
            String sql = "UPDATE users SET name=?, address=?, birthdate=?, email=?, password=?"
                    + " WHERE id=?";
            try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, u.getName());
                ps.setString(2, u.getAddress());
                ps.setDate(3, u.getBirthdate() != null ? Date.valueOf(u.getBirthdate()) : null);
                ps.setString(4, u.getEmail());
                ps.setString(5, u.getPassword());
                ps.setString(6, u.getId());
                ps.executeUpdate();
            }
        } else {
            String sql = "UPDATE users SET name=?, address=?, birthdate=?, email=? WHERE id=?";
            try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, u.getName());
                ps.setString(2, u.getAddress());
                ps.setDate(3, u.getBirthdate() != null ? Date.valueOf(u.getBirthdate()) : null);
                ps.setString(4, u.getEmail());
                ps.setString(5, u.getId());
                ps.executeUpdate();
            }
        }
    }

    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getString("id"));
        u.setName(rs.getString("name"));
        u.setAddress(rs.getString("address"));
        Date bd = rs.getDate("birthdate");
        if (bd != null) {
            u.setBirthdate(bd.toLocalDate());
        }
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        return u;
    }
}
