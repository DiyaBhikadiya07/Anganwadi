package com.example.anganwadi.Admin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anganwadi.R;
import com.example.anganwadi.Adapter.DashBoardAdapter;
import com.example.anganwadi.BaseActivity;
import com.example.anganwadi.Demo;
import com.example.anganwadi.Pojo.Model;
import com.example.anganwadi.SupabaseConfig;
import com.example.anganwadi.TotalStudent;
import com.example.anganwadi.interfaces.OnStudentItemClick;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AdminDashboard extends BaseActivity implements OnStudentItemClick {
    private ArrayList<Model> arrayList;
    private DashBoardAdapter dashBoardAdapter;

    private int totalStudents = 0;
    private int totalStaff = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        arrayList = new ArrayList<>();
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(AdminDashboard.this, 2);
        recyclerView.setLayoutManager(layoutManager);
        dashBoardAdapter = new DashBoardAdapter(arrayList, AdminDashboard.this, this);
        recyclerView.setAdapter(dashBoardAdapter);

        if (getIntent().hasExtra("isFromMain")) {
            TextView tvTitle = findViewById(R.id.tvStudentName);
            if (tvTitle != null) {
                tvTitle.setText("Reports");
            }
            View logoutBtn = findViewById(R.id.logoutButton);
            if (logoutBtn != null) {
                logoutBtn.setVisibility(View.GONE);
            }
        } else {
            View logoutBtn = findViewById(R.id.logoutButton);
            if (logoutBtn != null) {
                logoutBtn.setVisibility(View.VISIBLE);
                logoutBtn.setOnClickListener(v -> {
                    clearAllPreferences(AdminDashboard.this);
                    startActivity(new Intent(AdminDashboard.this, Demo.class));
                    finish();
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchDashboardData();
    }

    private void fetchDashboardData() {
        OkHttpClient client = new OkHttpClient();

        Request studentRequest = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/children?select=*")
                .get()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(studentRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray arr = new JSONArray(response.body().string());
                        totalStudents = arr.length();
                        runOnUiThread(() -> setupDashboard());
                    } catch (Exception ignored) {}
                }
            }
        });

        Request staffRequest = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/staff?select=*")
                .get()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(staffRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray arr = new JSONArray(response.body().string());
                        totalStaff = arr.length();
                        runOnUiThread(() -> setupDashboard());
                    } catch (Exception ignored) {}
                }
            }
        });
    }

    private void setupDashboard() {
        arrayList.clear();
        arrayList.add(new Model(R.drawable.totalchild, getString(R.string.total_students, totalStudents)));
        arrayList.add(new Model(R.drawable.totalss, getString(R.string.total_staff, totalStaff)));
        arrayList.add(new Model(R.drawable.children, getString(R.string.add_student)));
        arrayList.add(new Model(R.drawable.employees, getString(R.string.add_staff)));
        arrayList.add(new Model(R.drawable.children2, getString(R.string.student_details)));
        arrayList.add(new Model(R.drawable.bloodtest, getString(R.string.blood_details)));
        arrayList.add(new Model(R.drawable.notice, getString(R.string.notice)));
        dashBoardAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStudentClick(int pos) {
        switch (pos) {
            case 0:
                startActivity(new Intent(this, TotalStudent.class));
                break;
            case 1:
                startActivity(new Intent(this, TotalStaff.class));
                break;
            case 2:
                startActivity(new Intent(this, AddStudent.class));
                break;
            case 3:
                startActivity(new Intent(this, AddStaff.class));
                break;
            case 4:
                startActivity(new Intent(this, StudentDetailsActivity.class));
                break;
            case 5:
                startActivity(new Intent(this, TotalBloodDetails.class));
                break;
            case 6:
                startActivity(new Intent(this, NoticeAdminActivity.class));
                break;
        }
    }

    public void clearAllPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}