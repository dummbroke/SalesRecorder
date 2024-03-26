package com.example.salesrecorder;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton imgBtnFish = findViewById(R.id.imgBtnFish);

        imgBtnFish.setOnClickListener(v -> {
            Log.d("App Crashed", "FishSales button clicked");
            Intent intent = new Intent(MainActivity.this, FishSales.class);
            startActivity(intent);
        });

        ImageButton imgBtnBluetooth = findViewById(R.id.imgBtnBT);

        imgBtnBluetooth.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Bluetooth.class);
            startActivity(intent);
        });

        ImageButton imgBtnGetData = findViewById(R.id.imgBtnData);

        imgBtnGetData.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GetData.class);
            startActivity(intent);
        });

        ImageButton imgBtnHelpSupport = findViewById(R.id.imgBtnHelpSupport);

        imgBtnHelpSupport.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HelpSupport.class);
            startActivity(intent);
        });

        ImageButton imgBtnAboutUs = findViewById(R.id.imgBtnAboutUs);

        imgBtnAboutUs.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutUs.class);
            startActivity(intent);
        });

        ImageButton imgBtnSettings = findViewById(R.id.imgBtnSettings);

        imgBtnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
            startActivity(intent);
        });

        ImageButton imgBtnMyAccount = findViewById(R.id.imgBtnMyAccount);

        imgBtnMyAccount.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyAccountActivity.class);
            startActivity(intent);
        });
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Application")
                .setMessage("Are you sure you want to close this application?")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", null)
                .show();
    }
}

