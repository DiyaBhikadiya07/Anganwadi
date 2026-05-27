package com.example.anganwadi.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anganwadi.R;
import com.example.anganwadi.BaseActivity;
import com.example.anganwadi.Pojo.StaffData;
import com.example.anganwadi.SupabaseConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TotalStaff extends BaseActivity {
    private StaffAdapter adapter;
    private RecyclerView recyclerView;
    private TextView noStudentsTextView;
    private List<StaffData> staffList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_staff);

        noStudentsTextView = findViewById(R.id.noStudentsTextView);
        recyclerView = findViewById(R.id.rvStaff);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new StaffAdapter(staffList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnAdd).setOnClickListener(v -> {
            Intent i = new Intent(TotalStaff.this, AddStaff.class);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchStaffData();
    }

    private void fetchStaffData() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/staff")
                .get()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(TotalStaff.this, "Failed to load staff", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String body = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONArray array = new JSONArray(body);
                        staffList.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            StaffData staff = new StaffData();
                            staff.setName(obj.optString("name"));

                            // FIX: Handle Age (Convert potential integers to string, handle nulls)
                            String ageValue = obj.optString("age", "N/A");
                            if (ageValue == null || ageValue.equals("null") || ageValue.trim().isEmpty()) {
                                ageValue = "N/A";
                            }
                            staff.setAge(ageValue);

                            staff.setQualification(obj.optString("qualification", "N/A"));
                            staff.setEmail(obj.optString("email", "N/A"));
                            staff.setPhoneNo(obj.optString("phone", "N/A"));
                            staff.setCity(obj.optString("city", "N/A"));
                            staff.setGender(obj.optString("gender", "N/A"));
                            staff.setImageUrl(obj.optString("photo_url", ""));
                            staff.setKey(obj.optString("id"));
                            staffList.add(staff);
                        }
                        updateUI();
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void updateUI() {
        if (staffList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noStudentsTextView.setVisibility(View.VISIBLE);
            noStudentsTextView.setText("No Staff added!");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noStudentsTextView.setVisibility(View.GONE);
        }
    }

    private void deleteStaff(String key, int position) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/staff?id=eq." + key)
                .delete()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(TotalStaff.this, "Failed to delete staff", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(TotalStaff.this, "Staff deleted successfully", Toast.LENGTH_SHORT).show();
                        fetchStaffData();
                    }
                });
            }
        });
    }

    public class StaffAdapter extends RecyclerView.Adapter<StaffViewHolder> {
        private List<StaffData> mList;

        public StaffAdapter(List<StaffData> list) {
            this.mList = list;
        }

        @NonNull
        @Override
        public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_item, parent, false);
            return new StaffViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
            StaffData model = mList.get(position);
            holder.bind(model, model.getKey(), position);
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }

    public class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView myTextView;
        TextView ss;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.tvStudentName);
            ss = itemView.findViewById(R.id.txtAddNt);
        }

        public void bind(final StaffData staff, final String key, final int position) {
            myTextView.setText(staff.getName());
            ss.setVisibility(View.VISIBLE);
            ss.setText("Update");

            ss.setOnClickListener(v -> {
                Intent intent = new Intent(TotalStaff.this, AddStaff.class);
                intent.putExtra("staff_id", staff.getKey());
                intent.putExtra("name", staff.getName());
                intent.putExtra("age", staff.getAge());
                intent.putExtra("qualification", staff.getQualification());
                intent.putExtra("email", staff.getEmail());
                intent.putExtra("phone", staff.getPhoneNo());
                intent.putExtra("city", staff.getCity());
                intent.putExtra("gender", staff.getGender());
                intent.putExtra("imageUrl", staff.getImageUrl());
                startActivity(intent);
            });

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(TotalStaff.this, StaffProfileActivity.class);
                intent.putExtra("name", staff.getName());
                intent.putExtra("age", staff.getAge());
                intent.putExtra("qualification", staff.getQualification());
                intent.putExtra("gender", staff.getGender());
                intent.putExtra("city", staff.getCity());
                intent.putExtra("phone", staff.getPhoneNo());
                intent.putExtra("imageUrl", staff.getImageUrl());
                startActivity(intent);
            });

            itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(TotalStaff.this)
                        .setMessage(getString(R.string.delete_student_confirm))
                        .setPositiveButton(getString(R.string.delete), (dialog, which) -> deleteStaff(key, position))
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return true;
            });
        }
    }
}
