package com.posproj.pos.ui;

import com.posproj.pos.services.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Signup {

    private final UserService userService = new UserService();

    public Signup(Stage stage) {
        stage.setTitle("Create Account | POS System");

        Label title = new Label("Create Account");
        title.setStyle("""
                -fx-font-size: 36px;
                -fx-font-weight: bold;
                -fx-text-fill: #FFCC66;
                """);

        double btnWidth = 260;
        double inputWidth = 290;
        double btnFontSize = 18;
        double inputFontSize = 15;

        String inputStyle = String.format("""
                -fx-background-color: #3A3A3A; /* dark gray input */
                -fx-text-fill: #FFCC66; /* yellow text */
                -fx-prompt-text-fill: rgba(255,204,102,0.7);
                -fx-padding: 16 14;
                -fx-background-radius: 14;
                -fx-border-color: transparent;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 2);
                -fx-font-size: %.0fpx;
                -fx-transition: all 0.3s ease;
                """, inputFontSize);

        TextField username = new TextField();
        username.setPromptText("Choose a username");
        username.setPrefWidth(inputWidth);
        username.setStyle(inputStyle);
        addFocusEffect(username, inputStyle);

        PasswordField password = new PasswordField();
        password.setPromptText("Choose a password");
        password.setPrefWidth(inputWidth);
        password.setStyle(inputStyle);
        addFocusEffect(password, inputStyle);

        Label msg = new Label();
        msg.setStyle("-fx-text-fill: #FFCC66; -fx-font-size: 15px;");

        String btnStyle = String.format("""
                -fx-background-color: #FFCC66; /* yellow button */
                -fx-text-fill: #2F2F2F; /* dark gray text */
                -fx-font-size: %.0fpx;
                -fx-font-weight: bold;
                -fx-background-radius: 16;
                -fx-cursor: hand;
                -fx-transition: all 0.2s ease;
                """, btnFontSize);

        String btnHoverStyle = btnStyle + "-fx-scale-x:1.05; -fx-scale-y:1.05;";

        Button signupBtn = new Button("Create Account");
        signupBtn.setPrefWidth(btnWidth);
        signupBtn.setPrefHeight(50);
        signupBtn.setStyle(btnStyle);
        signupBtn.setOnMouseEntered(e -> signupBtn.setStyle(btnHoverStyle));
        signupBtn.setOnMouseExited(e -> signupBtn.setStyle(btnStyle));

        signupBtn.setOnAction(e -> {
            String user = username.getText().trim();
            String pass = password.getText().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                msg.setStyle("-fx-text-fill: #FFCC66;");
                msg.setText("⚠ All fields are required.");
                return;
            }

            boolean success = userService.registerUser(user, pass, "user");
            if (success) {
                username.clear();
                password.clear();
                msg.setStyle("-fx-text-fill: lightgreen;");
                msg.setText("Account created successfully!");
            } else {
                msg.setStyle("-fx-text-fill: red;");
                msg.setText("Username already exists or DB error.");
            }
        });

        Button loginBtn = new Button("Back to Login");
        loginBtn.setPrefWidth(btnWidth);
        loginBtn.setPrefHeight(45);
        String loginStyle = """
                -fx-background-color: #2F2F2F; /* dark gray */
                -fx-text-fill: #FFCC66; /* yellow text */
                -fx-font-size: 16px;
                -fx-background-radius: 16;
                -fx-cursor: hand;
                -fx-transition: all 0.2s ease;
                """;
        String loginHoverStyle = loginStyle + "-fx-scale-x:1.03; -fx-scale-y:1.03;";
        loginBtn.setStyle(loginStyle);
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(loginHoverStyle));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(loginStyle));
        loginBtn.setOnAction(e -> new Login(stage));

        VBox card = new VBox(22, title, username, password, signupBtn, loginBtn, msg);
        card.setPadding(new Insets(50));
        card.setAlignment(Pos.CENTER);
        card.setFillWidth(false);
        card.setStyle("""
                -fx-background-color: #2F2F2F; /* dark gray card */
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

    private void addFocusEffect(TextField field, String baseStyle) {
        field.focusedProperty().addListener((obs, oldV, newV) -> {
            field.setStyle(baseStyle + (newV
                    ? "-fx-effect: dropshadow(gaussian, rgba(255,204,102,0.45), 10, 0, 0, 2);"
                    : "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 2);"));
        });
    }
}
