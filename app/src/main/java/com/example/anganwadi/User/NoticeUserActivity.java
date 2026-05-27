package com.example.anganwadi.User;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NoticeUserActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private NoticeUserAdapter adapter;
    private List<NoticeModel> noticeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_user);

        recyclerView = findViewById(R.id.rvNotices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoticeUserAdapter(noticeList);
        recyclerView.setAdapter(adapter);

        fetchNotices();
    }

    private void fetchNotices() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/notices?select=*&order=id.desc")
                .get()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(NoticeUserActivity.this, "Failed to load notices", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body().string();
                        JSONArray array = new JSONArray(body);
                        noticeList.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            noticeList.add(new NoticeModel(
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

    private static class NoticeModel {
        String content, date;
        NoticeModel(String content, String date) { this.content = content; this.date = date; }
    }

    private class NoticeUserAdapter extends RecyclerView.Adapter<NoticeViewHolder> {
        private List<NoticeModel> list;
        NoticeUserAdapter(List<NoticeModel> list) { this.list = list; }

        @NonNull @Override public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice_user, parent, false);
            return new NoticeViewHolder(view);
        }

        @Override public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
            NoticeModel model = list.get(position);
            holder.tvContent.setText(model.content);
            String rawDate = model.date;
            if (rawDate != null && rawDate.contains("T")) rawDate = rawDate.split("T")[0];
            holder.tvDate.setText("Date: " + rawDate);
        }
        @Override public int getItemCount() { return list.size(); }
    }

    private static class NoticeViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvDate;
        NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvNoticeContent);
            tvDate = itemView.findViewById(R.id.tvNoticeDate);
        }
    }
}
