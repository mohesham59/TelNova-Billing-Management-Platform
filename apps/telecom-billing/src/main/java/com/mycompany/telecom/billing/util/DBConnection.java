package com.mycompany.telecom.billing.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static String getEnv(String key, String fallback) {
        String val = System.getenv(key);
        return (val != null && !val.isEmpty()) ? val : fallback;
    }

    public static Connection getConnection() {
        String host     = getEnv("DB_HOST",     "localhost");
        String port     = getEnv("DB_PORT",     "5432");
        String dbName   = getEnv("DB_NAME",     "telecom_billing");
        String user     = getEnv("DB_USER",     "telecom");
        String password = getEnv("DB_PASSWORD", "telecom_pass");

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;

        Connection con = null;
        try {
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection(url, user, password);
            System.out.println("DB Connected Successfully to: " + url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }
}