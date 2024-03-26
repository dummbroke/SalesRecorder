package com.example.salesrecorder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class EmployeeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);

        ImageButton imgBtnBT = findViewById(R.id.imgBtnBT);
        ImageButton imgBtnFish = findViewById(R.id.imgBtnFish);

        // Setting onClickListener for the Bluetooth ImageButton
        imgBtnBT.setOnClickListener(v -> {
            Intent bluetoothIntent = new Intent(EmployeeActivity.this, Bluetooth.class);
            startActivity(bluetoothIntent);
        });

        // Setting onClickListener for the Sales ImageButton
        imgBtnFish.setOnClickListener(v -> {
            Intent salesIntent = new Intent(EmployeeActivity.this, FishSales.class);
            startActivity(salesIntent);
        });
    }
}
