package com.example.anganwadi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.example.anganwadi.Admin.AdminDashboard;


public class SplashActivity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        imageView = findViewById(R.id.image);

        // Fixed deprecated Handler constructor
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                init();

            }
        }, 4000);
    }

    private void init() {
        if (isAdminLoggedIn()) {
            Intent intent = new Intent(SplashActivity.this, AdminDashboard.class);
            startActivity(intent);
            finish();
        } else {
            Intent i = new Intent(SplashActivity.this, Demo.class);
            startActivity(i);
            finish();
        }
    }

    private boolean isAdminLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isAdminLoggedIn", false);
    }
}
