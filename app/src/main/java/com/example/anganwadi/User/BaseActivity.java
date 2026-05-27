package com.example.anganwadi.User;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.example.anganwadi.User.LocaleHelper;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}
