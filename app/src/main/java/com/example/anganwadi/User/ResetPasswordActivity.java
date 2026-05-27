package com.example.anganwadi.User;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.anganwadi.AuthService;
import com.example.anganwadi.R;
import com.example.anganwadi.BaseActivity;

public class ResetPasswordActivity extends BaseActivity {

    EditText edtReset;
    Button btnReset;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        edtReset = findViewById(R.id.edtUserEmail);
        btnReset = findViewById(R.id.btnReset);
        progressBar = findViewById(R.id.progressbar);

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String email = edtReset.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        btnReset.setEnabled(false);

        AuthService.resetPassword(email, new AuthService.AuthCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(ResetPasswordActivity.this, "Reset link sent to your email", Toast.LENGTH_LONG).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    btnReset.setEnabled(true);
                    Toast.makeText(ResetPasswordActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
