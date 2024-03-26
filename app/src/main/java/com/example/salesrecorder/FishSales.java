package com.example.salesrecorder;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class FishSales extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish_sales);

        Log.d("FishSales", "FishSales activity started");

        // Set the initial fragment to Storage1Fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerfish, new Storage1Fragment(), Storage1Fragment.class.getSimpleName())
                    .commit();
        }
    }
}
