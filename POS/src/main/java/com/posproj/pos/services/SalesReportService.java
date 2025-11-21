package com.posproj.pos.services;

import com.posproj.pos.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SalesReportService {


    public double getDailyRevenue() {
        String sql = "SELECT SUM(total) as total FROM sales WHERE DATE(created_at) = CURRENT_DATE()";
        return getRevenue(sql);
    }


    public double getWeeklyRevenue() {
        String sql = "SELECT SUM(total) as total FROM sales WHERE YEARWEEK(created_at, 1) = YEARWEEK(CURDATE(), 1)";
        return getRevenue(sql);
    }


    public double getMonthlyRevenue() {
        String sql = "SELECT SUM(total) as total FROM sales WHERE YEAR(created_at) = YEAR(CURDATE()) AND MONTH(created_at) = MONTH(CURDATE())";
        return getRevenue(sql);
    }


    public Map<String, Integer> getTopProducts(int limit) {
        String sql = "SELECT p.name, SUM(s.quantity) as sold " +
                "FROM sales_items s " +
                "JOIN products p ON s.product_id = p.id " +
                "GROUP BY p.name " +
                "ORDER BY sold DESC " +
                "LIMIT ?";
        Map<String, Integer> topProducts = new LinkedHashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    topProducts.put(rs.getString("name"), rs.getInt("sold"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return topProducts;
    }


    private double getRevenue(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public Map<String, Double> getRevenueOverTime(int days) {
        Map<String, Double> revenueMap = new LinkedHashMap<>();


        for (int i = days - 1; i >= 0; i--) {
            String day = java.time.LocalDate.now().minusDays(i).toString();
            revenueMap.put(day, 0.0);
        }


        String sql = "SELECT DATE(created_at) as date, SUM(total) as total " +
                "FROM sales " +
                "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                "GROUP BY DATE(created_at) " +
                "ORDER BY DATE(created_at) ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, days);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getDate("date").toString();
                    double total = rs.getDouble("total");
                    revenueMap.put(date, total);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return revenueMap;
    }


}
