package com.example.salesrecorder;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<Category> {
    private Context context;
    private List<Category> categories;
    private static final String TAG = "CustomSpinnerAdapter";
    private OnItemLongClickListener onItemLongClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(Category category);
    }

    public CustomSpinnerAdapter(Context context, List<Category> categories) {
        super(context, android.R.layout.simple_spinner_item, categories);
        this.context = context;
        this.categories = categories;
        Log.d(TAG, "CustomSpinnerAdapter initialized with categories size: " + categories.size());
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }
        TextView textView = (TextView) convertView;
        Category category = getItem(position);
        if (category != null) {
            textView.setText(category.getName());
            Log.d(TAG, "Setting spinner view for category: " + category.getName());
        }

        convertView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null && position != 0) { // Avoid long press on default item
                onItemLongClickListener.onItemLongClick(getItem(position));
            }
            return true;
        });

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }
        TextView textView = (TextView) convertView;
        Category category = getItem(position);
        if (category != null) {
            textView.setText(category.getName());
            Log.d(TAG, "Setting spinner dropdown view for category: " + category.getName());
        }
        return convertView;
    }
}
