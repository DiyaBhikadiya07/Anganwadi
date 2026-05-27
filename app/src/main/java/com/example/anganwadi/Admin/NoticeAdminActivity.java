package com.example.anganwadi.Admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anganwadi.BaseActivity;
import com.example.anganwadi.R;
import com.example.anganwadi.SupabaseConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoticeAdminActivity extends BaseActivity {

    private EditText edtNotice;
    private Button btnSend;
    private RecyclerView rvHistory;
    private NoticeHistoryAdapter adapter;
    private List<NoticeModel> noticeList = new ArrayList<>();
    private String editingNoticeId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_admin);

        edtNotice = findViewById(R.id.edtNotice);
        btnSend = findViewById(R.id.btnSendNotice);
        rvHistory = findViewById(R.id.rvSentNotices);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoticeHistoryAdapter(noticeList);
        rvHistory.setAdapter(adapter);

        btnSend.setOnClickListener(v -> {
            String notice = edtNotice.getText().toString().trim();
            if (TextUtils.isEmpty(notice)) {
                Toast.makeText(this, "Please write something", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (editingNoticeId == null) {
                sendNoticeToSupabase(notice);
            } else {
                updateNotice(editingNoticeId, notice);
            }
        });
        
        fetchNoticeHistory();
    }

    private void fetchNoticeHistory() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/notices?select=*&order=id.desc")
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
                        String body = response.body().string();
                        JSONArray arr = new JSONArray(body);
                        noticeList.clear();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            noticeList.add(new NoticeModel(
                                    obj.optString("id"),
                                    obj.optString("content"),
                                    obj.optString("created_at")
                            ));
                        }
                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void sendNoticeToSupabase(String content) {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try { json.put("content", content); } catch (Exception ignored) {}

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/notices")
                .post(body)
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(NoticeAdminActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(NoticeAdminActivity.this, "Notice sent!", Toast.LENGTH_SHORT).show();
                        edtNotice.setText("");
                        fetchNoticeHistory();
                    }
                });
            }
        });
    }

    private void deleteNotice(String id) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/notices?id=eq." + id)
                .delete()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {}
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(NoticeAdminActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                        if (editingNoticeId != null && editingNoticeId.equals(id)) {
                            resetEditMode();
                        }
                        fetchNoticeHistory();
                    }
                });
            }
        });
    }

    private void updateNotice(String id, String newContent) {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try { json.put("content", newContent); } catch (Exception ignored) {}

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/notices?id=eq." + id)
                .patch(body)
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {}
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(NoticeAdminActivity.this, "Notice Updated!", Toast.LENGTH_SHORT).show();
                        resetEditMode();
                        fetchNoticeHistory();
                    }
                });
            }
        });
    }

    private void resetEditMode() {
        editingNoticeId = null;
        edtNotice.setText("");
        btnSend.setText("Send Notice");
    }

    private static class NoticeModel {
        String id, content, date;
        NoticeModel(String id, String content, String date) { this.id = id; this.content = content; this.date = date; }
    }

    private class NoticeHistoryAdapter extends RecyclerView.Adapter<NoticeViewHolder> {
        private List<NoticeModel> list;
        NoticeHistoryAdapter(List<NoticeModel> list) { this.list = list; }

        @NonNull @Override public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice_admin, parent, false);
            return new NoticeViewHolder(view);
        }

        @Override public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
            NoticeModel model = list.get(position);
            holder.tvContent.setText(model.content);
            String rawDate = model.date;
            if (rawDate != null && rawDate.contains("T")) rawDate = rawDate.split("T")[0];
            holder.tvDate.setText("Sent on: " + rawDate);

            holder.ivDelete.setOnClickListener(v -> new AlertDialog.Builder(NoticeAdminActivity.this)
                    .setTitle("Delete Notice").setMessage("Delete this notice?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteNotice(model.id))
                    .setNegativeButton("Cancel", null).show());

            holder.ivEdit.setOnClickListener(v -> {
                editingNoticeId = model.id;
                edtNotice.setText(model.content);
                edtNotice.requestFocus();
                // Visible cursor at the end
                edtNotice.setSelection(edtNotice.getText().length());
                btnSend.setText("Update Notice");
                Toast.makeText(NoticeAdminActivity.this, "Editing selected notice", Toast.LENGTH_SHORT).show();
            });
        }
        @Override public int getItemCount() { return list.size(); }
    }

    private static class NoticeViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvDate;
        ImageView ivEdit, ivDelete;
        NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvNoticeContent);
            tvDate = itemView.findViewById(R.id.tvNoticeDate);
            ivEdit = itemView.findViewById(R.id.ivEditNotice);
            ivDelete = itemView.findViewById(R.id.ivDeleteNotice);
        }
    }
}
