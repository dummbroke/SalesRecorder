package com.example.salesrecorder;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private Context context;
    private List<MyData> myDataList;
    private OnItemClickListener mListener;
    private OnItemLongClickListener mLongListener;
    private static final String TAG = "CustomAdapter";
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public interface OnItemLongClickListener {
        boolean onItemLongClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mLongListener = listener;
    }
    public CustomAdapter(Context context, List<MyData> myDataList) {
        this.context = context;
        this.myDataList = myDataList;
    }
    public void updateDataList(List<MyData> newDataList, TextView totalSalesCount, TextView totalSalesPrice, TextView totalSalesKilo) {
        this.myDataList.clear(); // Clear existing data
        this.myDataList.addAll(newDataList); // Add all new data
        notifyDataSetChanged(); // Notify the adapter of the change
        recalculateTotals(totalSalesCount, totalSalesPrice, totalSalesKilo); // Update totals
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row, parent, false);
        return new MyViewHolder(view, mListener, mLongListener);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        MyData currentItem = myDataList.get(position);
        if (currentItem != null) {
            // Format the Date object into a string showing hour and minute with AM/PM
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = currentItem.getDate();
            String formattedTime = date != null ? timeFormat.format(date) : "N/A";

            // Set the formatted time to the product_date_txt TextView
            holder.product_date_txt.setText(formattedTime);

            // Ensure the product name is set correctly, use subProduct if available
            String productName = currentItem.getSubProduct() != null && !currentItem.getSubProduct().isEmpty()
                    ? currentItem.getSubProduct()
                    : currentItem.getMainProduct();

            // Set other item views as before
            holder.product_id_txt.setText(String.valueOf(position + 1));
            holder.product_name_txt.setText(productName);
            holder.product_price_txt.setText(String.valueOf(currentItem.getProductPrice()));
            holder.product_weight_txt.setText(String.valueOf(currentItem.getProductWeight()));
            holder.itemView.setTag(currentItem.getId());
        } else {
            Log.e(TAG, "Item at position " + position + " is null.");
        }
    }
    @Override
    public int getItemCount() {
        return myDataList.size();
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView product_id_txt, product_name_txt, product_price_txt, product_weight_txt, product_date_txt;
        public MyViewHolder(View itemView, OnItemClickListener clickListener, OnItemLongClickListener longClickListener) {
            super(itemView);
            product_id_txt = itemView.findViewById(R.id.product_id_txt);
            product_name_txt = itemView.findViewById(R.id.product_name_txt);
            product_price_txt = itemView.findViewById(R.id.product_price_txt);
            product_weight_txt = itemView.findViewById(R.id.product_weight_txt);
            product_date_txt = itemView.findViewById(R.id.product_date_txt);

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        clickListener.onItemClick(position);
                    }
                }
            });
            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        return longClickListener.onItemLongClick(position);
                    }
                }
                return false;
            });
        }
    }
    public void deleteItem(int position, TextView totalSalesCount, TextView totalSalesPrice, TextView totalSalesKilo) {
        // Check if the current thread is the main thread since UI updates should be done on the main thread.
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // Proceed with the deletion if the position is valid.
            deleteItemInternal(position, totalSalesCount, totalSalesPrice, totalSalesKilo);
        } else {
            // If not on the main thread, post the task to the main thread handler.
            new Handler(Looper.getMainLooper()).post(() -> deleteItemInternal(position, totalSalesCount, totalSalesPrice, totalSalesKilo));
        }
    }
    private void deleteItemInternal(int position, TextView totalSalesCount, TextView totalSalesPrice, TextView totalSalesKilo) {
        synchronized (myDataList) {
            // Ensure the list is not empty and the position is valid before attempting deletion.
            if (myDataList.isEmpty() || position < 0 || position >= myDataList.size()) {
                Log.e(TAG, "Cannot delete item at position " + position + ": List is empty or position is invalid.");
                return;
            }

            MyData item = myDataList.get(position);
            if (item == null || item.getId() == null) {
                Log.e(TAG, "Cannot delete item at position " + position + ": Item is null or has no ID.");
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Log.e(TAG, "Cannot delete item at position " + position + ": User is not authenticated.");
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String documentPath = "users/" + user.getUid() + "/Sales/" + item.getId();
            db.document(documentPath)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        synchronized (myDataList) {
                            // Double-check that the item is still at the expected position before removing it.
                            if (!myDataList.isEmpty() && myDataList.size() > position && myDataList.get(position) == item) {
                                myDataList.remove(position);
                                notifyItemRemoved(position);
                                recalculateTotals(totalSalesCount, totalSalesPrice, totalSalesKilo);
                                Log.d(TAG, "Item successfully deleted from Firestore and RecyclerView");
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error deleting item from Firestore", e));
        }
    }

    private void recalculateTotals(TextView totalSalesCount, TextView totalSalesPrice, TextView totalSalesKilo) {
        int totalCount = 0;
        float totalPrice = 0;
        float totalKilo = 0;

        for (MyData data : myDataList) {
            totalCount++;
            totalPrice += data.getProductPrice();
            totalKilo += data.getProductWeight();
        }

        totalSalesCount.setText(String.valueOf(totalCount));
        totalSalesPrice.setText(String.format(Locale.getDefault(), "%.2f", totalPrice));
        totalSalesKilo.setText(String.format(Locale.getDefault(), "%.2f", totalKilo));
    }

}
