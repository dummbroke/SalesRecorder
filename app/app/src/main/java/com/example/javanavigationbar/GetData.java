package com.example.javanavigationbar;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class GetData extends AppCompatActivity {

    private Button btnChooseDate;
    private TextView txtDisplayDate;
    private Spinner filterSpinner1;
    private TextView totalSalesKilo1, totalSalesPrice1,
            totalSalesKilo2, totalSalesPrice2,
            totalSalesKilo3, totalSalesPrice3;
    private int year, month, day;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_data);

        Toolbar toolbar = findViewById(R.id.menuToolbar);
        setSupportActionBar(toolbar);

        btnChooseDate = findViewById(R.id.btnChooseDate);
        txtDisplayDate = findViewById(R.id.txtDisplayDate);
        filterSpinner1 = findViewById(R.id.filter_spinner1);

        totalSalesKilo1 = findViewById(R.id.total_sales_kilo1);
        totalSalesPrice1 = findViewById(R.id.total_sales_price1);

        totalSalesKilo2 = findViewById(R.id.total_sales_kilo2);
        totalSalesPrice2 = findViewById(R.id.total_sales_price2);

        totalSalesKilo3 = findViewById(R.id.total_sales_kilo3);
        totalSalesPrice3 = findViewById(R.id.total_sales_price3);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.productname, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner1.setAdapter(adapter);

        filterSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Check if a date has been chosen
                if (!txtDisplayDate.getText().toString().isEmpty()) {
                    // Parse the date from the text view
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    try {
                        Date chosenDate = sdf.parse(txtDisplayDate.getText().toString());
                        // Update the data based on the chosen date and the new selection in the spinner
                        updateData(chosenDate);
                    } catch (ParseException e) {
                        // If the date is not in the correct format, handle the error here. For example, you could show a Toast to the user:
                        Toast.makeText(GetData.this, "Date is not in the correct format. Please select a date.", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });


        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        btnChooseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        GetData.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // Create a Date object from the chosen date
                                Calendar chosenDate = Calendar.getInstance();
                                chosenDate.set(year, month, dayOfMonth);
                                Date date = chosenDate.getTime();

                                // Format the Date object to a string in the "yyyy-MM-dd" format
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                String formattedDate = sdf.format(date);

                                // Display the date in the "yyyy-MM-dd" format in the UI
                                txtDisplayDate.setText(formattedDate);

                                // Use date for querying Firestore
                                updateData(date);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

    }

    private void updateData(Date chosenDate) {
        TimeZone timeZone = TimeZone.getDefault();
        Calendar chosenCalendar = Calendar.getInstance(timeZone);
        chosenCalendar.setTime(chosenDate);
        chosenCalendar.set(Calendar.HOUR_OF_DAY, 0);
        chosenCalendar.set(Calendar.MINUTE, 0);
        chosenCalendar.set(Calendar.SECOND, 0);
        Date startOfDayDate = chosenCalendar.getTime();

        chosenCalendar.add(Calendar.DAY_OF_MONTH, 1);
        Date startOfNextDayDate = chosenCalendar.getTime();

        String selectedItem = (String) filterSpinner1.getSelectedItem();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d("GetData", "Selected item: " + selectedItem);
        Log.d("GetData", "Start of day: " + startOfDayDate.toString());
        Log.d("GetData", "Start of next day: " + startOfNextDayDate.toString());

        // Create the base queries
        Query query1 = db.collection("storage1")
                .whereGreaterThanOrEqualTo("date", startOfDayDate)
                .whereLessThan("date", startOfNextDayDate);

        Query query2 = db.collection("storage2")
                .whereGreaterThanOrEqualTo("date", startOfDayDate)
                .whereLessThan("date", startOfNextDayDate);

        Query query3 = db.collection("storage3")
                .whereGreaterThanOrEqualTo("date", startOfDayDate)
                .whereLessThan("date", startOfNextDayDate);

        // If the selected item is not "All", add the product name condition to the queries
        if (!selectedItem.equals("All")) {
            query1 = query1.whereEqualTo("productName", selectedItem);
            query2 = query2.whereEqualTo("productName", selectedItem);
            query3 = query3.whereEqualTo("productName", selectedItem);
        }

        Log.d("GetData", "Query 1: " + query1.toString());
        Log.d("GetData", "Query 2: " + query2.toString());
        Log.d("GetData", "Query 3: " + query3.toString());

        // Call a function to execute the queries and update the UI
        executeQueryAndUpdateUI(query1, totalSalesKilo1, totalSalesPrice1);
        executeQueryAndUpdateUI(query2, totalSalesKilo2, totalSalesPrice2);
        executeQueryAndUpdateUI(query3, totalSalesKilo3, totalSalesPrice3);
    }

    private void executeQueryAndUpdateUI(Query query, TextView totalSalesKilo, TextView totalSalesPrice) {
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int totalKilo = 0;
                    int totalPrice = 0;

                    Log.d("GetData", "Number of documents: " + task.getResult().size());

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("GetData", "Document data: " + document.getData());
                        totalKilo += document.getLong("productWeight").intValue();
                        totalPrice += document.getLong("productPrice").intValue();
                    }

                    totalSalesKilo.setText(String.valueOf(totalKilo));
                    totalSalesPrice.setText(String.valueOf(totalPrice));
                } else {
                    Log.d("GetData", "Error getting documents: ", task.getException());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d("GetData", "Error: ", e);
            }
        });
    }
}

