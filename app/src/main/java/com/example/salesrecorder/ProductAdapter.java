package com.example.salesrecorder;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<String> products;
    private List<String> productIds;
    private List<Category> categories;
    private OnProductListener onProductListener;
    private static final String TAG = "ProductAdapter";

    public ProductAdapter(List<String> products, List<String> productIds, List<Category> categories, OnProductListener onProductListener) {
        this.products = new ArrayList<>(products);
        this.productIds = new ArrayList<>(productIds);
        this.categories = new ArrayList<>(categories);
        this.onProductListener = onProductListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view, onProductListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String product = products.get(position);
        Category category = categories.get(position);
        holder.productNameTextView.setText(product + (category != null ? " (" + category.getName() + ")" : ""));
        Log.d(TAG, "Binding view for position: " + position + " with product: " + product + " and category: " + (category != null ? category.getName() : "null"));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }
    public void updateData(List<String> newProducts, List<String> newProductIds, List<Category> newCategories) {
        Log.d(TAG, "updateData: Updating product adapter data.");
        products.clear();
        productIds.clear();
        categories.clear();

        products.addAll(newProducts);
        productIds.addAll(newProductIds);
        categories.addAll(newCategories);

        notifyDataSetChanged();
        Log.d(TAG, "updateData: Data updated, notifyDataSetChanged called.");
    }
    public void clearData() {
        Log.d(TAG, "Clearing data in ProductAdapter");
        products.clear();
        productIds.clear();
        categories.clear();
        notifyDataSetChanged();
        Log.d(TAG, "Data cleared");
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView;
        ImageButton btnEditProduct, btnDeleteProduct;

        public ViewHolder(View itemView, OnProductListener onProductListener) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.tvProductName);
            btnEditProduct = itemView.findViewById(R.id.btnEditProduct);
            btnDeleteProduct = itemView.findViewById(R.id.btnDeleteProduct);

            btnEditProduct.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onProductListener.onEditClicked(position);
                }
            });

            btnDeleteProduct.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onProductListener.onDeleteClicked(position);
                }
            });
        }
    }

    public interface OnProductListener {
        void onProductCategoryAdded(Category category);
        void onSubProductAdded();
        void onProductCategoryUpdated(String productId, String newProductName);
        void onProductCategoryDeleted();
        void onSubProductsFetched(List<String> subProductNames, List<String> subProductIds, Category category);

        void onSubProductsFetched(List<String> subProductNames, List<String> subProductIds, List<String> categoryNames);

        void onEditClicked(int position);
        void onDeleteClicked(int position);
    }

    public String getProductId(int position) {
        return productIds.get(position);
    }

    public String getProductName(int position) {
        return products.get(position);
    }

    public void removeProduct(int position) {
        if (position < 0 || position >= products.size()) return;
        products.remove(position);
        productIds.remove(position);
        categories.remove(position);
        notifyItemRemoved(position);
    }

}
