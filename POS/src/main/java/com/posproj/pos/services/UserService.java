package com.posproj.pos.services;

import com.posproj.pos.db.DatabaseConnection;
import com.posproj.pos.models.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class UserService {

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password: " + e.getMessage());
        }
    }

    public boolean registerUser(String fullName, String password, String role) {
        if (fullName == null || fullName.isEmpty() || password == null || password.isEmpty()) {
            System.out.println("Username or password cannot be empty.");
            return false;
        }

        Connection conn = DatabaseConnection.getConnection();

        String checkQuery = "SELECT id FROM users WHERE full_name = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, fullName);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Username already exists.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking username: " + e.getMessage());
            return false;
        }

        String insertQuery = "INSERT INTO users (full_name, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            insertStmt.setString(1, fullName);
            insertStmt.setString(2, hashPassword(password));
            insertStmt.setString(3, role.toUpperCase());
            insertStmt.executeUpdate();
            System.out.println("User registered successfully!");
            return true;
        } catch (SQLException e) {
            System.out.println("Error during registration: " + e.getMessage());
            return false;
        }
    }

    public User login(String fullName, String password) {
        String hashedPassword = hashPassword(password);
        String query = "SELECT * FROM users WHERE full_name = ? AND password = ?";

        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, fullName);
            stmt.setString(2, hashedPassword);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Login successful for user: " + fullName);
                    return new User(
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getString("password"),
                            rs.getString("role")
                    );
                } else {
                    System.out.println("Invalid credentials.");
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during login: " + e.getMessage());
            return null;
        }
    }

    public boolean updatePassword(User user, String oldPassword, String newPassword) {
        String oldHash = hashPassword(oldPassword);
        String newHash = hashPassword(newPassword);

        String checkSql = "SELECT * FROM users WHERE full_name = ? AND password = ?";
        String updateSql = "UPDATE users SET password = ? WHERE full_name = ?";

        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, user.getFullName());
            checkStmt.setString(2, oldHash);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
            }

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, newHash);
                updateStmt.setString(2, user.getFullName());
                return updateStmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean changeUserRole(String username, String newRole) {
        String sql = "UPDATE users SET role = ? WHERE full_name = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newRole.toUpperCase());
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getTotalUsers() {
        String sql = "SELECT COUNT(*) AS total FROM users";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

}
