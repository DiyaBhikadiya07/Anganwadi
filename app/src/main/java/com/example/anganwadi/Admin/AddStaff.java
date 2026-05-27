package com.example.anganwadi.Admin;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anganwadi.R;
import com.example.anganwadi.BaseActivity;
import com.example.anganwadi.FileUtil;
import com.example.anganwadi.Pojo.StaffData;
import com.example.anganwadi.SupabaseConfig;
import com.example.anganwadi.SupabaseStorageHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddStaff extends BaseActivity {

    TextInputEditText edtName, edtAge, edtQualitfication, edtEmail, edtPhone, edtCity;
    TextInputLayout ageLayout, emailLayout, phoneLayout;
    Button btnStaff;
    private String key;
    CircleImageView cvProfile;
    Uri profilePicUri;
    private String imageUrlFirebase;
    String readImagePermission;
    private Uri files;
    private ProgressBar progressBar;
    RadioGroup rgGender;
    TextView headingAddstaff;

    private boolean isUpdate = false;
    private String staffId;

    // Pattern to ONLY accept @gmail.com
    private static final Pattern GMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@gmail\\.com$"
    );

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> settingsLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_staff);
        edtName = findViewById(R.id.edtSName);
        edtAge = findViewById(R.id.edtSAge);
        edtQualitfication = findViewById(R.id.edtQua);
        edtEmail = findViewById(R.id.edtSEmail);
        edtPhone = findViewById(R.id.edtPhone2);
        edtCity = findViewById(R.id.edtCity2);
        cvProfile = findViewById(R.id.cvProfile);
        rgGender = findViewById(R.id.rg);
        progressBar = findViewById(R.id.progressbar);
        headingAddstaff = findViewById(R.id.headingAddstaff);
        btnStaff = findViewById(R.id.btnStaff);

        ageLayout = findViewById(R.id.SAgetv);
        emailLayout = findViewById(R.id.SEmailtv);
        phoneLayout = findViewById(R.id.Phonetv2);

        initializeLaunchers();

        // Check if we are in update mode
        if (getIntent().hasExtra("staff_id")) {
            isUpdate = true;
            staffId = getIntent().getStringExtra("staff_id");
            populateFields();
            headingAddstaff.setText("Update Staff");
            btnStaff.setText("Update");
        }

        cvProfile.setOnClickListener(v -> {
            selectImage(AddStaff.this);
        });

        findViewById(R.id.iv_openCam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(AddStaff.this);
            }
        });

        btnStaff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = edtName.getText().toString().trim();
                String ageStr = edtAge.getText().toString().trim();
                String qualification = edtQualitfication.getText().toString().trim();
                String email = edtEmail.getText().toString().trim().toLowerCase();
                String phone = edtPhone.getText().toString().trim();
                String city = edtCity.getText().toString().trim();

                // Reset errors
                ageLayout.setError(null);
                emailLayout.setError(null);
                phoneLayout.setError(null);

                if (name.isEmpty() || ageStr.isEmpty() || qualification.isEmpty() || email.isEmpty() || phone.isEmpty() || city.isEmpty()) {
                    Toast.makeText(AddStaff.this, getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Age validation
                try {
                    int age = Integer.parseInt(ageStr);
                    if (ageStr.length() > 2) {
                        ageLayout.setError("Age must be a 2-digit number");
                        return;
                    }
                    if (age < 18) {
                        ageLayout.setError("Age must be 18 or above");
                        return;
                    }
                    if (age > 65) {
                        ageLayout.setError("Age cannot exceed 65 years");
                        return;
                    }
                } catch (NumberFormatException e) {
                    ageLayout.setError("Invalid age");
                    return;
                }

                // ONLY GMAIL VALIDATION - Reject everything else
                if (!GMAIL_PATTERN.matcher(email).matches()) {
                    emailLayout.setError("Only Gmail addresses are allowed (example@gmail.com)");
                    return;
                }

                // Additional check for common Gmail typos
                if (email.contains("@gmal.com") || email.contains("@gmali.com") ||
                        email.contains("@gmil.com") || email.contains("@gnail.com") ||
                        email.contains("@gamil.com") || email.contains("@gmeil.com") ||
                        email.contains("@gmial.com")) {
                    emailLayout.setError("Invalid email domain. Did you mean @gmail.com?");
                    return;
                }

                // Phone validation
                if (phone.length() != 10 || !phone.matches("\\d+")) {
                    phoneLayout.setError("Enter a valid 10-digit phone number");
                    return;
                }

                if (!isUpdate && profilePicUri == null) {
                    Toast.makeText(AddStaff.this, getString(R.string.select_profile_picture), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (profilePicUri != null) {
                    uploadProfilePicture();
                } else {
                    saveStaffData(imageUrlFirebase != null ? imageUrlFirebase : "");
                }
            }
        });

    }

    private void populateFields() {
        edtName.setText(getIntent().getStringExtra("name"));
        edtAge.setText(getIntent().getStringExtra("age"));
        edtQualitfication.setText(getIntent().getStringExtra("qualification"));
        edtEmail.setText(getIntent().getStringExtra("email"));
        edtPhone.setText(getIntent().getStringExtra("phone"));
        edtCity.setText(getIntent().getStringExtra("city"));
        imageUrlFirebase = getIntent().getStringExtra("imageUrl");

        String gender = getIntent().getStringExtra("gender");
        if (gender != null) {
            if (gender.equalsIgnoreCase("Male")) {
                rgGender.check(R.id.male);
            } else if (gender.equalsIgnoreCase("Female")) {
                rgGender.check(R.id.female);
            } else {
                rgGender.check(R.id.other);
            }
        }

        if (imageUrlFirebase != null && !imageUrlFirebase.isEmpty()) {
            Picasso.get().load(imageUrlFirebase).placeholder(R.drawable.profile_member).into(cvProfile);
        }
    }

    private void initializeLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Bitmap img = (Bitmap) result.getData().getExtras().get("data");
                            if (img != null) {
                                cvProfile.setImageBitmap(img);
                                profilePicUri = getImageUri(AddStaff.this, img);
                                String imgPath = FileUtil.getPath(AddStaff.this, profilePicUri);
                                files = Uri.parse(imgPath);
                                Log.e("image", imgPath);
                            }
                        }
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Uri img = result.getData().getData();
                            if (img != null) {
                                Picasso.get().load(img).into(cvProfile);
                                profilePicUri = img;
                                String imgPath = FileUtil.getPath(AddStaff.this, img);
                                files = Uri.parse(imgPath);
                            }
                        }
                    }
                });

        settingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                    }
                });
    }

    private void selectImage(Context context) {

        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle("Choose a Media");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraLauncher.launch(takePicture);

                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryLauncher.launch(pickPhoto);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public Uri getImageUri(Activity inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "intuenty", null);
        Log.d("image uri", path);
        return Uri.parse(path);
    }

    private void uploadProfilePicture() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        btnStaff.setEnabled(false);

        if (profilePicUri != null) {
            String fileName = "staff_" + System.currentTimeMillis() + ".jpg";
            SupabaseStorageHelper.uploadImage(this, profilePicUri, fileName, new SupabaseStorageHelper.UploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    imageUrlFirebase = imageUrl;
                    saveStaffData(imageUrlFirebase);
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        btnStaff.setEnabled(true);
                        Toast.makeText(AddStaff.this, "Photo upload failed: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            saveStaffData(imageUrlFirebase != null ? imageUrlFirebase : "");
        }
    }

    private void saveStaffData(String imageUrl) {
        String name = edtName.getText().toString().trim();
        String age = edtAge.getText().toString().trim();
        String qualification = edtQualitfication.getText().toString().trim();
        String email = edtEmail.getText().toString().trim().toLowerCase();
        String phone = edtPhone.getText().toString().trim();
        String city = edtCity.getText().toString().trim();

        int selectedId = rgGender.getCheckedRadioButtonId();
        String gender = "";
        if (selectedId != -1) {
            RadioButton radioButton = findViewById(selectedId);
            gender = radioButton.getText().toString();
        }

        OkHttpClient client = new OkHttpClient();
        try {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("age", age);
            json.put("qualification", qualification);
            json.put("email", email);
            json.put("phone", phone);
            json.put("city", city);
            json.put("gender", gender);
            json.put("photo_url", imageUrl);

            Request request;
            if (isUpdate) {
                RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
                request = new Request.Builder()
                        .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/staff?id=eq." + staffId)
                        .patch(body)
                        .addHeader("apikey", SupabaseConfig.API_KEY)
                        .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=minimal")
                        .build();
            } else {
                RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
                request = new Request.Builder()
                        .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/staff")
                        .post(body)
                        .addHeader("apikey", SupabaseConfig.API_KEY)
                        .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=minimal")
                        .build();
            }

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        btnStaff.setEnabled(true);
                        Toast.makeText(AddStaff.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        btnStaff.setEnabled(true);
                        if (response.isSuccessful()) {
                            String message = isUpdate ? "Staff updated successfully" : "Staff registered successfully";
                            Toast.makeText(AddStaff.this, message, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String message = isUpdate ? "Update failed: " : "Registration failed: ";
                            Toast.makeText(AddStaff.this, message + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            btnStaff.setEnabled(true);
        }
    }

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This permission is required for Profile Pic. Please grant the permission in the app settings.");

        builder.setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openAppSettings();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        settingsLauncher.launch(intent);
    }
}