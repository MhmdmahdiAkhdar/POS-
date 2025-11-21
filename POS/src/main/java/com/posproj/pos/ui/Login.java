package com.posproj.pos.ui;

import com.posproj.pos.models.User;
import com.posproj.pos.services.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Login {

    private final UserService userService = new UserService();

    public Login(Stage stage) {
        stage.setTitle("Login | POS System");

        Label title = new Label("Login");
        title.setStyle("""
                -fx-font-size: 36px;
                -fx-font-weight: bold;
                -fx-text-fill: #FFCC66;
                """);

        double btnWidth = 260;
        double inputWidth = 290;


        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(inputWidth);
        usernameField.setStyle(getInputStyle());
        addFocusEffect(usernameField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(inputWidth);
        passwordField.setStyle(getInputStyle());
        addFocusEffect(passwordField);


        Label msg = new Label();
        msg.setStyle("-fx-text-fill: #FFCC66; -fx-font-size: 15px;");


        Button loginBtn = new Button("Login");
        loginBtn.setPrefWidth(btnWidth);
        loginBtn.setPrefHeight(50);
        loginBtn.setStyle(getLoginBtnStyle());
        addHoverEffect(loginBtn, getLoginBtnStyle());

        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                msg.setText("⚠ Please fill all fields!");
                return;
            }

            User user = userService.login(username, password);
            if (user != null) {
                msg.setTextFill(Color.LIGHTGREEN);
                msg.setText("Login successful!");
                usernameField.clear();
                passwordField.clear();

                    new AdminDashboard(stage, user);

            } else {
                msg.setTextFill(Color.RED);
                msg.setText("Invalid username or password!");
            }
        });

        Button signupBtn = new Button("Create Account");
        signupBtn.setPrefWidth(btnWidth);
        signupBtn.setPrefHeight(45);
        signupBtn.setStyle(getSignupBtnStyle());
        addHoverEffect(signupBtn, getSignupBtnStyle());
        signupBtn.setOnAction(e -> new Signup(stage));

        VBox card = new VBox(22, title, usernameField, passwordField, loginBtn, signupBtn, msg);
        card.setPadding(new Insets(50));
        card.setAlignment(Pos.CENTER);
        card.setFillWidth(false);
        card.setStyle("""
                -fx-background-color: #2F2F2F; /* dark gray */
                -fx-background-radius: 28;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 30, 0, 0, 6);
                """);

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(60));
        root.setStyle("""
                -fx-background-color: linear-gradient(to bottom right, #1F1F1F, #2F2F2F);
                """);

        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        Scene scene = new Scene(root, screenWidth * 0.8, screenHeight * 0.8);
        stage.setScene(scene);
        stage.show();
    }

    private String getInputStyle() {
        return """
                -fx-background-color: #3A3A3A; /* slightly lighter dark gray */
                -fx-text-fill: #FFCC66; /* yellow text */
                -fx-prompt-text-fill: rgba(255,204,102,0.7);
                -fx-padding: 16 14;
                -fx-background-radius: 14;
                -fx-border-color: transparent;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 2);
                -fx-font-size: 15px;
                -fx-transition: all 0.3s ease;
                """;
    }

    private void addFocusEffect(TextField field) {
        field.focusedProperty().addListener((obs, oldV, newV) -> {
            field.setStyle(getInputStyle() + (newV
                    ? "-fx-effect: dropshadow(gaussian, rgba(255,204,102,0.45), 10, 0, 0, 2);"
                    : "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 2);"));
        });
    }

    private String getLoginBtnStyle() {
        return """
                -fx-background-color: #FFCC66; /* yellow */
                -fx-text-fill: #2F2F2F; /* dark gray text */
                -fx-font-weight: bold;
                -fx-background-radius: 16;
                -fx-font-size: 18px;
                -fx-cursor: hand;
                -fx-transition: all 0.2s ease;
                """;
    }

    private String getSignupBtnStyle() {
        return """
                -fx-background-color: #3A3A3A; /* dark gray */
                -fx-text-fill: #FFCC66; /* yellow text */
                -fx-font-size: 16px;
                -fx-background-radius: 16;
                -fx-cursor: hand;
                -fx-transition: all 0.2s ease;
                """;
    }

    private void addHoverEffect(Button btn, String baseStyle) {
        btn.setOnMouseEntered(e -> btn.setStyle(baseStyle + "-fx-scale-x:1.05; -fx-scale-y:1.05;"));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
    }
}
