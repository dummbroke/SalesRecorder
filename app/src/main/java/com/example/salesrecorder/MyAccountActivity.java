package com.example.salesrecorder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyAccountActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Button setPasswordBtn;
    private Button changePasswordBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);

        ImageView profileImage = findViewById(R.id.profileImage);
        TextView emailTextView = findViewById(R.id.emailTextView);
        setPasswordBtn = findViewById(R.id.setPasswordBtn);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        Button logoutBtn = findViewById(R.id.logoutBtn);

        updateUIBasedOnPasswordState();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            emailTextView.setText("User not signed in");
            finish();
            return;
        }

        String email = user.getEmail();
        Uri photoUrl = user.getPhotoUrl();

        emailTextView.setText(email);

        if (photoUrl != null) {
            Glide.with(this)
                    .load(photoUrl)
                    .into(profileImage);
        } else {
            emailTextView.setText("User not signed in");
        }

        setPasswordBtn.setOnClickListener(v -> {
            if ("Set Password".equals(setPasswordBtn.getText().toString())) {
                showSetPasswordDialog();
            } else {
                showResetPasswordDialog();
            }
        });

        changePasswordBtn.setOnClickListener(v -> showChangePasswordDialog());

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmationDialog();
            }
        });

    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(MyAccountActivity.this)
                .setTitle("Log Out") // Set the title of the dialog
                .setMessage("Are you sure you want to log out?") // Set the message to display
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout(); // Call logout method if user confirms
                    }
                })
                .setNegativeButton("Cancel", null) // Dismiss the dialog if user cancels
                .show(); // Show the dialog
    }

    private void logout() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Clear the saved login state
        SharedPreferences.Editor editor = getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        // Redirect to LoginActivity
        Intent intent = new Intent(MyAccountActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Finish the current activity so the user can't go back to it without logging in again
    }

    private void updateUIBasedOnPasswordState() {
        boolean isPasswordSet = sharedPreferences.getBoolean("IsPasswordSet", false);
        if (isPasswordSet) {
            setPasswordBtn.setText("Reset Password");

            changePasswordBtn.setVisibility(View.VISIBLE);
        } else {
            setPasswordBtn.setText("Set Password");
            changePasswordBtn.setVisibility(View.GONE);
        }
    }

    private void showSetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View customLayout = inflater.inflate(R.layout.dialog_set_password, null);
        builder.setView(customLayout);

        EditText pinEditText = customLayout.findViewById(R.id.pinEditText);
        EditText confirmPinEditText = customLayout.findViewById(R.id.confirmPinEditText);
        CheckBox showPinCheckBox = customLayout.findViewById(R.id.showPinCheckBox);
        Button setPassConfirmBtn = customLayout.findViewById(R.id.setPassConfirmBtn);

        AlertDialog dialog = builder.create();
        dialog.show();

        setPassConfirmBtn.setOnClickListener(v -> {
            String pin = pinEditText.getText().toString().trim();
            String confirmPin = confirmPinEditText.getText().toString().trim();

            if (!pin.isEmpty() && pin.equals(confirmPin)) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("IsPasswordSet", true);
                editor.putString("Password", pin);
                editor.apply();
                Toast.makeText(MyAccountActivity.this, "Password set successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                updateUIBasedOnPasswordState();
            } else {
                Toast.makeText(MyAccountActivity.this, "Passwords do not match or are empty!", Toast.LENGTH_SHORT).show();
            }
        });

        showPinCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                pinEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                confirmPinEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                pinEditText.setInputType(129);  // 129 is InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                confirmPinEditText.setInputType(129);
            }
        });

    }
    private void showResetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View customLayout = inflater.inflate(R.layout.dialog_confirm_password, null);
        builder.setView(customLayout);

        EditText confirmPasswordEditText = customLayout.findViewById(R.id.confirmPasswordEditText);
        CheckBox showPinCheckBox = customLayout.findViewById(R.id.showPinCheckBox);
        Button confirmBtn = customLayout.findViewById(R.id.setPassConfirmBtn);
        TextView forgotPinTextView = customLayout.findViewById(R.id.forgotPinTextView);
        forgotPinTextView.setOnClickListener(v -> showResetPinDialog());


        AlertDialog dialog = builder.create();
        dialog.show();

        confirmBtn.setOnClickListener(v -> {
            String enteredPassword = confirmPasswordEditText.getText().toString().trim();
            String storedPassword = sharedPreferences.getString("Password", null);

            if (enteredPassword.equals(storedPassword)) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("IsPasswordSet", false);  // Just disable the password, don't remove it
                editor.apply();
                Toast.makeText(MyAccountActivity.this, "Password disabled successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                updateUIBasedOnPasswordState();
            }
            else {
                Toast.makeText(MyAccountActivity.this, "Incorrect password!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View customLayout = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(customLayout);

        EditText oldPinEditText = customLayout.findViewById(R.id.oldpinEditText);
        oldPinEditText.setHint("Enter Current PIN");
        EditText newPinEditText = customLayout.findViewById(R.id.pinEditText);
        newPinEditText.setHint("Enter New PIN");
        EditText confirmPinEditText = customLayout.findViewById(R.id.confirmPinEditText);
        CheckBox showPinCheckBox = customLayout.findViewById(R.id.showPinCheckBox);
        Button setChangePassConfirmBtn = customLayout.findViewById(R.id.setChangePassConfirmBtn);
        TextView forgotPinTextView = customLayout.findViewById(R.id.forgotPinTextView);
        forgotPinTextView.setOnClickListener(v -> showResetPinDialog());

        AlertDialog dialog = builder.create();
        dialog.show();

        showPinCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                newPinEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                confirmPinEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                newPinEditText.setInputType(129);  // 129 is InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                confirmPinEditText.setInputType(129);
            }
        });

        setChangePassConfirmBtn.setOnClickListener(v -> {
            String oldPin = oldPinEditText.getText().toString().trim();
            String newPin = newPinEditText.getText().toString().trim();
            String confirmPin = confirmPinEditText.getText().toString().trim();
            String storedPassword = sharedPreferences.getString("Password", null);

            if (oldPin.equals(storedPassword)) {
                if (!newPin.isEmpty() && newPin.equals(confirmPin)) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Password", newPin);
                    editor.apply();
                    Toast.makeText(MyAccountActivity.this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(MyAccountActivity.this, "New passwords do not match or are empty!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MyAccountActivity.this, "Old password is incorrect!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showResetPinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset PIN");
        builder.setMessage("Please enter the reset code to reset the PIN.");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String resetCode = input.getText().toString().trim();
            if ("0000".equals(resetCode)) {  // Replace "0000" with the actual reset code
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("IsPasswordSet");
                editor.remove("Password");
                editor.apply();
                Toast.makeText(MyAccountActivity.this, "PIN reset successfully!", Toast.LENGTH_SHORT).show();
                updateUIBasedOnPasswordState();
            } else {
                Toast.makeText(MyAccountActivity.this, "Invalid reset code!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

}
