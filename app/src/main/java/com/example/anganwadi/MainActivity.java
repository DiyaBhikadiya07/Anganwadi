package com.example.anganwadi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.anganwadi.Adapter.DataAdapter;
import com.example.anganwadi.Pojo.DataClass;
import com.example.anganwadi.User.BloodDonationActivity;
import com.example.anganwadi.User.ChildCareActivity;
import com.example.anganwadi.User.NearbyCentersActivity;
import com.example.anganwadi.User.NewsActivity;
import com.example.anganwadi.User.NoticeUserActivity;
import com.example.anganwadi.User.ProfileActivity;
import com.example.anganwadi.User.TeacherInfoActivity;
import com.example.anganwadi.TotalStudent;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends BaseActivity implements ClickInterface {

    RecyclerView recyclerView;
    ArrayList<DataClass> data;
    private String userEmail;
    private boolean isNoticeAlertShown = false;
    private boolean isNutritionAlertShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        userEmail = prefs.getString("userEmail", "");

        prepareData();

        DataAdapter adapter = new DataAdapter(MainActivity.this, data, this);
        recyclerView.setAdapter(adapter);

        checkForUpdates();
    }

    public void prepareData() {
        data = new ArrayList<>();
        data.add(new DataClass(getString(R.string.centers), R.drawable.center1)); // Renamed from Location to Centers
        data.add(new DataClass(getString(R.string.nutrition), R.drawable.nplan));
        data.add(new DataClass(getString(R.string.notice), R.drawable.notice));
        data.add(new DataClass(getString(R.string.news), R.drawable.news));
        data.add(new DataClass(getString(R.string.child_care), R.drawable.care));
        data.add(new DataClass(getString(R.string.blood_donation), R.drawable.bloodtest));
        data.add(new DataClass(getString(R.string.teacher_information), R.drawable.teacherinfo));
    }

    private void checkForUpdates() {
        if (isAdminLoggedIn()) return;

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        
        // 1. Check for New/Updated Notice
        OkHttpClient client = new OkHttpClient();
        Request noticeRequest = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/notices?select=id&limit=1&order=id.desc")
                .get()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(noticeRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(response.body().string());
                        if (array.length() > 0) {
                            String currentId = array.getJSONObject(0).optString("id");
                            String lastNoticeId = prefs.getString("lastNoticeId", "");
                            if (!currentId.equals(lastNoticeId)) {
                                runOnUiThread(() -> {
                                    if (!isNoticeAlertShown) {
                                        showUpdateAlert("New Notice", "A new notice has arrived from the admin!");
                                        isNoticeAlertShown = true;
                                    }
                                    prefs.edit().putString("lastNoticeId", currentId).apply();
                                });
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        });

        // 2. Check for New Nutrition (if user is logged in)
        if (!TextUtils.isEmpty(userEmail)) {
            fetchUserPhoneAndCheckNutrition(prefs);
        }
    }

    private void fetchUserPhoneAndCheckNutrition(SharedPreferences prefs) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/users?select=phone&email=eq." + userEmail)
                .get()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(response.body().string());
                        if (array.length() > 0) {
                            String phone = array.getJSONObject(0).optString("phone");
                            checkNutritionCount(phone, prefs);
                        }
                    } catch (Exception ignored) {}
                }
            }
        });
    }

    private void checkNutritionCount(String phone, SharedPreferences prefs) {
        OkHttpClient client = new OkHttpClient();
        // Count nutrition entries for children associated with this phone
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/nutrition?select=id&count=exact")
                .get()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .addHeader("Range", "0-0") // Just to get the count
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String contentRange = response.header("Content-Range");
                if (contentRange != null && contentRange.contains("/")) {
                    String totalStr = contentRange.split("/")[1];
                    int currentCount = Integer.parseInt(totalStr);
                    int lastCount = prefs.getInt("lastNutritionCount", 0);
                    
                    if (currentCount > lastCount && lastCount != 0) {
                        runOnUiThread(() -> {
                            if (!isNutritionAlertShown) {
                                showUpdateAlert("Nutrition Update", "New nutrition data has been added for your child!");
                                isNutritionAlertShown = true;
                            }
                        });
                    }
                    prefs.edit().putInt("lastNutritionCount", currentCount).apply();
                }
            }
        });
    }

    private void showUpdateAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("View", (dialog, which) -> {
                    if (title.contains("Notice")) {
                        startActivity(new Intent(MainActivity.this, NoticeUserActivity.class));
                    } else {
                        Intent intent = new Intent(MainActivity.this, TotalStudent.class);
                        intent.putExtra("isFromMain", "true");
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Dismiss", null)
                .show();
    }

    private boolean isAdminLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isAdminLoggedIn", false);
    }

    @Override
    public void onItemClick(int position) {
        switch (position) {
            case 0: // Centers
                startActivity(new Intent(MainActivity.this, NearbyCentersActivity.class));
                break;
            case 1: // Nutrition
                Intent nutrition = new Intent(MainActivity.this, TotalStudent.class);
                nutrition.putExtra("isFromMain", "true");
                startActivity(nutrition);
                break;
            case 2: // Notice
                startActivity(new Intent(MainActivity.this, NoticeUserActivity.class));
                break;
            case 3: // News
                startActivity(new Intent(MainActivity.this, NewsActivity.class));
                break;
            case 4: // Child Care
                startActivity(new Intent(MainActivity.this, ChildCareActivity.class));
                break;
            case 5: // Blood Donation
                startActivity(new Intent(MainActivity.this, BloodDonationActivity.class));
                break;
            case 6: // Teacher Information
                startActivity(new Intent(MainActivity.this, TeacherInfoActivity.class));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.icon_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_profile_icon) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
