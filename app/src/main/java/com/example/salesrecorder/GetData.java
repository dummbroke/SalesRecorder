package com.example.salesrecorder;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GetData extends AppCompatActivity {
    private Button btnChooseDate;
    private TextView txtDisplayDate;
    private Spinner filterSpinner1;
    private Spinner filterSpinner2;
    private TextView totalSalescount, totalSalesKilo, totalSalesPrice;
    private static final String TAG = "GetData";
    private RecyclerView recyclerViewSales;
    private SalesAdapter salesAdapter;
    private List<MyData> salesDataList;
    private static final int CREATE_FILE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_data);

        Toolbar toolbar = findViewById(R.id.menuToolbar);
        setSupportActionBar(toolbar);

        recyclerViewSales = findViewById(R.id.recyclerView1);
        recyclerViewSales.setLayoutManager(new LinearLayoutManager(this));
        salesDataList = new ArrayList<>();
        salesAdapter = new SalesAdapter(salesDataList);
        recyclerViewSales.setAdapter(salesAdapter);

        btnChooseDate = findViewById(R.id.btnChooseDate);
        txtDisplayDate = findViewById(R.id.txtDisplayDate);
        filterSpinner1 = findViewById(R.id.filter_spinner1);
        filterSpinner2 = findViewById(R.id.filter_spinner2);

        totalSalescount = findViewById(R.id.total_sales_count1);
        totalSalesKilo = findViewById(R.id.total_sales_kilo1);
        totalSalesPrice = findViewById(R.id.total_sales_price1);

        filterSpinner2.setVisibility(View.GONE); // Initially hide the subcategory spinner

        ArrayAdapter<String> mainCategoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        mainCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner1.setAdapter(mainCategoryAdapter);

        ArrayAdapter<String> subCategoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner2.setAdapter(subCategoryAdapter);

        fetchMainCategories(mainCategoryAdapter);
        filterSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMainCategory = parent.getItemAtPosition(position).toString();
                fetchSubCategories(selectedMainCategory, subCategoryAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                filterSpinner2.setVisibility(View.GONE);
            }
        });
        btnChooseDate.setOnClickListener(v -> showDatePickerDialog());
        Button btnExportData = findViewById(R.id.btnExportData);
        btnExportData.setOnClickListener(v -> {
            if (salesDataList.isEmpty()) {
                Toast.makeText(this, "No data available to export.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate the CSV content and create the file
            String selectedSubProduct = filterSpinner2.getSelectedItem().toString();
            String selectedDate = txtDisplayDate.getText().toString();
            String csvContent = generateCsvContent();
            if (!csvContent.isEmpty() && !selectedSubProduct.equals("Select Sub-product") && !selectedDate.isEmpty()) {
                createFileForDownload(csvContent, selectedSubProduct, selectedDate);
            } else {
                Toast.makeText(this, "Please select a sub-product and date.", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                GetData.this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year1);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
                    txtDisplayDate.setText(selectedDate);
                    fetchSalesData(selectedDate);
                }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(new Date().getTime()); // Restrict future dates
        datePickerDialog.show();
    }


    private void fetchSalesData(String selectedDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date;
        try {
            date = dateFormat.parse(selectedDate);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing selected date", e);
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d(TAG, "User is not authenticated.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        String mainProductName = filterSpinner1.getSelectedItem().toString();
        String subProductName = filterSpinner2.getSelectedItem().toString();

        if (user != null && !mainProductName.equals("Select Product") && !subProductName.equals("Select Sub-product")) {
            fetchStorageSalesData(db, userId, mainProductName, subProductName, date, "Sales");
        } else {
            Log.d(TAG, "Please select both a main product and a sub product.");
        }
    }

    private void fetchStorageSalesData(FirebaseFirestore db, String userId, String mainProductName, String subProductName, Date date, String storagePath) {
        Calendar startOfDay = Calendar.getInstance();
        startOfDay.setTime(date);
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);

        Calendar endOfDay = Calendar.getInstance();
        endOfDay.setTime(date);
        endOfDay.add(Calendar.DAY_OF_MONTH, 1);

        Log.d(TAG, "Fetching data for " + storagePath + " from " + startOfDay.getTime() + " to " + endOfDay.getTime());

        db.collection("users").document(userId).collection(storagePath)
                .whereEqualTo("mainProduct", mainProductName)
                .whereEqualTo("subProduct", subProductName)
                .whereGreaterThanOrEqualTo("date", startOfDay.getTime())
                .whereLessThan("date", endOfDay.getTime())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalKilos = 0;
                    double totalPrice = 0;
                    List<MyData> fetchedSalesData = new ArrayList<>(); // Create a list to hold the fetched data

                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        MyData myData = documentSnapshot.toObject(MyData.class);
                        if (myData != null) {
                            totalKilos += myData.getProductWeight();
                            totalPrice += myData.getProductPrice();
                            fetchedSalesData.add(myData); // Add the data to the list
                        }
                    }
                    displaySalesData(totalKilos, totalPrice, fetchedSalesData); // Pass the fetched data list
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching data for " + storagePath, e));
    }

    private void displaySalesData(double totalKilos, double totalPrice, List<MyData> fetchedSalesData) {
        // Update the total kilos and total price
        totalSalesKilo.setText(String.format(Locale.getDefault(), "%.2f kg", totalKilos));
        totalSalesPrice.setText(String.format(Locale.getDefault(), "%.2f", totalPrice));

        // Update the sales data list and notify the adapter
        salesDataList.clear();
        salesDataList.addAll(fetchedSalesData);
        salesAdapter.notifyDataSetChanged();

        // Update the total sales count
        totalSalescount.setText(String.valueOf(salesDataList.size())); // Update the total count
    }

    private void updateRecyclerViewWithData(List<MyData> salesData) {
        // Assuming you have a class SalesAdapter which extends RecyclerView.Adapter
        salesAdapter.setSalesData(salesData);
        salesAdapter.notifyDataSetChanged();
    }

    private void fetchMainCategories(final ArrayAdapter<String> mainCategoryAdapter) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(TAG, "Fetching main categories for user: " + user.getUid());
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid()).collection("productCategories")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mainCategoryAdapter.clear();
                            mainCategoryAdapter.add("Select Product"); // Add initial selection option
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String categoryName = document.getString("name");
                                mainCategoryAdapter.add(categoryName);
                                Log.d(TAG, "Main category added: " + categoryName);
                            }
                            mainCategoryAdapter.notifyDataSetChanged();
                        } else {
                            Log.e(TAG, "Error getting main categories: ", task.getException());
                        }
                    });
        } else {
            Log.d(TAG, "No user logged in.");
        }
    }
    private void fetchSubCategories(final String categoryName, final ArrayAdapter<String> subCategoryAdapter) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && !categoryName.equals("Select Product")) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Log.d(TAG, "Fetching subproducts for category name: " + categoryName);
            db.collection("users").document(user.getUid()).collection("productCategories")
                    .whereEqualTo("name", categoryName).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // We can only use 'final' or effectively final variables inside the lambda, hence the new variable
                            final String categoryId = task.getResult().getDocuments().get(0).getId();
                            db.collection("users").document(user.getUid()).collection("productCategories")
                                    .document(categoryId).collection("subProducts").get()
                                    .addOnCompleteListener(subTask -> {
                                        if (subTask.isSuccessful()) {
                                            List<String> subProductNames = new ArrayList<>();
                                            for (QueryDocumentSnapshot document : subTask.getResult()) {
                                                String subProductName = document.getString("name");
                                                if (subProductName != null && !subProductName.isEmpty()) {
                                                    subProductNames.add(subProductName);
                                                }
                                            }
                                            if (!subProductNames.isEmpty()) {
                                                subCategoryAdapter.clear();
                                                subCategoryAdapter.addAll(subProductNames);
                                                subCategoryAdapter.notifyDataSetChanged();
                                                filterSpinner2.setVisibility(View.VISIBLE);
                                            } else {
                                                Log.d(TAG, "No subproducts found for category ID: " + categoryId);
                                                filterSpinner2.setVisibility(View.GONE);
                                            }
                                        } else {
                                            Log.e(TAG, "Error fetching subproducts for category ID: " + categoryId, subTask.getException());
                                        }
                                    });
                        } else {
                            Log.e(TAG, "Error fetching category ID for name: " + categoryName, task.getException());
                            filterSpinner2.setVisibility(View.GONE);
                        }
                    });
        } else {
            filterSpinner2.setVisibility(View.GONE);
            Log.d(TAG, "User not logged in or 'Select Product' is chosen.");
        }
    }
    private void createFileForDownload(String csvContent, String subProductName, String selectedDate) {
        // Replace slashes in dates with dashes or other characters allowed in file names
        String safeDate = selectedDate.replace("/", "-");
        String fileName = subProductName + "_Sales_" + safeDate + ".csv";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        // Start the intent and wait for the result in onActivityResult
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);

        // Save the CSV content to be used after the file is created
        this.csvContent = csvContent;
    }
    // Member variable to hold the CSV content
    private String csvContent;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    // Write the CSV content to the selected file
                    writeCsvToUri(uri, csvContent);
                    csvContent = null; // Clear the content once written
                }
            }
        }
    }

    private void writeCsvToUri(Uri uri, String csvContent) {
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                outputStream.write(csvContent.getBytes());
                outputStream.close();
                Toast.makeText(this, "CSV file saved successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Unable to open file output stream.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error writing CSV file", e);
            Toast.makeText(this, "Error saving CSV file.", Toast.LENGTH_SHORT).show();
        }
    }
    private String generateCsvContent() {
        StringBuilder csvBuilder = new StringBuilder();
        // Header row without the Time column
        csvBuilder.append("No,SubProduct,Date,Price,Kilo\n");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        double totalKilo = 0;
        double totalPrice = 0;

        for (int i = 0; i < salesDataList.size(); i++) {
            MyData data = salesDataList.get(i);
            String date = "\"" + dateFormat.format(data.getDate()) + "\"";

            // Update the totals
            totalKilo += data.getProductWeight();
            totalPrice += data.getProductPrice();

            // Row in the CSV without time
            csvBuilder.append(String.format(Locale.getDefault(), "%d,%s,%s,%.2f,%.2f\n",
                    (i + 1),
                    data.getSubProduct(),
                    date,
                    data.getProductPrice(),
                    data.getProductWeight()));
        }

        // Append totals at the end of CSV content with empty cells for 'No' and 'SubProduct'
        csvBuilder.append(String.format(Locale.getDefault(), ",,Total,%.2f,%.2f\n",
                totalPrice,
                totalKilo));

        return csvBuilder.toString();
    }
}
