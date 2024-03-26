package com.example.salesrecorder;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Storage1EmployeeFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private RecyclerView recyclerView;
    private CustomAdapter customAdapter;
    private List<MyData> myDataList;
    private TextView totalSalesCount, totalSalesPrice, totalSalesKilo;
    private int selectedPosition = -1;
    private ListenerRegistration registration;
    private Spinner mainProductSpinner; // Spinner for main products
    private Spinner subProductSpinner; // Spinner for subproducts
    private ArrayAdapter<String> subProductAdapter; // Adapter for subproducts
    private static final String TAG = "Storage1Fragment";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_storage1, container, false);
        // Initialize TextViews
        totalSalesCount = view.findViewById(R.id.total_sales_count);
        totalSalesPrice = view.findViewById(R.id.total_sales_price);
        totalSalesKilo = view.findViewById(R.id.total_sales_kilo);

        // Initialize and setup spinners
        mainProductSpinner = view.findViewById(R.id.filter_spinner);
        subProductSpinner = view.findViewById(R.id.filter_spinner2);
        ArrayAdapter<String> mainProductAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        mainProductAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mainProductSpinner.setAdapter(mainProductAdapter);


        subProductAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        subProductAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subProductSpinner.setAdapter(subProductAdapter);
        subProductSpinner.setVisibility(View.GONE); // Initially hide the sub product spinner

        mainProductSpinner.setOnItemSelectedListener(this);
        subProductSpinner.setOnItemSelectedListener(this);

        // Fetch and populate main product categories without adding "Select Product"
        fetchMainProducts(mainProductAdapter, subProductSpinner);
        //fetchDataAndUpdateAdapter();

        // Initialize RecyclerView and CustomAdapter
        recyclerView = view.findViewById(R.id.recyclerView1);
        myDataList = new ArrayList<>();
        customAdapter = new CustomAdapter(getContext(), myDataList);
        recyclerView.setAdapter(customAdapter);

        // Initialize RecyclerView and CustomAdapter
        recyclerView = view.findViewById(R.id.recyclerView1);
        myDataList = new ArrayList<>();
        customAdapter = new CustomAdapter(getContext(), myDataList);
        recyclerView.setAdapter(customAdapter);

        recyclerView.setAdapter(customAdapter);

        // Call this method to fetch and display the data
        fetchDataAndUpdateAdapter();

        // Firestore database instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch data for the current day
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Timestamp startOfDay = new Timestamp(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Timestamp startOfNextDay = new Timestamp(calendar.getTime());

        // Firestore query
        db.collection("storage1")
                .whereEqualTo("storageNumber", 1)
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThan("date", startOfNextDay)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        myDataList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Create a new MyData object with the document snapshot, which includes setting the ID
                            MyData myData = new MyData(document);
                            myData.setId(document.getId());
                        }
                        customAdapter.updateDataList(myDataList, totalSalesCount, totalSalesPrice, totalSalesKilo);
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
        // Set long click listener to show AlertDialog for deletion
        customAdapter.setOnItemLongClickListener(position -> {
            // Ensure the position is valid before attempting to delete.
            if (position < 0 || position >= myDataList.size()) {
                Log.e(TAG, "Cannot delete: Invalid position " + position);
                return true;
            }

            Log.d(TAG, "Long pressed on item: " + position);
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        Log.d(TAG, "Deleting item at position: " + position);
                        customAdapter.deleteItem(position, totalSalesCount, totalSalesPrice, totalSalesKilo);
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
            return true;
        });

        return view;
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.filter_spinner) {
            // Main product selected
            String selectedMainProduct = mainProductSpinner.getSelectedItem().toString();
            if (!selectedMainProduct.equals("Select Product")) {
                fetchSubProducts(selectedMainProduct, subProductAdapter, subProductSpinner);
            }
        } else if (parent.getId() == R.id.filter_spinner2) {
            // Sub-product selected
            String selectedSubProduct = subProductSpinner.getSelectedItem().toString();
            if (!selectedSubProduct.equals("Select Sub-product")) {
                String selectedMainProduct = mainProductSpinner.getSelectedItem().toString();
                if (!selectedMainProduct.equals("Select Product")) {
                    fetchDataBasedOnProductSelection(selectedMainProduct, selectedSubProduct);
                }
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
    @Override
    public void onResume() {
        super.onResume();
        // Ideally, you want to refresh your data based on some default or saved preference
        // For now, you might want to do nothing or set some default value to fetch data
    }
    @Override
    public void onStop() {
        super.onStop();
        if (registration != null) {
            registration.remove();  // Remove the listener when the fragment is stopped
        }
    }
    private void fetchMainProducts(final ArrayAdapter<String> mainProductAdapter, final Spinner subProductSpinner) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(TAG, "Fetching main categories for user: " + user.getUid());
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid()).collection("productCategories")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mainProductAdapter.clear();
                            mainProductAdapter.add("Select Product"); // Add initial selection option
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String categoryName = document.getString("name");
                                mainProductAdapter.add(categoryName);
                                Log.d(TAG, "Main category added: " + categoryName);
                            }
                            mainProductAdapter.notifyDataSetChanged();
                        } else {
                            Log.e(TAG, "Error getting main categories: ", task.getException());
                        }
                    });
        } else {
            Log.d(TAG, "No user logged in.");
        }
    }
    private void fetchSubProducts(final String categoryName, final ArrayAdapter<String> subProductAdapter, final Spinner subProductSpinner) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && !categoryName.equals("Select Product")) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Log.d(TAG, "Fetching subproducts for category name: " + categoryName);
            db.collection("users").document(user.getUid()).collection("productCategories")
                    .whereEqualTo("name", categoryName).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            final String categoryId = task.getResult().getDocuments().get(0).getId();
                            db.collection("users").document(user.getUid()).collection("productCategories")
                                    .document(categoryId).collection("subProducts").get()
                                    .addOnCompleteListener(subTask -> {
                                        if (subTask.isSuccessful()) {
                                            List<String> subProductNames = new ArrayList<>();
                                            subProductNames.add("Select Sub-product"); // Add initial selection option for sub-products
                                            for (QueryDocumentSnapshot document : subTask.getResult()) {
                                                String subProductName = document.getString("name");
                                                if (subProductName != null && !subProductName.isEmpty()) {
                                                    subProductNames.add(subProductName);
                                                }
                                            }
                                            subProductAdapter.clear();
                                            subProductAdapter.addAll(subProductNames);
                                            subProductAdapter.notifyDataSetChanged();
                                            subProductSpinner.setSelection(0, false); // Set the spinner to the first item
                                            subProductSpinner.setVisibility(View.VISIBLE);
                                        } else {
                                            Log.e(TAG, "Error fetching subproducts for category: " + categoryName, subTask.getException());
                                            subProductSpinner.setVisibility(View.GONE);
                                        }
                                    });
                        } else {
                            Log.e(TAG, "Error fetching category ID for name: " + categoryName, task.getException());
                            subProductSpinner.setVisibility(View.GONE);
                        }
                    });
        } else {
            subProductSpinner.setVisibility(View.GONE);
            Log.d(TAG, "User not logged in or 'Select Product' is chosen.");
        }
    }

    private void fetchDataAndUpdateAdapter() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String collectionPath = "users/" + user.getUid() + "/Sales";
            Query query = db.collection(collectionPath)
                    .orderBy("date", Query.Direction.DESCENDING);

            query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                myDataList.clear();
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    MyData myData = new MyData(documentSnapshot);
                    myData.setId(documentSnapshot.getId()); // Ensure you set the ID here
                    myDataList.add(myData);
                }
                customAdapter.updateDataList(myDataList, totalSalesCount, totalSalesPrice, totalSalesKilo);
            }).addOnFailureListener(e -> Log.e("SalesFragment", "Error fetching data", e));
        } else {
            Log.d("SalesFragment", "User is not authenticated. Cannot fetch data.");
        }
    }

    private void fetchDataBasedOnProductSelection(String mainProduct, String subProduct) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Detach any existing listener to prevent duplicate data
            if (registration != null) {
                registration.remove();
            }

            // Get the start and end of the current day for comparison
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.set(Calendar.HOUR_OF_DAY, 0);
            startCalendar.set(Calendar.MINUTE, 0);
            startCalendar.set(Calendar.SECOND, 0);
            startCalendar.set(Calendar.MILLISECOND, 0);
            Date startDate = startCalendar.getTime();

            Calendar endCalendar = Calendar.getInstance();
            endCalendar.add(Calendar.DAY_OF_MONTH, 1);
            Date endDate = endCalendar.getTime();

            // Set up a real-time listener for the selected products
            registration = db.collection("users").document(user.getUid())
                    .collection("Sales")
                    .whereEqualTo("mainProduct", mainProduct)
                    .whereEqualTo("subProduct", subProduct)
                    .whereGreaterThanOrEqualTo("date", startDate)
                    .whereLessThan("date", endDate)
                    .addSnapshotListener((querySnapshot, e) -> {
                        if (e != null) {
                            Log.e("SalesFragment", "Listen failed.", e);
                            return;
                        }
                        // Update adapter and recalculate totals
                        List<MyData> newDataList = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot) {
                            MyData myData = doc.toObject(MyData.class);
                            newDataList.add(myData);
                        }
                        customAdapter.updateDataList(newDataList, totalSalesCount, totalSalesPrice, totalSalesKilo);
                    });
        } else {
            Log.d("SalesFragment", "User is not authenticated. Cannot fetch data.");
        }
    }
    private void updateDataListAndCalculateTotals(QuerySnapshot queryDocumentSnapshots) {
        myDataList.clear();
        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
            MyData myData = documentSnapshot.toObject(MyData.class);
            if (myData != null) {
                myData.setId(documentSnapshot.getId());
                myDataList.add(myData);
            }
        }
        // Update the adapter and recalculate totals
        customAdapter.updateDataList(myDataList, totalSalesCount, totalSalesPrice, totalSalesKilo);

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Detach the listener when the view is destroyed
        if (registration != null) {
            registration.remove();
        }
    }
}