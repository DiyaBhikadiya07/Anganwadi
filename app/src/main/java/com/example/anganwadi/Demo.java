package com.example.anganwadi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.example.anganwadi.Admin.AdminLogin;
import com.example.anganwadi.User.LocaleHelper;
import com.example.anganwadi.User.LoginActivity;

public class Demo extends BaseActivity {

    Button signup, signup1, btnSelectLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAppLanguage();
        setContentView(R.layout.activity_demo);

        signup = findViewById(R.id.btnSignup);
        signup1 = findViewById(R.id.btnSignup1);
        btnSelectLanguage = findViewById(R.id.SelectLanguage);

        signup.setOnClickListener(v ->
                startActivity(new Intent(Demo.this, LoginActivity.class)));

        signup1.setOnClickListener(v ->
                startActivity(new Intent(Demo.this, AdminLogin.class)));

        btnSelectLanguage.setOnClickListener(v -> showLanguageDialog());
    }

    private void setAppLanguage() {
        SharedPreferences pref = getSharedPreferences("LANG", Context.MODE_PRIVATE);
        // String code = pref.getString("code", "en"); 
    }

    private void showLanguageDialog() {
        final String[] languages = {"English", "Hindi", "Gujarati"};
        final String[] codes = {"en", "hi", "gu"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_language));
        builder.setItems(languages, (dialog, which) -> {
            SharedPreferences.Editor editor =
                    getSharedPreferences("LANG", Context.MODE_PRIVATE).edit();
            editor.putString("code", codes[which]);
            editor.apply();

            LocaleHelper.setLocale(Demo.this, codes[which]);
            recreate();
        });
        builder.show();
    }
}
