package com.example.salesrecorder;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductsRepository {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "ProductsRepository";

    public interface ProductsCallback {
        void onProductCategoriesFetched(List<String> newCategories);
        void onProductCategoriesFetched(List<String> categoryNames, List<String> categoryIds);
        void onSubProductsFetched(List<String> subProductNames, List<String> subProductIds, String categoryId);
        void onSubProductDeleted();
        void onError(Exception e);
        void onProductCategoryDeleted();
    }
    public void fetchProductCategories(String userUID, ProductsCallback callback) {
        Log.d(TAG, "Fetching product categories for UID: " + userUID);
        db.collection("users").document(userUID).collection("productCategories").get()
                .addOnSuccessListener(result -> {
                    List<String> categoryNames = new ArrayList<>();
                    List<String> categoryIds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : result) {
                        String name = document.getString("name");
                        String id = document.getId();
                        categoryNames.add(name);
                        categoryIds.add(id);
                        Log.d(TAG, "Fetched category: " + name + " with ID: " + id);
                    }
                    callback.onProductCategoriesFetched(categoryNames, categoryIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching product categories", e);
                    callback.onError(e);
                });
    }

    public void fetchSubProducts(String userUID, String categoryId, ProductsCallback callback) {
        Log.d(TAG, "Fetching sub-products for category ID: " + categoryId);
        db.collection("users").document(userUID).collection("productCategories")
                .document(categoryId).collection("subProducts").get()
                .addOnSuccessListener(result -> {
                    List<String> subProductNames = new ArrayList<>();
                    List<String> subProductIds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : result) {
                        String name = document.getString("name");
                        subProductNames.add(name);
                        subProductIds.add(document.getId());
                        Log.d(TAG, "Fetched sub-product: " + name + " with ID: " + document.getId());
                    }
                    callback.onSubProductsFetched(subProductNames, subProductIds, categoryId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching sub-products", e);
                    callback.onError(e);
                });
    }

    public void addProductCategory(String userUID, String categoryName, ProductsCallback callback) {
        Map<String, Object> categoryData = new HashMap<>();
        categoryData.put("name", categoryName);

        db.collection("users").document(userUID).collection("productCategories")
                .add(categoryData)
                .addOnSuccessListener(documentReference -> {
                    fetchProductCategories(userUID, callback);
                })
                .addOnFailureListener(callback::onError);
    }

    public void addSubProduct(String userUID, String categoryId, String subProductName, ProductsCallback callback) {
        Map<String, Object> subProductData = new HashMap<>();
        subProductData.put("name", subProductName);
        db.collection("users").document(userUID).collection("productCategories").document(categoryId)
                .collection("subProducts").add(subProductData)
                .addOnSuccessListener(documentReference -> fetchSubProducts(userUID, categoryId, callback))
                .addOnFailureListener(callback::onError);
    }


    public void updateProductName(String userUID, String productId, String newProductName, ProductsCallback callback) {
        db.collection("products").document(productId)
                .update("name", newProductName)
                .addOnSuccessListener(aVoid -> fetchProductCategories(userUID, callback))
                .addOnFailureListener(callback::onError);
    }

    // Delete a sub-product from a category
    public void deleteSubProduct(String userUID, String categoryId, String productId, ProductsCallback callback) {
        db.collection("users").document(userUID).collection("productCategories").document(categoryId)
                .collection("subProducts").document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSubProductDeleted())
                .addOnFailureListener(callback::onError);
    }

    public void deleteProductCategoryWithSubProducts(String userUID, String categoryId, ProductsCallback callback) {
        DocumentReference categoryRef = db.collection("users").document(userUID).collection("productCategories").document(categoryId);
        WriteBatch batch = db.batch();
        batch.delete(categoryRef);

        categoryRef.collection("subProducts").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot subProduct : queryDocumentSnapshots) {
                        batch.delete(subProduct.getReference());
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Category and sub-products deleted successfully");
                                callback.onProductCategoryDeleted();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting category and sub-products", e);
                                callback.onError(e);
                            });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching sub-products for category", e));
    }
}