package com.example.salesrecorder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.SalesViewHolder> {
    private List<MyData> salesData;

    public SalesAdapter(List<MyData> salesData) {
        this.salesData = salesData;
    }

    @Override
    public SalesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_row2, parent, false);
        return new SalesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SalesViewHolder holder, int position) {
        MyData data = salesData.get(position);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // Format to display time

        holder.productId.setText(String.valueOf(position + 1)); // Assuming position corresponds to product ID
        holder.productName.setText(data.getSubProduct()); // You might want to check if you need mainProduct or subProduct
        holder.productDate.setText(data.getDate() != null ? timeFormat.format(data.getDate()) : "N/A"); // Using time format
        holder.productWeight.setText(String.format(Locale.getDefault(), "%.2f", data.getProductWeight()));
        holder.productPrice.setText(String.format(Locale.getDefault(), "%.2f", data.getProductPrice()));
    }


    @Override
    public int getItemCount() {
        return salesData.size();
    }

    public void setSalesData(List<MyData> salesData) {
        this.salesData = salesData;
        notifyDataSetChanged();
    }

    static class SalesViewHolder extends RecyclerView.ViewHolder {
        TextView productId, productName, productDate, productWeight, productPrice;

        public SalesViewHolder(View itemView) {
            super(itemView);
            productId = itemView.findViewById(R.id.product_id_txt);
            productName = itemView.findViewById(R.id.product_name_txt);
            productDate = itemView.findViewById(R.id.product_date_txt);
            productWeight = itemView.findViewById(R.id.product_weight_txt);
            productPrice = itemView.findViewById(R.id.product_price_txt);
        }
    }
}
