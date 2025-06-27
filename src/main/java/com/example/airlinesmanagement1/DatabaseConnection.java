package com.example.airlinesmanagement1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Replace with your actual database name
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/airlines_management1";
    private static final String DATABASE_USERNAME = "root";  // Default for XAMPP
    private static final String DATABASE_PASSWORD = "";      // Default for XAMPP (empty)

    public Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Failed to connect to DB: " + e.getMessage());
        }
        return null;
    }
}
