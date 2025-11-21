package com.posproj.pos.ui;

import com.posproj.pos.models.Product;
import com.posproj.pos.models.User;
import com.posproj.pos.services.ProductService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ProductPage {

    private final ProductService productService = new ProductService();
    private final FlowPane cardPane = new FlowPane();

    public ProductPage(Stage stage, User user) {
        stage.setTitle("Product Management");


        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2C2C2C;");


        HBox header = new HBox(10);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #2C2C2C;");
        header.setEffect(new DropShadow(10, Color.BLACK));

        Button returnBtn = new Button("← Return");
        styleTopButton(returnBtn);
        returnBtn.setOnAction(e -> new AdminDashboard(stage, user));
        header.getChildren().add(returnBtn);


        TextField searchField = styledField("Search");
        Button searchBtn = styledButton("Search");

        TextField nameField = styledField("Name");
        TextField barcodeField = styledField("Barcode");
        TextField priceField = styledField("Price");
        TextField qtyField = styledField("Quantity");

        Button addBtn = styledButton("Add");

        HBox searchBox = new HBox(10, searchField, searchBtn);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setPadding(new Insets(10));

        HBox inputBox = new HBox(10, nameField, barcodeField, priceField, qtyField, addBtn);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(10));

        VBox topBox = new VBox(10, searchBox, inputBox);
        topBox.setAlignment(Pos.CENTER);

        VBox topContainer = new VBox(header, topBox);
        root.setTop(topContainer);


        cardPane.setPadding(new Insets(20));
        cardPane.setHgap(20);
        cardPane.setVgap(20);
        cardPane.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(cardPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: #2C2C2C; -fx-border-color: #2C2C2C;");
        cardPane.setStyle("-fx-background-color: #2C2C2C;");

        Platform.runLater(() -> scrollPane.lookupAll(".scroll-bar")
                .forEach(sb -> sb.setStyle("-fx-pref-width:0; -fx-pref-height:0; -fx-opacity:0;")));

        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.show();


        addBtn.setOnAction(e -> {
            try {
                Product p = new Product(0,
                        nameField.getText(),
                        barcodeField.getText(),
                        parseDouble(priceField.getText()),
                        parseInt(qtyField.getText()));
                if (productService.addProduct(p)) refreshCards();
            } catch (Exception ex) {
                showAlert("Invalid input", "Please enter valid product details.");
            }
        });

        searchBtn.setOnAction(e -> {
            String keyword = searchField.getText();
            cardPane.getChildren().clear();
            for (Product p : productService.searchProducts(keyword)) {
                cardPane.getChildren().add(createCard(p));
            }
        });

        refreshCards();
    }


    private void refreshCards() {
        cardPane.getChildren().clear();
        for (Product p : productService.getAllProducts()) {
            cardPane.getChildren().add(createCard(p));
        }
    }

    private VBox createCard(Product p) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #2C2C2C; -fx-background-radius: 15;");
        card.setEffect(new DropShadow(10, Color.rgb(0,0,0,0.5)));
        card.setPrefWidth(250);

        Label name = new Label(p.getName());
        name.setTextFill(Color.web("#FFD700"));
        name.setFont(Font.font(16));

        Label barcode = new Label("Barcode: " + p.getBarcode());
        barcode.setTextFill(Color.web("#EEEEEE"));

        Label price = new Label("Price: $" + p.getPrice());
        price.setTextFill(Color.web("#EEEEEE"));

        Label qty = new Label("Quantity: " + p.getQuantity());
        qty.setTextFill(Color.web("#EEEEEE"));

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);

        Button deleteBtn = styledButton("Delete");
        deleteBtn.setStyle("-fx-background-color: #FF5555; -fx-text-fill: white; -fx-background-radius: 8;");

        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #FF5555; -fx-text-fill: white; -fx-background-radius: 8;"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: #FF5555; -fx-text-fill: white; -fx-background-radius: 8;"));
        deleteBtn.setOnAction(e -> {
            if (productService.deleteProduct(p.getId())) refreshCards();
            else showAlert("Cannot Delete", "This product has associated sales and cannot be deleted.");
        });

        Button updateBtn = styledButton("Update");
        updateBtn.setStyle("-fx-background-color: #FFD700; -fx-text-fill: #1C1C1C; -fx-background-radius: 8;");
        updateBtn.setOnAction(e -> showUpdateDialog(p));

        actions.getChildren().addAll(updateBtn, deleteBtn);

        card.getChildren().addAll(name, barcode, price, qty, actions);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #3A3A3A; -fx-background-radius: 15;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #2C2C2C; -fx-background-radius: 15;"));

        return card;
    }

    private void showUpdateDialog(Product p) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Product");


        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #1C1C1C, #2C2C2C);" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-font-family: 'Segoe UI';"
        );
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));


        TextField nameField = new TextField(p.getName());
        TextField barcodeField = new TextField(p.getBarcode());
        TextField priceField = new TextField(String.valueOf(p.getPrice()));
        TextField qtyField = new TextField(String.valueOf(p.getQuantity()));

        for (TextField tf : new TextField[]{nameField, barcodeField, priceField, qtyField}) {
            tf.setStyle(
                    "-fx-background-color: rgba(255,215,0,0.15);" +
                            "-fx-text-fill: #FFD700;" +
                            "-fx-prompt-text-fill: #FFD700;" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 5 10 5 10;"
            );
        }

        content.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Barcode:"), barcodeField,
                new Label("Price:"), priceField,
                new Label("Quantity:"), qtyField
        );

        dialogPane.setContent(content);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (!nameField.getText().isBlank()) p.setName(nameField.getText());
                    if (!barcodeField.getText().isBlank()) p.setBarcode(barcodeField.getText());
                    if (!priceField.getText().isBlank()) p.setPrice(parseDouble(priceField.getText()));
                    if (!qtyField.getText().isBlank()) p.setQuantity(parseInt(qtyField.getText()));

                    if (productService.updateProduct(p)) refreshCards();
                } catch (Exception ex) {
                    showAlert("Invalid input", "Please enter valid product details.");
                }
            }
        });
    }


    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(
                "-fx-background-color: #2C2C2C;" +
                        "-fx-text-fill: #FFD700;" +
                        "-fx-prompt-text-fill: #FFD700;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 5 10 5 10;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;"
        );
        return tf;
    }

    private Button styledButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font(14));
        btn.setStyle(
                "-fx-background-color: #FFD700;" +
                        "-fx-text-fill: #1C1C1C;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 5 15 5 15;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;"
        );
        btn.setEffect(new DropShadow(5, Color.BLACK));
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #FFEA70;" +
                        "-fx-text-fill: #1C1C1C;" +
                        "-fx-background-radius: 10;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #FFD700;" +
                        "-fx-text-fill: #1C1C1C;" +
                        "-fx-background-radius: 10;"
        ));
        return btn;
    }

    private void styleTopButton(Button btn) {
        btn.setFont(Font.font("Segoe UI", 16));
        btn.setStyle("-fx-background-color: #FFD700; -fx-text-fill: #1C1C1C; -fx-background-radius: 12; -fx-padding: 6 12 6 12;");
        btn.setEffect(new DropShadow(6, Color.rgb(0,0,0,0.4)));
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #FFEA70; -fx-text-fill: #1C1C1C; -fx-background-radius: 12;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #FFD700; -fx-text-fill: #1C1C1C; -fx-background-radius: 12;"));
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
}
