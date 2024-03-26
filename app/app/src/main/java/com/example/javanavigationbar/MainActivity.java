package com.example.javanavigationbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton imgBtnFish = findViewById(R.id.imgBtnFish);

        imgBtnFish.setOnClickListener(v -> {
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

        ImageButton imgBtnTutorial = findViewById(R.id.imgBtnTutorial);

        imgBtnTutorial.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Tutorial.class);
            startActivity(intent);
        });
    }
}
