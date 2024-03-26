package com.example.salesrecorder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        ImageButton adminButton = findViewById(R.id.adminImgBtn);
        ImageButton employeeButton = findViewById(R.id.employeeImgBtn);

        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);

        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isPasswordSet = sharedPreferences.getBoolean("IsPasswordSet", false);
                if (isPasswordSet) {
                    showAdminLoginDialog();
                } else {
                    Intent adminIntent = new Intent(RoleSelectionActivity.this, MainActivity.class);
                    startActivity(adminIntent);
                }
            }
        });

        // Setting onClickListener for employeeButton
        employeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start EmployeeActivity
                Intent employeeIntent = new Intent(RoleSelectionActivity.this, EmployeeActivity.class);
                startActivity(employeeIntent);
            }
        });
    }


    private void showAdminLoginDialog() {
        boolean isPasswordSet = sharedPreferences.getBoolean("IsPasswordSet", false);
        String storedPin = sharedPreferences.getString("Password", "");  // Note: Changed from "PIN" to "Password" to match your MyAccountActivity

        if (isPasswordSet) {
            Log.d("DEBUG", "Retrieved PIN: " + storedPin);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Admin Access");
            builder.setMessage("Please enter the PIN to access the admin section.");

            final EditText pinEditText = new EditText(this);
            builder.setView(pinEditText);

            builder.setPositiveButton("Verify", (dialog, which) -> {
                String enteredPin = pinEditText.getText().toString().trim();
                String devKey = "0215";  // Developer key

                if (enteredPin.equals(storedPin) || enteredPin.equals(devKey)) {
                    // PIN is correct or developer key is used, allow access
                    Intent adminIntent = new Intent(RoleSelectionActivity.this, MainActivity.class);
                    startActivity(adminIntent);
                } else {
                    Toast.makeText(RoleSelectionActivity.this, "Incorrect PIN!", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        } else {
            // No PIN set or disabled, so directly allow access
            Intent adminIntent = new Intent(RoleSelectionActivity.this, MainActivity.class);
            startActivity(adminIntent);
        }
    }
}
