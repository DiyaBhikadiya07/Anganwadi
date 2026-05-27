package com.example.anganwadi.User;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.anganwadi.BaseActivity;
import com.example.anganwadi.R;
import com.example.anganwadi.SupabaseConfig;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpdatePasswordActivity extends BaseActivity {

    private EditText edtNewPassword, edtConfirmPassword;
    private Button btnUpdatePassword;
    private ProgressBar progressBar;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        progressBar = findViewById(R.id.progressbar);

        handleIntent(getIntent());

        btnUpdatePassword.setOnClickListener(v -> updatePassword());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            String uriString = data.toString();
            Log.d("UpdatePassword", "Opening URL: " + uriString);

            // 1. Try manual extraction from full string (most reliable for fragments)
            if (uriString.contains("access_token=")) {
                accessToken = extractParam(uriString, "access_token");
            }

            // 2. Try fragment API
            if (accessToken == null) {
                String fragment = data.getFragment();
                if (fragment != null) {
                    accessToken = extractParamFromSection(fragment, "access_token");
                }
            }

            // 3. Try query parameters
            if (accessToken == null) {
                accessToken = data.getQueryParameter("access_token");
            }

            if (accessToken != null) {
                accessToken = accessToken.trim();
                Log.d("UpdatePassword", "Token found successfully");
            }
        }

        if (accessToken == null) {
            Toast.makeText(this, "Session link invalid. Please request a new reset email.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private String extractParam(String url, String param) {
        String pattern = param + "=";
        if (url.contains(pattern)) {
            try {
                int start = url.indexOf(pattern) + pattern.length();
                int end = url.indexOf("&", start);
                if (end == -1) end = url.length();
                return url.substring(start, end);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private String extractParamFromSection(String section, String param) {
        try {
            String[] parts = section.split("&");
            for (String part : parts) {
                if (part.startsWith(param + "=")) {
                    return part.substring((param + "=").length());
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private void updatePassword() {
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword) || newPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (accessToken == null) {
            Toast.makeText(this, "Session expired. Request a new link.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        btnUpdatePassword.setEnabled(false);

        updatePasswordInSupabase(newPassword);
    }

    private void updatePasswordInSupabase(String newPassword) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();
            json.put("password", newPassword);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/auth/v1/user")
                    .put(body)
                    .addHeader("apikey", SupabaseConfig.API_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        btnUpdatePassword.setEnabled(true);
                        Toast.makeText(UpdatePasswordActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    final String responseBody = response.body() != null ? response.body().string() : "";
                    final int code = response.code();
                    
                    runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            Toast.makeText(UpdatePasswordActivity.this, "Password updated successfully!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(UpdatePasswordActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            btnUpdatePassword.setEnabled(true);
                            String message = "Link may be expired or used.";
                            try {
                                JSONObject errorJson = new JSONObject(responseBody);
                                message = errorJson.optString("message", errorJson.optString("msg", message));
                            } catch (Exception ignored) {}
                            
                            // Show detailed error for debugging
                            Toast.makeText(UpdatePasswordActivity.this, "Error (" + code + "): " + message, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            btnUpdatePassword.setEnabled(true);
        }
    }
}
