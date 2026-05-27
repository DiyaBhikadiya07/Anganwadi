package com.example.anganwadi.Admin;

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
import com.example.anganwadi.Pojo.BloodUser;
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

public class TotalBloodDetails extends BaseActivity {

    private RecyclerView recyclerView;
    private BloodDonorAdapter adapter;
    private List<BloodUser> donorList = new ArrayList<>();
    private TextView tvNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_blood_details);

        recyclerView = findViewById(R.id.rvBloodDetails);
        tvNoData = findViewById(R.id.tvNoData);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BloodDonorAdapter(donorList);
        recyclerView.setAdapter(adapter);

        fetchBloodDonationData();
    }

    private void fetchBloodDonationData() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/blood_donation?select=*")
                .get()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(TotalBloodDetails.this, "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    tvNoData.setVisibility(View.VISIBLE);
                    tvNoData.setText("Network Error");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String body = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(TotalBloodDetails.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        tvNoData.setVisibility(View.VISIBLE);
                        tvNoData.setText("Server Error: " + response.code());
                    });
                    return;
                }

                runOnUiThread(() -> {
                    try {
                        JSONArray array = new JSONArray(body);
                        donorList.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            BloodUser user = new BloodUser();
                            user.setName(obj.optString("name"));
                            user.setPhone(obj.optString("phone"));
                            user.setCity(obj.optString("city"));
                            user.setGrp(obj.optString("blood_group"));
                            user.setGender(obj.optString("gender"));
                            donorList.add(user);
                        }
                        
                        if (donorList.isEmpty()) {
                            tvNoData.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            tvNoData.setText("No blood donors found");
                        } else {
                            tvNoData.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        tvNoData.setVisibility(View.VISIBLE);
                        tvNoData.setText("Data Parsing Error");
                    }
                });
            }
        });
    }

    private class BloodDonorAdapter extends RecyclerView.Adapter<BloodDonorViewHolder> {
        private List<BloodUser> list;

        public BloodDonorAdapter(List<BloodUser> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public BloodDonorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_donor, parent, false);
            return new BloodDonorViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull BloodDonorViewHolder holder, int position) {
            BloodUser donor = list.get(position);
            holder.tvName.setText(donor.getName());
            holder.tvPhone.setText("Phone: " + donor.getPhone());
            holder.tvGroup.setText(donor.getGrp());
            holder.tvCity.setText("City: " + donor.getCity());
            holder.tvGender.setText("Gender: " + donor.getGender());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private static class BloodDonorViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvGroup, tvCity, tvGender;

        public BloodDonorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDonorName);
            tvPhone = itemView.findViewById(R.id.tvDonorPhone);
            tvGroup = itemView.findViewById(R.id.tvDonorGroup);
            tvCity = itemView.findViewById(R.id.tvDonorCity);
            tvGender = itemView.findViewById(R.id.tvDonorGender);
        }
    }
}
