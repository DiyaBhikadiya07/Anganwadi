package com.example.anganwadi.User;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.anganwadi.R;
import com.example.anganwadi.BaseActivity;
import com.example.anganwadi.MainActivity;
import com.example.anganwadi.Pojo.BloodUser;
import com.example.anganwadi.SupabaseConfig;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BloodDonationActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {

    EditText name, phone, city;
    TextInputLayout phoneLayout;
    Button register;
    Spinner spin;
    RadioGroup rg;
    String[] grp = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+" , "O-"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_donation);

        name = findViewById(R.id.edtName);
        phone = findViewById(R.id.edtPhone);
        city = findViewById(R.id.edtCity);
        phoneLayout = findViewById(R.id.Phonetv);
        register = findViewById(R.id.btnRegister);
        spin = findViewById(R.id.bloodgrpspinner);
        rg = findViewById(R.id.rg);
        
        spin.setOnItemSelectedListener(this);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, grp);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.common_google_signin_btn_text_dark_default));
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_blood_donation));
        }

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String donorName = name.getText().toString().trim();
                String donorPhone = phone.getText().toString().trim();
                String donorCity = city.getText().toString().trim();
                String donorGroup = spin.getSelectedItem().toString();

                // Reset phone error
                phoneLayout.setError(null);
                
                if (donorName.isEmpty()) {
                    name.setError("Please Enter Name");
                    return;
                }

                // Strictly 10 digits validation
                if (donorPhone.length() != 10) {
                    phoneLayout.setError("Please enter a valid 10-digit phone number");
                    phone.requestFocus();
                    return;
                }

                register.setEnabled(false);
                
                BloodUser bloodUser = new BloodUser();
                bloodUser.setName(donorName);
                bloodUser.setPhone(donorPhone);
                bloodUser.setCity(donorCity);
                bloodUser.setGrp(donorGroup);

                int selectedId = rg.getCheckedRadioButtonId();
                if (selectedId != -1) {
                    RadioButton rb = findViewById(selectedId);
                    bloodUser.setGender(rb.getText().toString());
                } else {
                    bloodUser.setGender("Not Specified");
                }

                saveToSupabase(bloodUser);
            }
        });
    }

    private void saveToSupabase(BloodUser bloodUser) {
        OkHttpClient client = new OkHttpClient();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", bloodUser.getName());
            jsonObject.put("phone", bloodUser.getPhone());
            jsonObject.put("city", bloodUser.getCity());
            jsonObject.put("blood_group", bloodUser.getGrp());
            jsonObject.put("gender", bloodUser.getGender());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/blood_donation")
                .post(body)
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    register.setEnabled(true);
                    Toast.makeText(BloodDonationActivity.this, "Failed to register: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(BloodDonationActivity.this, getString(R.string.blood_registered), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(BloodDonationActivity.this, MainActivity.class));
                        finish();
                    });
                } else {
                    runOnUiThread(() -> {
                        register.setEnabled(true);
                        Toast.makeText(BloodDonationActivity.this, "Storage Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {}

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}
}
