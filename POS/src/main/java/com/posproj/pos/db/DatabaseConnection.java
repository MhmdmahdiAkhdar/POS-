package com.posproj.pos.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/pos";
    private static final String USER = "root";
    private static final String PASSWORD = "123";

    private static Connection connection;

    public static void open() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connected.");
            }
        } catch (Exception e) {
            System.err.println("Failed to open DB: " + e.getMessage());
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                open();
            }
        } catch (SQLException e) {
            System.err.println("Failed to get DB connection: " + e.getMessage());
        }
        return connection;
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database closed.");
            }
        } catch (Exception e) {
            System.err.println("Failed to close DB: " + e.getMessage());
        }
    }
}
