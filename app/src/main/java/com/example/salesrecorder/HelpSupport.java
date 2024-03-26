package com.example.salesrecorder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class HelpSupport extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        ImageButton facebookButton = findViewById(R.id.facebookButton);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/profile.php?id=100066444800507"));
                startActivity(browserIntent);
            }
        });

        ImageButton instagramButton = findViewById(R.id.instagramButton);
        instagramButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/keenthomas15/?igshid=MzNlNGNkZWQ4Mg%3D%3D&fbclid=IwAR3LT03XdyuFdSBgpS7zgeaoAAnKPvQ9eHcZgPdqdCzhM6Guw-kA66zIBAY"));
                startActivity(browserIntent);
            }
        });

        ImageButton tiktokButton = findViewById(R.id.tiktokButton);
        tiktokButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiktok.com/@broketom?is_from_webapp=1&sender_device=pc"));
                startActivity(browserIntent);
            }
        });
    }
}