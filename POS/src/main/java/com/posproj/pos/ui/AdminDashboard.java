package com.posproj.pos.ui;

import com.posproj.pos.models.User;
import com.posproj.pos.services.ProductService;
import com.posproj.pos.services.SalesService;
import com.posproj.pos.services.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class AdminDashboard {

    private final UserService userService = new UserService();
    private final ProductService productService = new ProductService();
    private final SalesService salesService = new SalesService();

    public AdminDashboard(Stage stage, User user) {
        stage.setTitle("POS Dashboard");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #111111, #1C1C1C);");

        VBox mainContent = new VBox(30);
        mainContent.setPadding(new Insets(25));
        mainContent.setAlignment(Pos.TOP_CENTER);


        HBox statBar = new HBox(20);
        statBar.setAlignment(Pos.CENTER);


        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            statBar.getChildren().addAll(
                    createStatCard("Users", String.valueOf(userService.getTotalUsers()), "#FF6F61"),
                    createStatCard("Products", String.valueOf(productService.getTotalProducts()), "#6BCB77"),
                    createStatCard("Today Sales", "$" + String.format("%.2f", salesService.getTodaysSales()), "#FFD93D")
            );
        } else {

            statBar.getChildren().add(createStatCard("Today Sales", "$" + String.format("%.2f", salesService.getTodaysSales()), "#FFD93D"));
        }


        GridPane moduleGrid = new GridPane();
        moduleGrid.setHgap(30);
        moduleGrid.setVgap(30);
        moduleGrid.setAlignment(Pos.CENTER);


        String[][] modules = "ADMIN".equalsIgnoreCase(user.getRole()) ?
                new String[][]{
                        {"Cart", "🛒", "#FF6F61"},
                        {"Products", "📦", "#6BCB77"},
                        {"Sales", "💰", "#FFD93D"},
                        {"Settings", "⚙️", "#FF6F61"}
                } :
                new String[][]{
                        {"Cart", "🛒", "#FF6F61"},
                        {"Settings", "⚙️", "#FF6F61"}
                };


        for (int i = 0; i < modules.length; i++) {
            String name = modules[i][0];
            String icon = modules[i][1];
            String color = modules[i][2];

            Runnable action = switch (name) {
                case "Cart" -> () -> new Cart(stage, user);
                case "Products" -> () -> new ProductPage(stage, user);
                case "Sales" -> () -> new SalesReportPage(stage, user);
                case "Settings" -> () -> new SettingsPage(stage, user);
                default -> () -> {};
            };

            moduleGrid.add(createModuleCard(name, icon, color, action), i % 2, i / 2);
        }


        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            mainContent.getChildren().addAll(statBar, moduleGrid);
        } else {
            mainContent.getChildren().addAll(statBar, moduleGrid);
        }

        root.setCenter(mainContent);

        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.show();
    }

    private StackPane createStatCard(String title, String value, String colorHex) {
        StackPane card = new StackPane();
        card.setPrefSize(180, 80);
        card.setBackground(new Background(new BackgroundFill(Color.web("#1E1E1E"), new CornerRadii(12), Insets.EMPTY)));
        card.setEffect(new DropShadow(12, Color.rgb(0, 0, 0, 0.6)));

        VBox content = new VBox(4);
        content.setAlignment(Pos.CENTER);

        Label t = new Label(title);
        t.setFont(Font.font("Segoe UI", 14));
        t.setTextFill(Color.web("#CCCCCC"));

        Label v = new Label(value);
        v.setFont(Font.font("Segoe UI Semibold", 20));
        v.setTextFill(Color.web(colorHex));

        content.getChildren().addAll(t, v);
        card.getChildren().add(content);
        return card;
    }

    private StackPane createModuleCard(String name, String icon, String colorHex, Runnable action) {
        StackPane card = new StackPane();
        card.setPrefSize(220, 160);
        card.setBackground(new Background(new BackgroundFill(Color.web("#262626"), new CornerRadii(12), Insets.EMPTY)));
        card.setEffect(new DropShadow(16, Color.rgb(0, 0, 0, 0.6)));

        VBox content = new VBox(8);
        content.setAlignment(Pos.CENTER);

        Label ic = new Label(icon);
        ic.setFont(Font.font(48));
        ic.setTextFill(Color.web(colorHex));
        ic.setEffect(new DropShadow(24, Color.rgb(255, 255, 255, 0.25)));

        Label text = new Label(name);
        text.setFont(Font.font("Segoe UI", 20));
        text.setTextFill(Color.web("#FFFFFF"));

        content.getChildren().addAll(ic, text);
        card.getChildren().add(content);


        card.setOnMouseEntered(e -> {
            card.setScaleX(1.08);
            card.setScaleY(1.08);
            card.setStyle("-fx-border-color: " + colorHex + "; -fx-border-width: 2; -fx-border-radius: 12;");
        });

        card.setOnMouseExited(e -> {
            card.setScaleX(1);
            card.setScaleY(1);
            card.setStyle("-fx-border-width: 0;");
        });

        card.setOnMouseClicked(e -> action.run());
        return card;
    }
}
