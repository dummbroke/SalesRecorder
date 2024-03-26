package com.example.salesrecorder;

public class Product {
    private String id;
    private String name;

    // Firestore requires a no-argument constructor for deserialization
    public Product() {}

    public Product(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

