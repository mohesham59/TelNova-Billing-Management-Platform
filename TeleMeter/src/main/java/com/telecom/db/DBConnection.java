package com.telecom.db;
import java.sql.Connection;
import java.sql.DriverManager;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author mohesham
 */
public class DBConnection {
        private static final String URL = "jdbc:postgresql://ep-lucky-recipe-albakvk0-pooler.c-3.eu-central-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require";    
        private static final String USER = "neondb_owner";
    private static final String PASSWORD = "npg_9bCGRsVIoLF1";

    public static Connection getConnection() {
        Connection con = null;
        try {
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }
}

