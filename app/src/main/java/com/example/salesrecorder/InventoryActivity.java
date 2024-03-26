package com.example.salesrecorder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InventoryActivity extends AppCompatActivity implements ProductAdapter.OnProductListener, ProductsRepository.ProductsCallback, AdapterView.OnItemSelectedListener, AdapterView.OnItemLongClickListener {

    private RecyclerView productsRecyclerView;
    private Spinner spinnerProductCategory;
    private Button addProductButton;
    private FloatingActionButton fabAddSubProduct;
    private ArrayAdapter<String> spinnerAdapter;
    private List<Category> categories;
    private List<String> categoryIds;
    private ProductAdapter productAdapter;
    private String userUID;
    private ProductsRepository productsRepository;
    private static final String TAG = "InventoryActivity";
    private CustomSpinnerAdapter customSpinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        categories = new ArrayList<>();
        categories.add(new Category("Select Product", null));
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userUID = currentUser.getUid();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeUI();
        productsRepository = new ProductsRepository();
        productsRepository.fetchProductCategories(userUID, this);
    }

    private void initializeUI() {
        spinnerProductCategory = findViewById(R.id.spinnerProductCategory);
        productsRecyclerView = findViewById(R.id.recyclerViewProducts);
        addProductButton = findViewById(R.id.btnAddProduct);
        fabAddSubProduct = findViewById(R.id.fabAddSubProduct);

        // Initialize the category list with a default "Select Category" option
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(new Category("Select Category", ""));

        // Initialize the custom spinner adapter with the category list
        customSpinnerAdapter = new CustomSpinnerAdapter(this, categoryList);
        spinnerProductCategory.setAdapter(customSpinnerAdapter);

        productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), this);
        productsRecyclerView.setAdapter(productAdapter);

        addProductButton.setOnClickListener(v -> showAddProductDialog());
        fabAddSubProduct.setOnClickListener(v -> showAddSubProductDialog());

        spinnerProductCategory.setOnItemSelectedListener(this);
        spinnerProductCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category selectedCategory = customSpinnerAdapter.getItem(position);
                if (selectedCategory != null && !selectedCategory.getId().isEmpty()) {
                    productsRepository.fetchSubProducts(userUID, selectedCategory.getId(), InventoryActivity.this);
                } else {
                    productAdapter.clearData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                productAdapter.clearData();
            }

        });

        customSpinnerAdapter.setOnItemLongClickListener(category -> {
            if (category != null && !category.getId().isEmpty()) {
                confirmAndDeleteCategory(category.getId());
            }
        });

    }
    private void confirmAndDeleteCategory(String categoryId) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this category and all its sub-products?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    productsRepository.deleteProductCategoryWithSubProducts(userUID, categoryId, new ProductsRepository.ProductsCallback() {
                        @Override
                        public void onProductCategoryDeleted() {
                            Toast.makeText(InventoryActivity.this, "Category deleted successfully", Toast.LENGTH_SHORT).show();
                            refreshCategoryData();
                        }

                        @Override
                        public void onProductCategoriesFetched(List<String> newCategories) {

                        }

                        @Override
                        public void onProductCategoriesFetched(List<String> categoryNames, List<String> categoryIds) {
                            // This callback is not used here.
                        }

                        @Override
                        public void onSubProductsFetched(List<String> subProductNames, List<String> subProductIds, String categoryId) {
                            // This callback is not used here.
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(InventoryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onSubProductDeleted() {
                            // This callback is not used here.
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onProductCategoriesFetched(List<String> newCategories) {

    }
    @Override
    public void onProductCategoriesFetched(List<String> categoryNames, List<String> categoryIds) {
        categories.clear(); // Clear the existing categories list
        categories.add(new Category("Select Product", "")); // Add default selection item

        for (int i = 0; i < categoryNames.size(); i++) {
            if (categoryNames.get(i) != null && categoryIds.get(i) != null) {
                categories.add(new Category(categoryNames.get(i), categoryIds.get(i)));
            }
        }

        customSpinnerAdapter.clear();
        customSpinnerAdapter.addAll(categories);
        customSpinnerAdapter.notifyDataSetChanged();

        // Reset spinner selection to the first item to avoid IndexOutOfBoundsException
        spinnerProductCategory.setSelection(0);

        // Fetch sub-products for the default or currently selected category
        if (categories.size() > 1) {
            Category selectedCategory = (Category) spinnerProductCategory.getSelectedItem();
            if (selectedCategory != null && !selectedCategory.getId().isEmpty()) {
                productsRepository.fetchSubProducts(userUID, selectedCategory.getId(), this);
            }
        }
    }

    @Override
    public void onProductCategoryAdded(Category category) {
    }

    @Override
    public void onSubProductAdded() {
        Toast.makeText(this, "Sub-product added successfully", Toast.LENGTH_SHORT).show();
        refreshCurrentCategory();
    }

    @Override
    public void onProductCategoryUpdated(String productId, String newProductName) {
        Toast.makeText(this, "Product category updated successfully", Toast.LENGTH_SHORT).show();
        refreshCurrentCategory();
    }

    @Override
    public void onProductCategoryDeleted() {
        Toast.makeText(this, "Category and sub-products deleted successfully", Toast.LENGTH_SHORT).show();
        refreshCategoryData(); // Refresh categories after deletion
    }

    @Override
    public void onSubProductsFetched(List<String> subProductNames, List<String> subProductIds, Category category) {

    }

    @Override
    public void onSubProductsFetched(List<String> subProductNames, List<String> subProductIds, List<String> categoryNames) {

    }

    @Override
    public void onSubProductDeleted() {
        Toast.makeText(this, "Sub-product deleted successfully", Toast.LENGTH_SHORT).show();
        refreshCurrentCategory();
    }
    private void refreshCurrentCategory() {
        int selectedCategoryIndex = spinnerProductCategory.getSelectedItemPosition();
        Category selectedCategory = customSpinnerAdapter.getItem(selectedCategoryIndex);

        if (selectedCategory != null && !selectedCategory.getId().isEmpty()) {
            productsRepository.fetchSubProducts(userUID, selectedCategory.getId(), this);
        } else {
            productAdapter.clearData();
        }
    }

    private void refreshCategoryData() {
        productsRepository.fetchProductCategories(userUID, this);
    }

    @Override
    public void onSubProductsFetched(List<String> subProductNames, List<String> subProductIds, String categoryId) {
        Category category = getCategoryById(categoryId);
        if (category != null) {
            productAdapter.updateData(subProductNames, subProductIds, Collections.nCopies(subProductNames.size(), category));
        } else {
            Log.e(TAG, "Category with ID " + categoryId + " not found.");
        }
    }

    private Category getCategoryById(String categoryId) {
        for (Category category : categories) {
            if (category.getId() != null && category.getId().equals(categoryId)) {
                return category;
            }
        }
        // Log an error if the category ID is not found
        Log.e(TAG, "Category with ID " + categoryId + " not found.");
        return null; // Return null if the category is not found.
    }
    @Override
    public void onError(Exception e) {
        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error occurred in InventoryActivity", e);
        Log.e("InventoryActivity", "Error occurred", e);
    }
    private void updateCategorySpinner(List<String> categoryNames) {
        spinnerAdapter.clear();
        spinnerAdapter.addAll(categoryNames);
        spinnerAdapter.notifyDataSetChanged();
        // Set the spinner to "Select Product" after updating the categories
        int selectProductPosition = spinnerAdapter.getPosition("Select Product");
        spinnerProductCategory.setSelection(selectProductPosition, false);
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Product Category");

        final EditText input = new EditText(this);
        input.setHint("Enter product category name");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String categoryName = input.getText().toString().trim();
            if (!categoryName.isEmpty()) {
                productsRepository.addProductCategory(userUID, categoryName, this);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAddSubProductDialog() {
        int selectedCategoryIndex = spinnerProductCategory.getSelectedItemPosition();
        Category selectedCategory = customSpinnerAdapter.getItem(selectedCategoryIndex);

        if (selectedCategory == null || selectedCategory.getId().isEmpty()) {
            Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show();
            return;
        }

        String categoryId = selectedCategory.getId();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Sub-Product to " + selectedCategory.getName());

        final EditText input = new EditText(this);
        input.setHint("Enter sub-product name");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String subProductName = input.getText().toString().trim();
            if (!subProductName.isEmpty()) {
                productsRepository.addSubProduct(userUID, categoryId, subProductName, this);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }


    @Override
    public void onEditClicked(int position) {
        // Get the product ID and product name based on the position
        String productId = productAdapter.getProductId(position);
        String productName = productAdapter.getProductName(position);
        // Call the method to show the edit dialog with the product ID and name
        showEditProductDialog(productId, productName);
    }

    private void showEditProductDialog(String productId, String productName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Product");

        final EditText input = new EditText(this);
        input.setText(productName);
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newProductName = input.getText().toString().trim();
            if (!newProductName.isEmpty()) {
                productsRepository.updateProductName(userUID, productId, newProductName, this);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onDeleteClicked(int position) {
        String subProductId = productAdapter.getProductId(position);
        int selectedCategoryIndex = spinnerProductCategory.getSelectedItemPosition();
        Category selectedCategory = customSpinnerAdapter.getItem(selectedCategoryIndex);

        if (selectedCategory != null && !selectedCategory.getId().isEmpty()) {
            productsRepository.deleteSubProduct(userUID, selectedCategory.getId(), subProductId, this);
        } else {
            Toast.makeText(this, "Category not found", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Category selectedCategory = customSpinnerAdapter.getItem(position);
        if (selectedCategory != null && !selectedCategory.getId().isEmpty()) {
            confirmAndDeleteCategory(selectedCategory.getId());
        }
        return true;
    }
}