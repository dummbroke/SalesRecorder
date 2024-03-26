package com.example.javanavigationbar;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class FishSales extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    Storage1Fragment storage1Fragment = new Storage1Fragment();
    Storage2Fragment storage2Fragment = new Storage2Fragment();
    Storage3Fragment storage3Fragment = new Storage3Fragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish_sales);

        bottomNavigationView = findViewById(R.id.bottomNavView);

        getSupportFragmentManager().beginTransaction().replace(R.id.containerfish, storage1Fragment).commit();

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.storage1 -> {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containerfish, storage1Fragment).commit();
                    return true;
                }
                case R.id.storage2 -> {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containerfish, storage2Fragment).commit();
                    return true;
                }
                case R.id.storage3 -> {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containerfish, storage3Fragment).commit();
                    return true;
                }
            }
            return false;
        });
    }
}