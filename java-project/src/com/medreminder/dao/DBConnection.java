package com.medreminder.dao;

import com.medreminder.exception.DatabaseOperationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages MySQL JDBC connections using configuration from config/db.properties.
 * Demonstrates JDBC Connectivity and File Handling (Properties loading).
 */
public final class DBConnection {

    private static final String CONFIG_PATH = "config/db.properties";
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/medicine_reminder_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root";

    private static String url = DEFAULT_URL;
    private static String user = DEFAULT_USER;
    private static String password = DEFAULT_PASSWORD;

    static {
        loadConfig();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Warning: MySQL JDBC Driver not found on classpath. "
                    + "Add mysql-connector-j JAR to lib/ and project build path.");
        }
    }

    private DBConnection() {
        // Utility class: prevent instantiation
    }

    private static void loadConfig() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            props.load(fis);
            url = props.getProperty("db.url", DEFAULT_URL).trim();
            user = props.getProperty("db.user", DEFAULT_USER).trim();
            password = props.getProperty("db.password", DEFAULT_PASSWORD).trim();
        } catch (IOException e) {
            // Fall back to default local credentials if config file is missing
            System.out.println("Notice: Using default MySQL connection settings (" + DEFAULT_URL + ").");
        }
    }

    /**
     * Opens and returns a fresh JDBC Connection to the MySQL database.
     */
    public static Connection getConnection() throws DatabaseOperationException {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Unable to connect to MySQL database. Verify MySQL is running and credentials in config/db.properties are correct.",
                    e);
        }
    }
}
