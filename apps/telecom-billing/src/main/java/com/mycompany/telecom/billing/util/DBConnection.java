package com.mycompany.telecom.billing.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author Ali
 */
public class DBConnection {

    private static final String URL;

    static {
        // Load credentials from db.properties (excluded from Git via .gitignore)
        Properties props = new Properties();
        try (InputStream in = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (in == null) {
                throw new RuntimeException(
                        "db.properties not found on classpath. "
                        + "Copy db.properties.example to db.properties and fill in your credentials.");
            }
            props.load(in);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }

        String host = props.getProperty("db.host");
        String name = props.getProperty("db.name");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");
        String sslmode = props.getProperty("db.sslmode", "require");
        String channelBinding = props.getProperty("db.channelBinding", "require");

        URL = "jdbc:postgresql://" + host + "/" + name
                + "?user=" + user
                + "&password=" + password
                + "&sslmode=" + sslmode
                + "&channelBinding=" + channelBinding;

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
