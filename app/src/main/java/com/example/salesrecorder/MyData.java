package com.example.salesrecorder;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.Date;

public class MyData implements Serializable {
    private String id;
    private String mainProduct;
    private String subProduct;
    private float productPrice;
    private float productWeight;
    private Date date;
    private String categoryId;
    private String subProductId;

    public MyData() {}

    public MyData(String mainProduct, String subProduct, float productPrice, float productWeight, Date date, String categoryId, String subProductId) {
        this.mainProduct = mainProduct;
        this.subProduct = subProduct;
        this.productPrice = productPrice;
        this.productWeight = productWeight;
        this.date = date;
        this.categoryId = categoryId;
        this.subProductId = subProductId;
    }
    public MyData(DocumentSnapshot document) {
        if (document != null) {
            this.id = document.getId();
            this.mainProduct = document.getString("mainProduct");
            this.subProduct = document.getString("subProduct");
            this.productPrice = document.getDouble("productPrice").floatValue();
            this.productWeight = document.getDouble("productWeight").floatValue();
            this.date = document.getDate("date");
        }
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMainProduct() { return mainProduct; }
    public void setMainProduct(String mainProduct) { this.mainProduct = mainProduct; }
    public String getSubProduct() { return subProduct; }
    public void setSubProduct(String subProduct) { this.subProduct = subProduct; }
    public float getProductPrice() { return productPrice; }
    public void setProductPrice(float productPrice) { this.productPrice = productPrice; }
    public float getProductWeight() { return productWeight; }
    public void setProductWeight(float productWeight) { this.productWeight = productWeight; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getCategoryId() { return categoryId; }
    public void setSubProductId(String subProductId) { this.subProductId = subProductId; }
    public String getSubProductId() { return subProductId; }

    @Override
    public String toString() {
        return "MyData{" +
                "id='" + id + '\'' +
                ", mainProduct='" + mainProduct + '\'' +
                ", subProduct='" + subProduct + '\'' +
                ", productPrice=" + productPrice +
                ", productWeight=" + productWeight +
                ", date=" + date +
                ", categoryId='" + categoryId + '\'' +
                ", subProductId='" + subProductId + '\'' +
                '}';
    }
    public void saveToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String TAG = "MyData";

        if (user == null) {
            Log.d(TAG, "User is not authenticated. Cannot save data to Firestore.");
            return;
        }

        String salesCollection = "Sales";
        DocumentReference newSalesRef = db.collection("users").document(user.getUid())
                .collection(salesCollection).document();

        this.setId(newSalesRef.getId()); // Set the ID before adding the document

        newSalesRef.set(this)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "New document added to Sales with ID: " + this.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding document to Sales", e));
    }
}
