package com.posproj.pos.models;

import javafx.beans.property.*;

public class CartItem {
    private final StringProperty name;
    private final DoubleProperty price;
    private final IntegerProperty quantity;

    public CartItem(String name, double price, int quantity) {
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.quantity = new SimpleIntegerProperty(quantity);
    }

    public String getName() { return name.get(); }
    public double getPrice() { return price.get(); }
    public int getQuantity() { return quantity.get(); }

    public void setQuantity(int qty) { quantity.set(qty); }

    public StringProperty nameProperty() { return name; }
    public DoubleProperty priceProperty() { return price; }
    public IntegerProperty quantityProperty() { return quantity; }
}
