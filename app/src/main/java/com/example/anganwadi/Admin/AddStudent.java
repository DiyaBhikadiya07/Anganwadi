package com.example.anganwadi.Admin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.anganwadi.R;
import com.example.anganwadi.BaseActivity;
import com.example.anganwadi.SupabaseConfig;
import com.example.anganwadi.Pojo.StudentData;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddStudent extends BaseActivity {

    TextInputEditText edtName, edtAge, edtFather, edtMother, edtCity, edtPhone;
    TextInputLayout ageLayout, phoneLayout;
    RadioGroup rgGender;
    Button btnAction;
    private String userEmail;
    private StudentData updateStudent;
    private boolean isUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);
        
        edtName = findViewById(R.id.edtCName);
        edtAge = findViewById(R.id.edtAge);
        edtFather = findViewById(R.id.edtFathername);
        edtMother = findViewById(R.id.edtMothername);
        edtCity = findViewById(R.id.edtCity1);
        edtPhone = findViewById(R.id.edtCPhone1);
        rgGender = findViewById(R.id.rg);
        btnAction = findViewById(R.id.btnRegister);
        
        ageLayout = findViewById(R.id.Agetv);
        phoneLayout = findViewById(R.id.CPhonetv1);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        userEmail = prefs.getString("userEmail", "");

        // Check if we are in Update mode
        if (getIntent().hasExtra("student_data")) {
            updateStudent = (StudentData) getIntent().getSerializableExtra("student_data");
            isUpdate = true;
            setupUpdateUI();
        } else {
            if (isAdminLoggedIn()) {
                edtPhone.setEnabled(true);
            } else {
                edtPhone.setEnabled(false);
                loadUserPhoneNumber();
            }
        }

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUpdate) {
                    performUpdate();
                } else {
                    saveStudentData();
                }
            }
        });
    }

    private void setupUpdateUI() {
        btnAction.setText("Update");
        edtName.setText(updateStudent.getName());
        edtAge.setText(updateStudent.getAge());
        edtFather.setText(updateStudent.getFatherName());
        edtMother.setText(updateStudent.getMotherName());
        edtCity.setText(updateStudent.getCity());
        edtPhone.setText(updateStudent.getPhone());
        
        if (updateStudent.getGender() != null) {
            if (updateStudent.getGender().equalsIgnoreCase("Male")) {
                ((RadioButton)findViewById(R.id.male)).setChecked(true);
            } else if (updateStudent.getGender().equalsIgnoreCase("Female")) {
                ((RadioButton)findViewById(R.id.female)).setChecked(true);
            }
        }
    }

    private void loadUserPhoneNumber() {
        if (userEmail.isEmpty()) return;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/users?email=eq." + userEmail)
                .get()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String body = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONArray array = new JSONArray(body);
                        if (array.length() > 0) {
                            JSONObject user = array.getJSONObject(0);
                            edtPhone.setText(user.optString("phone"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private boolean validateInputs() {
        String ageStr = edtAge.getText().toString().trim();
        String phoneStr = edtPhone.getText().toString().trim();
        
        ageLayout.setError(null);
        phoneLayout.setError(null);

        // Age Validation
        if (!ageStr.isEmpty()) {
            try {
                int age = Integer.parseInt(ageStr);
                if (ageStr.length() > 2) {
                    ageLayout.setError("Allow only 2-digit numbers");
                    return false;
                }
                if (age > 6) {
                    ageLayout.setError("Age must be between 0 to 6");
                    return false;
                }
            } catch (NumberFormatException e) {
                ageLayout.setError("Invalid age");
                return false;
            }
        }

        // Phone Validation
        if (phoneStr.length() != 10) {
            phoneLayout.setError("Enter a 10-digit phone number");
            return false;
        }

        return true;
    }

    private void saveStudentData() {
        String name = edtName.getText().toString().trim();
        String age = edtAge.getText().toString().trim();
        String fatherName = edtFather.getText().toString().trim();
        String motherName = edtMother.getText().toString().trim();
        String city = edtCity.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (name.isEmpty() || age.isEmpty() || fatherName.isEmpty() || motherName.isEmpty() || city.isEmpty() || phone.isEmpty() || selectedGenderId == -1) {
            Toast.makeText(AddStudent.this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validateInputs()) return;

        String gender = ((RadioButton)findViewById(selectedGenderId)).getText().toString();

        OkHttpClient client = new OkHttpClient();
        try {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("age", Integer.parseInt(age));
            json.put("father_name", fatherName);
            json.put("mother_name", motherName);
            json.put("city", city);
            json.put("phone", phone);
            json.put("gender", gender);

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/children")
                    .post(body)
                    .addHeader("apikey", SupabaseConfig.API_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(AddStudent.this, "Error", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(AddStudent.this, "Student added", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void performUpdate() {
        String name = edtName.getText().toString().trim();
        String age = edtAge.getText().toString().trim();
        String fatherName = edtFather.getText().toString().trim();
        String motherName = edtMother.getText().toString().trim();
        String city = edtCity.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        String gender = ((RadioButton)findViewById(selectedGenderId)).getText().toString();

        if (!validateInputs()) return;

        OkHttpClient client = new OkHttpClient();
        try {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("age", Integer.parseInt(age));
            json.put("father_name", fatherName);
            json.put("mother_name", motherName);
            json.put("city", city);
            json.put("phone", phone);
            json.put("gender", gender);

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/children?id=eq." + updateStudent.getKey())
                    .patch(body)
                    .addHeader("apikey", SupabaseConfig.API_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(AddStudent.this, "Update Failed", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(AddStudent.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private boolean isAdminLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isAdminLoggedIn", false);
    }
}
