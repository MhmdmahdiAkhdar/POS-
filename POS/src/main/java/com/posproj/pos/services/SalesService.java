package com.posproj.pos.services;

import com.posproj.pos.db.DatabaseConnection;
import com.posproj.pos.models.CartItem;
import com.posproj.pos.models.Product;

import java.sql.*;
import java.util.List;

public class SalesService {

    private final ProductService productService = new ProductService();

    public int createSale(double total) {
        String sql = "INSERT INTO sales (total) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setDouble(1, total);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public boolean insertSaleItem(int saleId, int productId, int qty, double price) {
        String sql = "INSERT INTO sales_items (sale_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, saleId);
            stmt.setInt(2, productId);
            stmt.setInt(3, qty);
            stmt.setDouble(4, price);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean checkout(List<CartItem> cart, List<Product> productList) {
        double total = cart.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        int saleId = createSale(total);
        if (saleId <= 0) return false;

        for (CartItem item : cart) {

            Product product = productList.stream()
                    .filter(p -> p.getName().equals(item.getName()))
                    .findFirst().orElse(null);

            if (product == null) continue;

            boolean stockReduced = productService.reduceStock(product.getId(), item.getQuantity());
            if (!stockReduced) return false;

            insertSaleItem(saleId, product.getId(), item.getQuantity(), item.getPrice());
        }

        return true;
    }

    public double getTodaysSales() {
        String sql = "SELECT SUM(total) AS total FROM sales WHERE DATE(created_at) = CURDATE()";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total");
            }else {
                return 0.0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

}