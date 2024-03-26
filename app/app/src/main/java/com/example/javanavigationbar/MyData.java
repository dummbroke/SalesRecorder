package com.example.javanavigationbar;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.Date;

public class MyData implements Serializable {
    public enum ProductType {
        TUNA,
        BANGUS,
        TILAPIA,
        RIBS,
        PORK_CHOP,
        MASKARA
    }

    private String id;
    private int storageNumber; // Added this line
    private ProductType productName;
    private int productPrice;
    private int productWeight;
    private Date date;

    // Empty constructor is needed for Firestore's deserializer
    public MyData() {}

    public MyData(int storageNumber, ProductType productType, int productPrice, int productWeight, Date date) {
        this.storageNumber = storageNumber;
        this.productName = productType;
        this.productPrice = productPrice;
        this.productWeight = productWeight;
        this.date = date;
    }

    public MyData(DocumentSnapshot document) {
        this.id = document.getId();
        this.storageNumber = document.getLong("storageNumber").intValue(); // Added this line
        this.productName = ProductType.valueOf(document.getString("productName").toUpperCase());
        this.productPrice = document.getLong("productPrice").intValue();
        this.productWeight = document.getLong("productWeight").intValue();
        this.date = document.getDate("date");
    }

    // getters and setters for each field

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStorageNumber() { // Added this method
        return storageNumber;
    }

    public void setStorageNumber(int storageNumber) { // Added this method
        this.storageNumber = storageNumber;
    }

    public ProductType getProductName() {
        return productName;
    }

    public void setProductName(ProductType productName) {
        this.productName = productName;
    }

    public int getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(int productPrice) {
        this.productPrice = productPrice;
    }

    public int getProductWeight() {
        return productWeight;
    }

    public void setProductWeight(int productWeight) {
        this.productWeight = productWeight;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void saveToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Save the MyData object to the appropriate Firestore collection
        if (storageNumber == 1) {
            db.collection("storage1").add(this);
        } else if (storageNumber == 2) {
            db.collection("storage2").add(this);
        } else if (storageNumber == 3) {
            db.collection("storage3").add(this);
        }
    }
}
