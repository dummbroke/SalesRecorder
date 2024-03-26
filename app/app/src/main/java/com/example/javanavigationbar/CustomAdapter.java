package com.example.javanavigationbar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private Context context;
    private List<MyData> myDataList;  // Add this line
    private OnItemClickListener mListener;
    private OnItemLongClickListener mLongListener;

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

    public void updateDataList(List<MyData> newDataList) {
        this.myDataList = newDataList;
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row, parent, false);
        return new MyViewHolder(view, mListener, mLongListener);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

        Date date = myDataList.get(position).getDate();
        String formattedDate = date != null ? dateFormat.format(date) : "N/A";


        holder.product_id_txt.setText(String.valueOf(position + 1)); // Display the position + 1 (because position is 0-based)
        holder.product_name_txt.setText(myDataList.get(position).getProductName().toString());
        holder.product_price_txt.setText(String.valueOf(myDataList.get(position).getProductPrice()));
        holder.product_weight_txt.setText(String.valueOf(myDataList.get(position).getProductWeight()));
        holder.product_date_txt.setText(formattedDate);
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            clickListener.onItemClick(position);
                        }
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (longClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            return longClickListener.onItemLongClick(position)  ;
                        }
                    }
                    return false;
                }
            });
        }
    }
}
