package com.example.anganwadi.Admin;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import com.example.anganwadi.R;
import com.example.anganwadi.Adapter.NutritionDataAdapter;
import com.example.anganwadi.BaseActivity;
import com.example.anganwadi.Pojo.NutritionData;
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

public class NutritionDataActivity extends BaseActivity {
    private RecyclerView nutritionRecyclerView;
    private NutritionDataAdapter adapter;
    private List<NutritionData> nutritionDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_data);

        nutritionRecyclerView = findViewById(R.id.nutritionRecyclerView);
        nutritionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String studentKey = getIntent().getStringExtra("studentKey");

        if (studentKey != null) {
            fetchDataForStudent(studentKey);
        }
    }

    private void fetchDataForStudent(String studentKey) {
        OkHttpClient client = new OkHttpClient();
        
        // URL based on your nutrition table: Column child_id (uuid)
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/nutrition?child_id=eq." + studentKey)
                .get()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(NutritionDataActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String resBody = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONArray array = new JSONArray(resBody);
                        nutritionDataList.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            NutritionData data = new NutritionData();
                            // Mapping from your table column names
                            data.setDate(obj.optString("date"));
                            data.setFood(obj.optString("food_name"));
                            data.setImageUrl(obj.optString("image_url"));
                            nutritionDataList.add(data);
                        }
                        updateUI();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateUI() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            adapter = new NutritionDataAdapter(nutritionDataList);
            nutritionRecyclerView.setAdapter(adapter);
        }
    }
}
