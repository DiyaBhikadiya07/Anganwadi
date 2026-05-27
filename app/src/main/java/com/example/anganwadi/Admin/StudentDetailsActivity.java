package com.example.anganwadi.Admin;

import android.content.DialogInterface;
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

import com.example.anganwadi.BaseActivity;
import com.example.anganwadi.Pojo.StudentData;
import com.example.anganwadi.R;
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

public class StudentDetailsActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private StudentNameAdapter adapter;
    private List<StudentData> studentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_details);

        recyclerView = findViewById(R.id.rvStudentNames);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new StudentNameAdapter(studentList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchStudentNames();
    }

    private void fetchStudentNames() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/children?select=*")
                .get()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(StudentDetailsActivity.this, "Failed to load students", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String body = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONArray array = new JSONArray(body);
                        studentList.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            StudentData student = new StudentData();
                            student.setKey(obj.optString("id"));
                            student.setName(obj.optString("name"));
                            student.setAge(obj.optString("age"));
                            student.setPhone(obj.optString("phone"));
                            student.setFatherName(obj.optString("father_name"));
                            student.setMotherName(obj.optString("mother_name"));
                            student.setCity(obj.optString("city"));
                            student.setGender(obj.optString("gender"));
                            studentList.add(student);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private class StudentNameAdapter extends RecyclerView.Adapter<StudentViewHolder> {
        private List<StudentData> list;

        public StudentNameAdapter(List<StudentData> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_name, parent, false);
            return new StudentViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
            StudentData student = list.get(position);
            holder.tvName.setText(student.getName());
            
            // Move options dialog to long-press
            holder.itemView.setOnLongClickListener(v -> {
                showOptionsDialog(student);
                return true;
            });
            
            // Keep regular click for showing nutrition data or detail if needed, 
            // but the request specifically mentioned long-press for options.
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private void showOptionsDialog(StudentData student) {
        String[] options = {"Update", "Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(student.getName());
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Update
                Intent intent = new Intent(StudentDetailsActivity.this, AddStudent.class);
                intent.putExtra("student_data", student);
                startActivity(intent);
            } else if (which == 1) {
                // Delete
                confirmDelete(student);
            }
        });
        builder.show();
    }

    private void confirmDelete(StudentData student) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Student")
                .setMessage("Are you sure you want to delete " + student.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteStudent(student.getKey()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteStudent(String key) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/children?id=eq." + key)
                .delete()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(StudentDetailsActivity.this, "Failed to delete student", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(StudentDetailsActivity.this, "Student deleted successfully", Toast.LENGTH_SHORT).show();
                        fetchStudentNames();
                    } else {
                        Toast.makeText(StudentDetailsActivity.this, "Delete failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStudentNameOnly);
        }
    }
}
