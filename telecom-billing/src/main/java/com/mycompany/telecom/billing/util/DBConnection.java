/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.telecom.billing.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author Ali
 */
public class DBConnection {

    // Neon — all options (ssl, channelBinding) must live in the URL itself
    private static final String URL =
        "jdbc:postgresql://ep-lucky-recipe-albakvk0-pooler.c-3.eu-central-1.aws.neon.tech/neondb" +
        "?user=neondb_owner" +
        "&password=npg_9bCGRsVIoLF1" +
        "&sslmode=require" +
        "&channelBinding=require";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
