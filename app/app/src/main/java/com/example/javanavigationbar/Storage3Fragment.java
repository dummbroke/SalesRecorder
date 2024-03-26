package com.example.javanavigationbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Storage3Fragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private RecyclerView recyclerView;
    private CustomAdapter customAdapter;
    private List<MyData> myDataList;
    private TextView totalSalesCount, totalSalesPrice, totalSalesKilo;
    private int selectedPosition = -1;
    private ListenerRegistration registration;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_storage3, container, false);

        totalSalesCount = view.findViewById(R.id.total_sales_count);
        totalSalesPrice = view.findViewById(R.id.total_sales_price);
        totalSalesKilo = view.findViewById(R.id.total_sales_kilo);

        Spinner coloredSpinner = view.findViewById(R.id.filter_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.productname,
                R.layout.color_spinner
        );
        adapter.setDropDownViewResource(R.layout.color_dropdown_spinner);
        coloredSpinner.setAdapter(adapter);
        coloredSpinner.setOnItemSelectedListener(this);

        recyclerView = view.findViewById(R.id.recyclerView1);
        myDataList = new ArrayList<>();
        customAdapter = new CustomAdapter(getContext(), myDataList);

        recyclerView.setAdapter(customAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Get the start and end of the current day in milliseconds
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date()); // Set to current date and time
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Timestamp startOfDay = new Timestamp(calendar.getTime());

        calendar.add(Calendar.DAY_OF_MONTH, 1); // Add 1 day
        Timestamp startOfNextDay = new Timestamp(calendar.getTime());

        db.collection("storage3")
                .whereEqualTo("storageNumber", 3)
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThan("date", startOfNextDay)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            myDataList.clear();
                            int totalCount = 0;
                            int totalPrice = 0;
                            int totalKilo = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                MyData myData = new MyData(document);  // Use the new constructor
                                myDataList.add(myData);
                                totalCount += 1; // Assuming each document represents one sale
                                totalPrice += myData.getProductPrice();
                                totalKilo += myData.getProductWeight();
                            }
                            customAdapter.updateDataList(myDataList);  // Update the data list in the adapter

                            // Update your TextViews
                            totalSalesCount.setText(String.valueOf(totalCount));
                            totalSalesPrice.setText(String.valueOf(totalPrice));
                            totalSalesKilo.setText(String.valueOf(totalKilo));
                        } else if (isAdded()) {
                            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                        }
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                    }
                });

        customAdapter.setOnItemLongClickListener(new CustomAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(int position) {
                Log.d("CustomAdapter", "Long click detected on position: " + position);  // Add this line

                selectedPosition = position;  // Store the position of the long pressed item
                // Get the selected item using selectedPosition
                MyData selectedItem = myDataList.get(selectedPosition);
                // Check if selectedItem or its ID is null
                if (selectedItem == null) {
                    Log.e("CustomAdapter", "Selected item is null");
                    return true;
                }
                if (selectedItem.getId() == null) {
                    Log.e("CustomAdapter", "Selected item's ID is null");
                    return true;
                }
                Log.d("CustomAdapter", "Selected item: " + selectedItem.getId());  // Add this line

                // Create an AlertDialog
                AlertDialog dialog = new AlertDialog.Builder(getContext())  // Change this line
                        .setTitle("Delete Data")
                        .setMessage("Are you sure you want to delete this data?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Delete the selected item from Firestore
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("products").document(selectedItem.getId())
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Remove the selected item from the list and notify the adapter
                                                myDataList.remove(selectedPosition);
                                                customAdapter.notifyItemRemoved(selectedPosition);

                                                // Reset selectedPosition
                                                selectedPosition = -1;

                                                Toast.makeText(getContext(), "Data deleted", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(Exception e) {
                                                Log.e("CustomAdapter", "Error deleting item: ", e);
                                                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .create();  // Add this line

                Log.d("CustomAdapter", "Showing dialog");  // Add this line
                dialog.show();  // Change this line

                return true;  // Return true to indicate that the long press event has been handled
            }
        });


        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {
        if (isResumed()) {  // Check if the fragment is currently visible
            String choice = adapterView.getItemAtPosition(i).toString();

            // Clear the data list and update the adapter immediately when a new product is selected
            myDataList.clear();
            customAdapter.notifyDataSetChanged();

            // Then execute the Firestore query
            updateQuery(choice);
        }
    }

    private void updateQuery(String productName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the start and end of the current day in milliseconds
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date()); // Set to current date and time
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Timestamp startOfDay = new Timestamp(calendar.getTime());

        calendar.add(Calendar.DAY_OF_MONTH, 1); // Add 1 day
        Timestamp startOfNextDay = new Timestamp(calendar.getTime());

        // Log the start of day, start of next day, and product name
        Log.d("updateQuery", "Start of day: " + startOfDay.toString());
        Log.d("updateQuery", "Start of next day: " + startOfNextDay.toString());
        Log.d("updateQuery", "Product name: " + productName);

        Query query;
        if (productName.equals("ALL")) {
            query = db.collection("storage3")
                    .whereEqualTo("storageNumber", 3)
                    .whereGreaterThanOrEqualTo("date", startOfDay)
                    .whereLessThan("date", startOfNextDay)
                    .orderBy("date", Query.Direction.DESCENDING);
        } else {
            query = db.collection("storage3")
                    .whereEqualTo("storageNumber", 3)
                    .whereEqualTo("productName", productName)
                    .whereGreaterThanOrEqualTo("date", startOfDay)
                    .whereLessThan("date", startOfNextDay)
                    .orderBy("date", Query.Direction.DESCENDING);
        }

        // If there's an existing listener, remove it before adding a new one
        if (registration != null) {
            registration.remove();
        }

        // Add a real-time listener to the query
        registration = query.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                // Log any error that occurred
                Log.e("updateQuery", "Query failed", e);
                return;
            }

            // Log the number of documents returned by the query
            Log.d("updateQuery", "Query results: " + querySnapshot.getDocuments().size() + " documents");

            myDataList.clear();
            int totalCount = 0;
            int totalPrice = 0;
            int totalKilo = 0;
            for (QueryDocumentSnapshot document : querySnapshot) {
                MyData myData = new MyData(document);  // Use the new constructor
                myDataList.add(myData);
                totalCount += 1; // Assuming each document represents one sale
                totalPrice += myData.getProductPrice();
                totalKilo += myData.getProductWeight();
            }
            customAdapter.updateDataList(myDataList);  // Update the data list in the adapter

            // Update your TextViews
            totalSalesCount.setText(String.valueOf(totalCount));
            totalSalesPrice.setText(String.valueOf(totalPrice));
            totalSalesKilo.setText(String.valueOf(totalKilo));
        });
    }



    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
    @Override
    public void onResume() {
        super.onResume();
        updateQuery("ALL");  // Fetch all data when the fragment becomes visible
    }
    @Override
    public void onStop() {
        super.onStop();
        if (registration != null) {
            registration.remove();  // Remove the listener when the fragment is stopped
        }
    }
}
