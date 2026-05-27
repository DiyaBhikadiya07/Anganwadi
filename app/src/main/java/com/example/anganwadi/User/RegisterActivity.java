package com.example.anganwadi.User;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.anganwadi.AuthService;
import com.example.anganwadi.R;
import com.example.anganwadi.SupabaseConfig;
import com.example.anganwadi.SupabaseStorageHelper;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import okhttp3.*;

import java.io.IOException;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends androidx.appcompat.app.AppCompatActivity {

    EditText edtName, edtEmail, edtPassword, edtPhone, edtCity, edtRepeatPassword;
    TextInputLayout emailLayout, phoneLayout, passwordLayout, repeatPasswordLayout;
    RadioGroup rgGender;
    Button btnRegister;
    CircleImageView imageView;
    ProgressBar progressBar;

    Uri imageUri;

    ActivityResultLauncher<Intent> galleryLauncher;

    // Pattern to check if email ends with @gmail.com
    private static final Pattern GMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@gmail\\.com$"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtCreatePassword);
        edtRepeatPassword = findViewById(R.id.edtRepeatPassword);
        edtPhone = findViewById(R.id.edtPhone);
        edtCity = findViewById(R.id.edtCity);
        rgGender = findViewById(R.id.rg);
        btnRegister = findViewById(R.id.btnRegister);
        imageView = findViewById(R.id.cvProfileMember);
        progressBar = findViewById(R.id.progressbar);

        emailLayout = findViewById(R.id.Emailtv);
        phoneLayout = findViewById(R.id.Phonetv);
        passwordLayout = findViewById(R.id.CreatePasstv);
        repeatPasswordLayout = findViewById(R.id.RepeatPasstv);

        // Image picker
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        imageView.setImageURI(imageUri);
                    }
                });

        imageView.setOnClickListener(v -> openGallery());
        findViewById(R.id.iv_openCam).setOnClickListener(v -> openGallery());

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void registerUser() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim().toLowerCase();
        String password = edtPassword.getText().toString().trim();
        String repeatPassword = edtRepeatPassword.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String city = edtCity.getText().toString().trim();

        // Reset errors
        emailLayout.setError(null);
        phoneLayout.setError(null);
        passwordLayout.setError(null);
        repeatPasswordLayout.setError(null);

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(phone) || TextUtils.isEmpty(city) || selectedGenderId == -1) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // ONLY GMAIL ACCEPTED - Reject everything else
        if (!GMAIL_PATTERN.matcher(email).matches()) {
            emailLayout.setError("Only Gmail addresses are allowed (example@gmail.com)");
            edtEmail.requestFocus();
            return;
        }

        // Additional check for common Gmail typos
        if (email.contains("@gmal.com") || email.contains("@gmali.com") ||
                email.contains("@gmil.com") || email.contains("@gnail.com") ||
                email.contains("@gamil.com") || email.contains("@gmeil.com")) {
            emailLayout.setError("Invalid email domain. Did you mean @gmail.com?");
            edtEmail.requestFocus();
            return;
        }

        // Phone validation (Exactly 10 digits)
        if (phone.length() != 10 || !TextUtils.isDigitsOnly(phone)) {
            phoneLayout.setError("Please enter a valid 10-digit phone number");
            edtPhone.requestFocus();
            return;
        }

        // Password validation (minimum 6 characters)
        if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            edtPassword.requestFocus();
            return;
        }

        if (!password.equals(repeatPassword)) {
            repeatPasswordLayout.setError("Passwords do not match");
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Select Profile Image First", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedGender = findViewById(selectedGenderId);
        String gender = selectedGender.getText().toString();

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // START AUTH REGISTER
        AuthService.register(email, password, new AuthService.AuthCallback() {
            @Override
            public void onSuccess() {
                String fileName = "profile_" + System.currentTimeMillis() + ".jpg";

                SupabaseStorageHelper.uploadImage(RegisterActivity.this, imageUri, fileName,
                        new SupabaseStorageHelper.UploadCallback() {
                            @Override
                            public void onSuccess(String imageUrl) {
                                saveUserToTable(name, email, phone, city, gender, imageUrl);
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                                    btnRegister.setEnabled(true);
                                    Toast.makeText(RegisterActivity.this, "Upload Error: " + error, Toast.LENGTH_LONG).show();
                                });
                            }
                        });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Register Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void saveUserToTable(String name, String email, String phone, String city, String gender, String imageUrl) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("email", email);
            json.put("phone", phone);
            json.put("city", city);
            json.put("gender", gender);
            json.put("profile_url", imageUrl);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/users")
                    .post(body)
                    .addHeader("apikey", SupabaseConfig.API_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Failed to save user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseBody = response.body() != null ? response.body().string() : "";
                    runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Registration successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            btnRegister.setEnabled(true);
                            Toast.makeText(RegisterActivity.this, "Error saving profile: " + response.code() + " " + responseBody, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
            });
        }
    }
}