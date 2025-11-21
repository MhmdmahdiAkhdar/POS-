package com.posproj.pos.ui;

import com.posproj.pos.models.User;
import com.posproj.pos.services.SalesReportService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Map;

public class SalesReportPage {

    private final SalesReportService reportService = new SalesReportService();

    public SalesReportPage(Stage stage, User user) {
        stage.setTitle("POS - Sales Dashboard");

        VBox content = new VBox(30);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color:#1B1B1B;");

        HBox header = new HBox(10);
        header.setPadding(new Insets(10, 30, 10, 30));
        header.setAlignment(Pos.CENTER_LEFT);
        Button returnBtn = new Button("← Return");
        styleTopButton(returnBtn);
        returnBtn.setOnAction(e -> new AdminDashboard(stage, user));
        header.getChildren().add(returnBtn);

        Label title = new Label("Sales Dashboard");
        title.setFont(Font.font("Segoe UI", 36));
        title.setTextFill(Color.web("#FFD966"));
        VBox headerBox = new VBox(10, header, title);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().add(headerBox);

        HBox revenueCards = new HBox(30);
        revenueCards.setAlignment(Pos.CENTER);

        double dailyRevenue = reportService.getDailyRevenue();
        double weeklyRevenue = reportService.getWeeklyRevenue();
        double monthlyRevenue = reportService.getMonthlyRevenue();

        revenueCards.getChildren().addAll(
                createRevenueCard("Daily Revenue", dailyRevenue, Color.web("#4CAF50")),
                createRevenueCard("Weekly Revenue", weeklyRevenue, Color.web("#2196F3")),
                createRevenueCard("Monthly Revenue", monthlyRevenue, Color.web("#9C27B0"))
        );

        content.getChildren().add(revenueCards);

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Revenue ($)");
        xAxis.setTickLabelFill(Color.web("#FFECC1"));
        yAxis.setTickLabelFill(Color.web("#FFECC1"));
        xAxis.setTickLabelRotation(45);

        LineChart<String, Number> revenueChart = new LineChart<>(xAxis, yAxis);
        revenueChart.setTitle("Revenue Last 7 Days");
        revenueChart.setLegendVisible(false);
        revenueChart.setAnimated(false);
        revenueChart.setHorizontalGridLinesVisible(false);
        revenueChart.setVerticalGridLinesVisible(false);
        revenueChart.setStyle("-fx-background-color: #2A2A2A; -fx-border-color: transparent;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Double> last7DaysRevenue = reportService.getRevenueOverTime(7);
        last7DaysRevenue.forEach((date, total) -> series.getData().add(new XYChart.Data<>(date, total)));
        revenueChart.getData().add(series);

        VBox chartBox = new VBox(revenueChart);
        chartBox.setMinHeight(500);
        chartBox.setPrefHeight(500);
        chartBox.setMaxHeight(500);
        chartBox.setAlignment(Pos.CENTER);
        content.getChildren().add(chartBox);

        Label topProductsTitle = new Label("Top Products");
        topProductsTitle.setFont(Font.font("Segoe UI", 22));
        topProductsTitle.setTextFill(Color.web("#FFECC1"));

        TilePane productsPane = new TilePane();
        productsPane.setPadding(new Insets(10));
        productsPane.setHgap(20);
        productsPane.setVgap(20);
        productsPane.setPrefColumns(3);
        productsPane.setTileAlignment(Pos.TOP_LEFT);

        Map<String, Integer> topProducts = reportService.getTopProducts(50);
        int count = 0;
        for (Map.Entry<String, Integer> entry : topProducts.entrySet()) {
            if (count >= 20) break;
            productsPane.getChildren().add(createProductCard(entry.getKey(), entry.getValue()));
            count++;
        }

        VBox productsBox = new VBox(10, topProductsTitle, productsPane);
        productsBox.setPadding(new Insets(10));
        productsBox.setStyle("-fx-background-color: #1F1F1F; -fx-background-radius: 12;");
        productsBox.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));
        content.getChildren().add(productsBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: transparent;" +
                        "-fx-padding: 0;" +
                        "-fx-border-color: transparent;"
        );

        Platform.runLater(() -> scrollPane.lookupAll(".scroll-bar")
                .forEach(sb -> sb.setStyle("-fx-pref-width:0; -fx-pref-height:0; -fx-opacity:0;")));

        Scene scene = new Scene(scrollPane,
                Screen.getPrimary().getBounds().getWidth() * 0.8,
                Screen.getPrimary().getBounds().getHeight() * 0.8);

        stage.setScene(scene);
        stage.show();
    }

    private VBox createRevenueCard(String labelText, double value, Color baseColor) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", 22));
        label.setTextFill(Color.web("#FFECC1"));

        Label totalLabel = new Label("$" + String.format("%.2f", value));
        totalLabel.setFont(Font.font("Segoe UI", 42));
        totalLabel.setTextFill(Color.WHITE);

        VBox card = new VBox(15, label, totalLabel);
        card.setPadding(new Insets(40));
        card.setAlignment(Pos.CENTER);
        card.setMinWidth(360);
        card.setMinHeight(220);
        card.setStyle("-fx-background-radius:20; -fx-background-color: linear-gradient(to bottom right, "
                + toRgbString(baseColor.brighter()) + ", " + toRgbString(baseColor) + ");");
        card.setEffect(new DropShadow(12, Color.rgb(0, 0, 0, 0.4)));

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-radius:20; -fx-background-color: linear-gradient(to bottom right, "
                + toRgbString(baseColor) + ", " + toRgbString(baseColor.darker()) + ");"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-radius:20; -fx-background-color: linear-gradient(to bottom right, "
                + toRgbString(baseColor.brighter()) + ", " + toRgbString(baseColor) + ");"));

        return card;
    }

    private VBox createProductCard(String productName, int soldCount) {
        Label name = new Label(productName);
        name.setFont(Font.font("Segoe UI", 18));
        name.setTextFill(Color.web("#FFECC1"));

        Label sold = new Label(soldCount + " sold");
        sold.setFont(Font.font(16));
        sold.setTextFill(Color.web("#FFD966"));

        VBox card = new VBox(10, name, sold);
        card.setPadding(new Insets(16));
        card.setAlignment(Pos.CENTER);
        card.setMinWidth(220);
        card.setStyle("-fx-background-color:#2A2A2A; -fx-background-radius:16;");
        card.setEffect(new DropShadow(6, Color.rgb(0, 0, 0, 0.35)));

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color:#333333; -fx-background-radius:16;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color:#2A2A2A; -fx-background-radius:16;"));

        return card;
    }

    private String toRgbString(Color c) {
        return "rgb(" + (int) (c.getRed() * 255) + "," + (int) (c.getGreen() * 255) + "," + (int) (c.getBlue() * 255) + ")";
    }

    private void styleTopButton(Button btn) {
        btn.setFont(Font.font("Segoe UI", 16));
        btn.setStyle(
                "-fx-background-color: #FFD700;" +
                        "-fx-text-fill: #1C1C1C;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 6 12 6 12;"
        );
        btn.setEffect(new DropShadow(6, Color.rgb(0,0,0,0.4)));
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #FFEA70;" +
                        "-fx-text-fill: #1C1C1C;" +
                        "-fx-background-radius: 12;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #FFD700;" +
                        "-fx-text-fill: #1C1C1C;" +
                        "-fx-background-radius: 12;"
        ));
    }
}
