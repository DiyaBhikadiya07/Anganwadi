package com.example.anganwadi.User;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.example.anganwadi.BaseActivity;
import com.example.anganwadi.R;
import com.example.anganwadi.SupabaseConfig;
import com.example.anganwadi.SupabaseStorageHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileActivity extends BaseActivity {

    TextView edtName, edtEmail, edtPhone, edtCity;
    CircleImageView cvProfileImage;
    Button btnUpdate, btnLogout;
    ProgressBar progressBar;
    Uri profilePicUri;

    private String userEmail;
    private String currentImageUrl;
    private ImageView ivBack;

    private ActivityResultLauncher<Intent> galleryLauncher;

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_exit, null);
        builder.setView(dialogView);

        TextView title = dialogView.findViewById(R.id.text_title);
        title.setText(R.string.msg_logout);

        final AlertDialog dialog = builder.create();

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogView.setBackgroundResource(R.drawable.dialog_bg);

        Button btnYes = dialogView.findViewById(R.id.btn_yes);
        Button btnNo = dialogView.findViewById(R.id.btn_no);

        btnYes.setBackgroundColor(getResources().getColor(R.color.Accent));
        btnNo.setBackgroundColor(getResources().getColor(R.color.Accent));

        btnYes.setText(getString(R.string.yes));
        btnNo.setText(getString(R.string.no));

        btnYes.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent setupIntent = new Intent(ProfileActivity.this, LoginActivity.class);
            Toast.makeText(getBaseContext(), "Logged Out", Toast.LENGTH_LONG).show();
            setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(setupIntent);
            dialog.dismiss();
            finishAffinity();
        });

        btnNo.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        edtName = findViewById(R.id.edtName);
        ivBack = findViewById(R.id.ivBack);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtCity = findViewById(R.id.edtCity);
        cvProfileImage = findViewById(R.id.cvProfileMember);
        btnUpdate = findViewById(R.id.btnRegister);
        btnLogout = findViewById(R.id.logoutButton);
        progressBar = findViewById(R.id.progressbar);

        // Make email field unchangeable
        edtEmail.setEnabled(false);
        edtEmail.setFocusable(false);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        userEmail = prefs.getString("userEmail", "");

        if (TextUtils.isEmpty(userEmail)) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ivBack.setOnClickListener(v -> onBackPressed());

        loadUserProfile();

        btnUpdate.setOnClickListener(view -> updateProfile());
        btnLogout.setOnClickListener(view -> showLogoutDialog());
        cvProfileImage.setOnClickListener(v -> selectProfileImage());

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        profilePicUri = result.getData().getData();
                        cvProfileImage.setImageURI(profilePicUri);
                    }
                });
    }

    private void loadUserProfile() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/users?email=eq." + userEmail)
                .get()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String body = response.body().string();
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    try {
                        JSONArray array = new JSONArray(body);
                        if (array.length() > 0) {
                            JSONObject user = array.getJSONObject(0);
                            edtName.setText(user.optString("name"));
                            edtEmail.setText(user.optString("email"));
                            edtPhone.setText(user.optString("phone"));
                            edtCity.setText(user.optString("city"));
                            currentImageUrl = user.optString("profile_url");

                            if (!TextUtils.isEmpty(currentImageUrl)) {
                                Picasso.get().load(currentImageUrl).placeholder(R.drawable.profile_member).into(cvProfileImage);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void updateProfile() {
        String name = edtName.getText().toString().trim();
        String city = edtCity.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(city) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        if (profilePicUri != null) {
            String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
            SupabaseStorageHelper.uploadImage(this, profilePicUri, fileName, new SupabaseStorageHelper.UploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    saveUserData(name, city, phone, imageUrl);
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfileActivity.this, "Image Upload Failed: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            saveUserData(name, city, phone, currentImageUrl);
        }
    }

    private void saveUserData(String name, String city, String phone, String imageUrl) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("city", city);
            json.put("phone", phone);
            json.put("profile_url", imageUrl);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/users?email=eq." + userEmail)
                    .patch(body)
                    .addHeader("apikey", SupabaseConfig.API_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfileActivity.this, "Update Failed", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Update Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectProfileImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }
}
