/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, name, address, birthdate FROM users ORDER BY id";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public User findById(String id) throws SQLException {
        String sql = "SELECT id, name, address, birthdate FROM users WHERE id = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void insert(User u) throws SQLException {
        String sql = "INSERT INTO users (id, name, address, birthdate) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getId());
            ps.setString(2, u.getName());
            ps.setString(3, u.getAddress());
            ps.setDate(4, u.getBirthdate() != null ? Date.valueOf(u.getBirthdate()) : null);
            ps.executeUpdate();
        }
    }

    public void update(User u) throws SQLException {
        String sql = "UPDATE users SET name=?, address=?, birthdate=? WHERE id=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getAddress());
            ps.setDate(3, u.getBirthdate() != null ? Date.valueOf(u.getBirthdate()) : null);
            ps.setString(4, u.getId());
            ps.executeUpdate();
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
        return u;
    }
}
