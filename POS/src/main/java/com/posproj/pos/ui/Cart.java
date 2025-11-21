package com.posproj.pos.ui;

import com.posproj.pos.models.CartItem;
import com.posproj.pos.models.Product;
import com.posproj.pos.models.User;
import com.posproj.pos.services.ProductService;
import com.posproj.pos.services.SalesService;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;

public class Cart {

    private final ProductService productService = new ProductService();
    private final SalesService salesService = new SalesService();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<CartItem> cartList = FXCollections.observableArrayList();
    private final DecimalFormat money = new DecimalFormat("0.00");

    private final Label subtotalLabel = new Label("Subtotal: $0.00");
    private final Label itemsLabel = new Label("Items: 0");
    private final VBox cartBox;
    private final FlowPane productGrid;
    private final StringBuilder barcodeBuffer = new StringBuilder();
    private final PauseTransition barcodeResetTimer = new PauseTransition(Duration.millis(300));

    public Cart(Stage stage, User user) {

        Button returnBtn = new Button("Return");
        returnBtn.setStyle("-fx-background-color:#FFD966; -fx-text-fill:#1B1B1B; -fx-font-weight:bold; -fx-background-radius:6;");
        returnBtn.setOnAction(e -> new AdminDashboard(stage, user));

        HBox topBar = new HBox(10, returnBtn);
        topBar.setPadding(new Insets(12));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color:#2B2B2B; -fx-background-radius:0 0 12 12;");

        productGrid = new FlowPane();
        productGrid.setHgap(12);
        productGrid.setVgap(12);
        productGrid.setPadding(new Insets(16));
        productGrid.setStyle("-fx-background-color:#1F1F1F; -fx-background-radius:12;");

        ScrollPane scrollProducts = new ScrollPane(productGrid);
        scrollProducts.setFitToWidth(true);
        scrollProducts.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollProducts.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollProducts.setPrefWidth(640);
        scrollProducts.setStyle(
                "-fx-background:#1F1F1F;" +
                        "-fx-background-insets:0;" +
                        "-fx-padding:0;" +
                        "-fx-background-radius:12;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;" +
                        "-fx-border-color: transparent;"
        );

        cartBox = new VBox(10);
        cartBox.setPadding(new Insets(10));
        cartBox.setStyle("-fx-background-color:transparent;");

        ScrollPane cartScroll = new ScrollPane(cartBox);
        cartScroll.setFitToWidth(true);
        cartScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        cartScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        cartScroll.setStyle(
                "-fx-background:#262626;" +
                        "-fx-background-insets:0;" +
                        "-fx-padding:8;" +
                        "-fx-background-radius:12;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;" +
                        "-fx-border-color: transparent;"
        );

        VBox rightPanel = new VBox(cartScroll);
        rightPanel.setPadding(new Insets(16));
        rightPanel.setStyle("-fx-background-color:#262626; -fx-background-radius:12;");
        rightPanel.setPrefWidth(420);

        HBox center = new HBox(16, scrollProducts, rightPanel);
        center.setPadding(new Insets(16));
        center.setStyle("-fx-background-color:#1B1B1B;");

        subtotalLabel.setFont(Font.font("Segoe UI", 20));
        subtotalLabel.setTextFill(Color.web("#FFD966"));

        itemsLabel.setFont(Font.font(14));
        itemsLabel.setTextFill(Color.web("#FFECC1"));

        Button checkoutBtn = styledPrimary("Checkout");
        checkoutBtn.setPrefHeight(50);
        checkoutBtn.setOnAction(e -> performCheckout());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField discountField = new TextField();
        discountField.setPromptText("Discount %");
        discountField.setPrefWidth(70);
        discountField.setStyle("-fx-background-color:#303030; -fx-text-fill:#FFE39C; -fx-background-radius:6;");
        discountField.textProperty().addListener((obs, oldVal, newVal) -> updateTotals());

        Label finalTotalLabel = new Label("Final: $0.00");
        finalTotalLabel.setFont(Font.font(18));
        finalTotalLabel.setTextFill(Color.web("#FFD966"));

        HBox bottomBar = new HBox(20,
                subtotalLabel,
                itemsLabel,
                discountField,
                finalTotalLabel,
                spacer,
                checkoutBtn
        );
        bottomBar.setPadding(new Insets(16));
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setStyle("-fx-background-color:#2B2B2B; -fx-background-radius: 12 12 0 0;");

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(center);
        root.setBottom(bottomBar);
        root.setStyle("-fx-background-color:#1B1B1B;");

        Scene scene = new Scene(root,
                Math.max(1100, Screen.getPrimary().getBounds().getWidth() * 0.9),
                Math.max(680, Screen.getPrimary().getBounds().getHeight() * 0.8));
        stage.setScene(scene);
        stage.show();

        productList.setAll(productService.searchProducts(""));
        loadProducts();

        cartList.addListener((javafx.collections.ListChangeListener<CartItem>) change -> {
            updateTotals();
            refreshCartUI();
        });

        refreshCartUI();

        barcodeResetTimer.setOnFinished(e -> barcodeBuffer.setLength(0));

        scene.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            String ch = e.getCharacter();
            if (ch != null && ch.length() > 0 && ch.charAt(0) >= 32) {
                barcodeBuffer.append(ch);
                barcodeResetTimer.playFromStart();
            }
        });

        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String code = barcodeBuffer.toString().trim();
                barcodeBuffer.setLength(0);
                barcodeResetTimer.stop();
                if (!code.isEmpty()) handleBarcodeScan(code);
                e.consume();
            }
        });
    }

    private void handleBarcodeScan(String code) {
        if (code.isBlank()) return;

        Product p = productList.stream()
                .filter(prod -> {
                    try {
                        Method m = prod.getClass().getMethod("getBarcode");
                        Object val = m.invoke(prod);
                        if (val != null) {
                            String barcodeValue = String.valueOf(val).trim();
                            if (!barcodeValue.isEmpty() && barcodeValue.equalsIgnoreCase(code)) return true;
                        }
                    } catch (NoSuchMethodException nsme) {
                    } catch (Exception ex) {
                    }
                    return prod.getName() != null && prod.getName().equalsIgnoreCase(code);
                })
                .findFirst()
                .orElse(null);

        if (p == null) {
            showInfo("No product found for scanned code: " + code);
            return;
        }
        if (p.getQuantity() <= 0) {
            showStockAlert("Product '" + p.getName() + "' is out of stock!");
            return;
        }
        addToCart(p, 1);
    }

    private void loadProducts() {
        productGrid.getChildren().clear();
        for (Product p : productList) productGrid.getChildren().add(createProductCard(p));
    }

    private VBox createProductCard(Product p) {
        Label name = new Label(p.getName());
        name.setFont(Font.font(14));
        name.setTextFill(Color.web("#FFECC1"));
        name.setWrapText(true);
        name.setMaxWidth(140);

        Label price = new Label("$" + money.format(p.getPrice()));
        price.setTextFill(Color.web("#FFD966"));
        price.setFont(Font.font(13));

        Label stock = new Label("Stock: " + p.getQuantity());
        stock.setTextFill(p.getQuantity() > 0 ? Color.LIGHTGREEN : Color.SALMON);
        stock.setFont(Font.font(12));

        TextField qtyField = new TextField("1");
        qtyField.setPrefWidth(50);
        qtyField.setStyle("-fx-background-radius:6; -fx-background-color:#303030; -fx-text-fill:#FFE39C;");

        Button addBtn = styledPrimary("Add");
        addBtn.setDisable(p.getQuantity() <= 0);
        addBtn.setOnAction(e -> {
            int qty;
            try { qty = Integer.parseInt(qtyField.getText().trim()); } catch (Exception ex) { return; }
            if (qty <= 0) return;
            if (p.getQuantity() < qty) {
                showStockAlert("Not enough stock. Available: " + p.getQuantity());
                return;
            }
            addToCart(p, qty);
        });

        VBox card = new VBox(8, name, price, stock, qtyField, addBtn);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12));
        card.setPrefWidth(160);
        card.setStyle("-fx-background-color:#2A2A2A; -fx-background-radius:12;");
        card.setOnMouseEntered(ev -> card.setStyle("-fx-background-color:#333333; -fx-background-radius:12;"));
        card.setOnMouseExited(ev -> card.setStyle("-fx-background-color:#2A2A2A; -fx-background-radius:12;"));
        card.setEffect(new DropShadow(6, Color.rgb(0,0,0,0.4)));
        return card;
    }

    private void addToCart(Product p, int qty) {
        CartItem existing = cartList.stream()
                .filter(c -> c.getName().equals(p.getName()))
                .findFirst().orElse(null);
        if (existing != null) {
            int newQty = existing.getQuantity() + qty;
            int idx = cartList.indexOf(existing);
            if (idx >= 0) cartList.set(idx, new CartItem(existing.getName(), existing.getPrice(), newQty));
        } else {
            cartList.add(new CartItem(p.getName(), p.getPrice(), qty));
        }
        p.setQuantity(p.getQuantity() - qty);
        loadProducts();
        refreshCartUI();
        updateTotals();
    }

    private void removeFromCart(CartItem item) {
        productList.stream()
                .filter(p -> p.getName().equals(item.getName()))
                .findFirst()
                .ifPresent(p -> p.setQuantity(p.getQuantity() + item.getQuantity()));
        cartList.remove(item);
        loadProducts();
        refreshCartUI();
        updateTotals();
    }

    private void performCheckout() {
        if (cartList.isEmpty()) return;
        salesService.checkout(cartList, productList);
        String receipt = generateReceipt();
        showReceiptPopup(receipt);
        cartList.clear();
        loadProducts();
        refreshCartUI();
        updateTotals();
    }

    private String generateReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("------ POS Receipt ------\n");
        sb.append("Items:\n");
        double sub = 0;
        for (CartItem item : cartList) {
            double lineTotal = item.getPrice() * item.getQuantity();
            sb.append(String.format("%s x%d - $%s\n",
                    item.getName(),
                    item.getQuantity(),
                    money.format(lineTotal)));
            sub += lineTotal;
        }
        HBox bottom = (HBox) subtotalLabel.getParent();
        TextField discountField = (TextField) bottom.getChildren().get(2);
        double discountPercent = 0;
        try { if (!discountField.getText().isBlank()) discountPercent = Double.parseDouble(discountField.getText()); } catch (Exception ignored) {}
        double discountAmount = sub * (discountPercent / 100);
        double finalTotal = sub - discountAmount;
        sb.append("------------------------\n");
        sb.append("Subtotal: $" + money.format(sub) + "\n");
        sb.append("Discount: " + discountPercent + "% (-$" + money.format(discountAmount) + ")\n");
        sb.append("Final Total: $" + money.format(finalTotal) + "\n");
        sb.append("Thank you for your purchase!\n");
        sb.append("------------------------\n");
        return sb.toString();
    }

    private void showReceiptPopup(String receiptText) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Receipt");

        TextArea textArea = new TextArea(receiptText);
        textArea.setFont(Font.font("Monospaced", 14));
        textArea.setEditable(false);
        textArea.setStyle("-fx-control-inner-background:#262626; -fx-text-fill:#FFECC1;");

        Button closeBtn = new Button("Close");
        closeBtn.setPrefWidth(80);
        closeBtn.setStyle("-fx-background-color:#F4C25A; -fx-background-radius:6; -fx-font-weight:bold; -fx-text-fill:#1B1B1B;");
        closeBtn.setOnAction(e -> dialog.close());

        Button openBtn = new Button("Open in Notes");
        openBtn.setPrefWidth(120);
        openBtn.setStyle("-fx-background-color:#FFD966; -fx-background-radius:6; -fx-font-weight:bold; -fx-text-fill:#1B1B1B;");
        openBtn.setOnAction(e -> openReceiptInNotes(receiptText));

        HBox buttons = new HBox(12, openBtn, closeBtn);
        buttons.setAlignment(Pos.CENTER);

        VBox box = new VBox(12, textArea, buttons);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:#262626; -fx-background-radius:12;");

        Scene scene = new Scene(box, 400, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void openReceiptInNotes(String text) {
        try {
            File tempFile = File.createTempFile("POS_Receipt_", ".txt");
            tempFile.deleteOnExit();
            try (FileWriter writer = new FileWriter(tempFile)) { writer.write(text); }
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().edit(tempFile);
            else showInfo("Cannot open Notes on this system.");
        } catch (IOException ex) {
            ex.printStackTrace();
            showInfo("Failed to open receipt: " + ex.getMessage());
        }
    }

    private void showInfo(String message) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        Label lbl = new Label(message);
        lbl.setFont(Font.font(14));
        lbl.setTextFill(Color.web("#FFECC1"));

        Button ok = new Button("OK");
        ok.setPrefWidth(80);
        ok.setStyle("-fx-background-color:#F4C25A; -fx-background-radius:6; -fx-font-weight:bold; -fx-text-fill:#222222;");
        ok.setOnAction(e -> dialog.close());

        VBox box = new VBox(12, lbl, ok);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:#262626; -fx-background-radius:12;");

        Scene scene = new Scene(box, 300, 120);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void updateTotals() {
        double sub = 0;
        int items = 0;
        for (CartItem i : cartList) { sub += i.getPrice() * i.getQuantity(); items += i.getQuantity(); }
        subtotalLabel.setText("Subtotal: $" + money.format(sub));
        itemsLabel.setText("Items: " + items);

        double discountPercent = 0;
        try {
            TextField discountField = (TextField) ((HBox) subtotalLabel.getParent()).getChildren().get(2);
            if (!discountField.getText().isBlank()) discountPercent = Double.parseDouble(discountField.getText());
        } catch (Exception ignored) {}

        double discountAmount = sub * (discountPercent / 100);
        double finalTotal = sub - discountAmount;

        Label finalTotalLabel = (Label) ((HBox) subtotalLabel.getParent()).getChildren().get(3);
        finalTotalLabel.setText("Final: $" + money.format(finalTotal));
    }

    private void refreshCartUI() {
        cartBox.getChildren().clear();
        if (cartList.isEmpty()) {
            Label empty = new Label("Cart is empty");
            empty.setFont(Font.font(14));
            empty.setTextFill(Color.web("#CFCFCF"));
            cartBox.getChildren().add(empty);
            return;
        }
        for (CartItem item : cartList) cartBox.getChildren().add(createCartCard(item));
    }

    private HBox createCartCard(CartItem item) {
        Label name = new Label(item.getName());
        name.setFont(Font.font(15));
        name.setTextFill(Color.web("#FFECC1"));
        name.setMaxWidth(180);
        name.setWrapText(true);

        Label price = new Label("$" + money.format(item.getPrice() * item.getQuantity()));
        price.setFont(Font.font(15));
        price.setTextFill(Color.web("#FFD966"));

        Button removeBtn = removeButton("Remove");
        removeBtn.setPrefWidth(72);
        removeBtn.setOnAction(e -> removeFromCart(item));

        HBox card = new HBox(12, name, price, removeBtn);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color:#2A2A2A; -fx-background-radius:12;");
        card.setEffect(new DropShadow(6, Color.rgb(0,0,0,0.35)));
        card.setOnMouseEntered(ev -> card.setStyle("-fx-background-color:#333333; -fx-background-radius:12;"));
        card.setOnMouseExited(ev -> card.setStyle("-fx-background-color:#2A2A2A; -fx-background-radius:12;"));
        return card;
    }

    private Button styledPrimary(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:#F4C25A; -fx-font-weight:bold; -fx-background-radius:6; -fx-text-fill:#222222;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color:#FFD97D; -fx-font-weight:bold; -fx-background-radius:6; -fx-text-fill:#222222;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color:#F4C25A; -fx-font-weight:bold; -fx-background-radius:6; -fx-text-fill:#222222;"));
        return b;
    }

    private Button removeButton(String text) {
        Button b = new Button(text);
        b.setPrefSize(72, 32);
        b.setStyle("-fx-background-color:#DD4444; -fx-background-radius:6; -fx-text-fill:white;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color:#FF6666; -fx-background-radius:6; -fx-text-fill:white;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color:#DD4444; -fx-background-radius:6; -fx-text-fill:white;"));
        return b;
    }

    private void showStockAlert(String message) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Stock Notice");

        Label lbl = new Label(message);
        lbl.setFont(Font.font(14));
        lbl.setTextFill(Color.web("#222222"));

        Button ok = new Button("OK");
        ok.setPrefWidth(80);
        ok.setStyle("-fx-background-color:#F4C25A; -fx-background-radius:6; -fx-font-weight:bold; -fx-text-fill:#222222;");
        ok.setOnAction(e -> dialog.close());

        VBox box = new VBox(12, lbl, ok);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(18));
        box.setStyle("-fx-background-color:#FFEFCF; -fx-background-radius:12;");

        Scene scene = new Scene(box);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
