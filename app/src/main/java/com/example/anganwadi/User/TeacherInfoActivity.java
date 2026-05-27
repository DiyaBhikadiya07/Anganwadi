package com.example.anganwadi.User;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anganwadi.BaseActivity;
import com.example.anganwadi.Pojo.StaffData;
import com.example.anganwadi.R;
import com.example.anganwadi.SupabaseConfig;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

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

public class TeacherInfoActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private TeacherAdapter adapter;
    private List<StaffData> teacherList = new ArrayList<>();
    private TextView tvNoData;
    private MaterialCardView featuredCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.teacher_information));
        }

        recyclerView = findViewById(R.id.rvTeachers);
        tvNoData = findViewById(R.id.tvNoData);
        featuredCard = findViewById(R.id.featuredTeacherCard);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupFeaturedTeacher();

        adapter = new TeacherAdapter(teacherList);
        recyclerView.setAdapter(adapter);

        fetchTeacherData();
    }

    private void setupFeaturedTeacher() {
        featuredCard.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherInfoActivity.this, TeacherDetailActivity.class);
            intent.putExtra("name", "Lathiya Shobhaben Ghanshyambhai");
            intent.putExtra("age", "35");
            intent.putExtra("qualification", "B.Com");
            intent.putExtra("gender", "Female");
            intent.putExtra("phone", "9878943456");
            intent.putExtra("imageUrl", "women.jpeg");
            startActivity(intent);
        });
    }

    private void fetchTeacherData() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/staff?select=*")
                .get()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    if (teacherList.isEmpty()) tvNoData.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String body = response.body() != null ? response.body().string() : "[]";
                runOnUiThread(() -> {
                    try {
                        JSONArray array = new JSONArray(body);
                        teacherList.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String name = obj.optString("name");
                            
                            // Prevent duplicating the featured teacher if they are in the DB
                            if (!name.equalsIgnoreCase("Lathiya Shobhaben Ghanshyambhai")) {
                                StaffData staff = new StaffData();
                                staff.setName(name);
                                staff.setQualification(obj.optString("qualification"));
                                staff.setGender(obj.optString("gender"));
                                staff.setAge(obj.optString("age"));
                                staff.setImageUrl(obj.optString("photo_url"));
                                staff.setPhoneNo(obj.optString("phone"));
                                teacherList.add(staff);
                            }
                        }

                        tvNoData.setVisibility(teacherList.isEmpty() ? View.GONE : View.GONE);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
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

    private class TeacherAdapter extends RecyclerView.Adapter<TeacherViewHolder> {
        private List<StaffData> list;

        public TeacherAdapter(List<StaffData> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_info, parent, false);
            return new TeacherViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
            StaffData teacher = list.get(position);
            holder.tvName.setText(teacher.getName());

            if (teacher.getImageUrl() != null && !teacher.getImageUrl().isEmpty()) {
                Picasso.get().load(teacher.getImageUrl()).placeholder(R.drawable.profile_member).into(holder.ivProfile);
            } else {
                holder.ivProfile.setImageResource(R.drawable.profile_member);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(TeacherInfoActivity.this, TeacherDetailActivity.class);
                intent.putExtra("name", teacher.getName());
                intent.putExtra("qualification", teacher.getQualification());
                intent.putExtra("gender", teacher.getGender());
                intent.putExtra("age", teacher.getAge());
                intent.putExtra("imageUrl", teacher.getImageUrl());
                intent.putExtra("phone", teacher.getPhoneNo());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private static class TeacherViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivProfile;

        public TeacherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTeacherName);
            ivProfile = itemView.findViewById(R.id.ivTeacherProfile);
        }
    }
}
