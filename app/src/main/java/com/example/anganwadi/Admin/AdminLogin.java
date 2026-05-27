package com.example.anganwadi.Admin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.anganwadi.Admin.AdminDashboard;
import com.example.anganwadi.R;
import com.example.anganwadi.BaseActivity;

public class AdminLogin extends BaseActivity {
    Button loginbtn;
    EditText edtUser, edtPassword;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        loginbtn = findViewById(R.id.btnLoginAdmin);
        edtUser = findViewById(R.id.edtUsernameAdmin);
        edtPassword = findViewById(R.id.edtPasswordAdmin);

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginAdmin();
            }

        });


    }

    private void LoginAdmin() {
        if (edtUser.getText().toString().equals("user") && edtPassword.getText().toString().equals("User@123")) {
            setAdminLoginStatus(this, true);
            Toast.makeText(AdminLogin.this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminLogin.this, AdminDashboard.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(AdminLogin.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
        }
    }

    public void setAdminLoginStatus(Context context, boolean isLoggedIn) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isAdminLoggedIn", isLoggedIn);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
