package com.posproj.pos.ui;

import com.posproj.pos.audio.MusicPlayer;
import com.posproj.pos.models.User;
import com.posproj.pos.services.UserService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class SettingsPage {

    private final UserService userService = new UserService();

    public SettingsPage(Stage stage, User user) {

        MusicPlayer.playLoop();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #1F1F1F, #2F2F2F);");

        VBox container = new VBox(30);
        container.setPadding(new Insets(30));
        container.setAlignment(Pos.TOP_CENTER);

        Button returnBtn = createStyledButton("Return");
        returnBtn.setOnAction(e -> new AdminDashboard(stage, user));

        Label title = new Label("⚙ Settings");
        title.setFont(Font.font("Segoe UI", 36));
        title.setTextFill(Color.web("#FFCC66"));

        HBox topBar = new HBox(20, returnBtn, title);
        topBar.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(topBar);

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            container.getChildren().add(createAdminRoleManager());
        }

        VBox passwordBox = createPasswordChanger(user);
        container.getChildren().add(passwordBox);

        Button muteBtn = createStyledButton(MusicPlayer.isMuted() ? "🔇 Mute" : "🔊 Sound");
        muteBtn.setOnAction(e -> {
            MusicPlayer.toggleMute();
            muteBtn.setText(MusicPlayer.isMuted() ? "🔇 Mute" : "🔊 Sound");
        });
        container.getChildren().add(muteBtn);
        VBox.setMargin(muteBtn, new Insets(10, 0, 0, 0));

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("""
                -fx-background: transparent;
                -fx-background-color: transparent;
                -fx-border-color: transparent;
                """);
        Platform.runLater(() ->
                scrollPane.lookupAll(".scroll-bar").forEach(sb -> sb.setOpacity(0))
        );
        root.setCenter(scrollPane);

        HBox logoutBox = new HBox();
        logoutBox.setAlignment(Pos.CENTER);
        logoutBox.setPadding(new Insets(10, 0, 10, 0));
        Button logout = createStyledButton("Logout");
        logout.setOnAction(e -> new Login(stage));
        logoutBox.getChildren().add(logout);
        root.setBottom(logoutBox);

        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        Scene scene = new Scene(root, screenWidth * 0.8, screenHeight * 0.8);

        stage.setScene(scene);
        stage.show();
    }

    private VBox createCard() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setPrefWidth(500);
        box.setAlignment(Pos.TOP_CENTER);
        box.setBackground(new Background(new BackgroundFill(
                Color.web("#2F2F2F"),
                new CornerRadii(18),
                Insets.EMPTY
        )));
        box.setEffect(new DropShadow(15, Color.rgb(0, 0, 0, 0.5)));
        return box;
    }

    private VBox createAdminRoleManager() {
        VBox box = createCard();

        Label header = new Label("Change User Role");
        header.setFont(Font.font("Segoe UI Semibold", 22));
        header.setTextFill(Color.web("#FFCC66"));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setPrefHeight(35);
        usernameField.setStyle("-fx-background-radius: 10; -fx-background-color: #3A3A3A; -fx-text-fill: #FFCC66;");

        ComboBox<String> roles = new ComboBox<>();
        roles.getItems().addAll("ADMIN", "USER");
        roles.setPromptText("Select new role");
        roles.setPrefHeight(35);
        roles.setStyle("-fx-background-color: #FFCC66; -fx-background-radius: 10; -fx-text-fill: #2F2F2F; -fx-font-weight: bold;");

        Label msg = new Label();
        msg.setStyle("-fx-text-fill: #FFCC66;");

        Button apply = createStyledButton("Apply Role");
        apply.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String newRole = roles.getValue();
            if (username.isEmpty() || newRole == null) {
                msg.setText("Please fill all fields!");
                return;
            }
            boolean success = userService.changeUserRole(username, newRole);
            msg.setText(success ? "Role updated!" : "Failed to update role.");
        });

        box.getChildren().addAll(header, usernameField, roles, apply, msg);
        return box;
    }

    private VBox createPasswordChanger(User user) {
        VBox box = createCard();

        Label header = new Label("Change Password");
        header.setFont(Font.font("Segoe UI Semibold", 22));
        header.setTextFill(Color.web("#FFCC66"));

        PasswordField oldPass = new PasswordField();
        oldPass.setPromptText("Current password");
        oldPass.setPrefHeight(35);
        oldPass.setStyle("-fx-background-radius: 10; -fx-background-color: #3A3A3A; -fx-text-fill: #FFCC66;");

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New password");
        newPass.setPrefHeight(35);
        newPass.setStyle("-fx-background-radius: 10; -fx-background-color: #3A3A3A; -fx-text-fill: #FFCC66;");

        Label msg = new Label();
        msg.setStyle("-fx-text-fill: #FFCC66;");

        Button change = createStyledButton("Update Password");
        change.setOnAction(e -> {
            String oldPassword = oldPass.getText();
            String newPassword = newPass.getText();

            if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                msg.setText("Fill all fields.");
                return;
            }
            if (oldPassword.equals(newPassword)) {
                msg.setText("Passwords cannot match.");
                return;
            }

            boolean updated = userService.updatePassword(user, oldPassword, newPassword);

            if (updated) {
                msg.setStyle("-fx-text-fill: lightgreen;");
                msg.setText("Password updated!");
                oldPass.clear();
                newPass.clear();
            } else {
                msg.setStyle("-fx-text-fill: red;");
                msg.setText("Password update failed.");
            }
        });

        box.getChildren().addAll(header, oldPass, newPass, change, msg);
        return box;
    }

    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setPrefSize(160, 40);
        btn.setFont(Font.font("Segoe UI", 16));
        btn.setTextFill(Color.web("#2F2F2F"));
        btn.setStyle("""
                -fx-background-color: linear-gradient(to right, #FFCC66, #FFD483);
                -fx-background-radius: 14;
                -fx-font-weight: bold;
                -fx-cursor: hand;
                """);
        btn.setEffect(new DropShadow(6, Color.rgb(0,0,0,0.4)));

        btn.setOnMouseEntered(e -> btn.setStyle("""
                -fx-background-color: linear-gradient(to right, #FFE28A, #FFD966);
                -fx-background-radius: 14;
                -fx-font-weight: bold;
                -fx-cursor: hand;
                """));
        btn.setOnMouseExited(e -> btn.setStyle("""
                -fx-background-color: linear-gradient(to right, #FFCC66, #FFD483);
                -fx-background-radius: 14;
                -fx-font-weight: bold;
                -fx-cursor: hand;
                """));
        return btn;
    }
}
