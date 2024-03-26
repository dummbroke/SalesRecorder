package com.example.salesrecorder;

public class Category {
    private String name;
    private String id;

    public Category(String name, String id) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        // Allow null or empty ID for special placeholder categories
        this.name = name;
        this.id = id; // No exception is thrown for null or empty ID
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Category{" +
                "name='" + name + '\'' +
                ", id='" + (id == null ? "null" : id) + '\'' +
                '}';
    }
}
