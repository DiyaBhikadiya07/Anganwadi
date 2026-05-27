package com.example.anganwadi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;

import androidx.appcompat.app.AlertDialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anganwadi.Admin.NutritionDataActivity;
import com.example.anganwadi.Adapter.StudentListAdapter;
import com.example.anganwadi.Pojo.NutritionData;
import com.example.anganwadi.Pojo.StudentData;
import com.example.anganwadi.interfaces.OnNutritionClick;
import com.example.anganwadi.interfaces.OnStudent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TotalStudent extends BaseActivity {
    private ArrayList<StudentData> studentList = new ArrayList<>();
    RecyclerView recyclerView;
    TextView noStudentsTextView;
    StudentListAdapter adapter;
    private static final int CAMERA_PICK = 1;
    private static final int GALLERY_PICK = 2;
    private Uri imageUri;
    private ImageView ivNutritionImage;
    private SearchView searchView;
    private String userEmail;
    private ProgressDialog progressDialog;
    private Button btnMultiNutrition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_student);
        noStudentsTextView = findViewById(R.id.noStudentsTextView);
        recyclerView = findViewById(R.id.rvStudents);
        searchView = findViewById(R.id.searchView);
        btnMultiNutrition = findViewById(R.id.btnMultiNutrition);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        userEmail = prefs.getString("userEmail", "");

        btnMultiNutrition.setOnClickListener(v -> {
            List<String> selectedKeys = adapter.getSelectedStudentKeys();
            if (!selectedKeys.isEmpty()) {
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                showAddMultiNutritionDialog(selectedKeys, date);
            }
        });

        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchDataFromBackend();
    }

    private void setupSearch() {
        if (isAdminLoggedIn()) {
            searchView.setVisibility(View.VISIBLE);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filter(newText);
                    return true;
                }
            });
        } else {
            searchView.setVisibility(View.GONE);
        }
    }

    private void filter(String text) {
        ArrayList<StudentData> filteredList = new ArrayList<>();
        for (StudentData item : studentList) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (adapter != null) {
            adapter.filterList(filteredList);
        }
    }

    private void fetchDataFromBackend() {
        if (isAdminLoggedIn()) {
            fetchAllStudents();
        } else {
            fetchUserPhoneNumberAndStudents();
        }
    }

    private void fetchAllStudents() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/children")
                .get()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(TotalStudent.this, "Failed to load students", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String body = response.body().string();
                runOnUiThread(() -> parseStudentsAndPopulate(body));
            }
        });
    }

    private void fetchUserPhoneNumberAndStudents() {
        if (TextUtils.isEmpty(userEmail)) return;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/users?email=eq." + userEmail)
                .get()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String body = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONArray array = new JSONArray(body);
                        if (array.length() > 0) {
                            String phone = array.getJSONObject(0).optString("phone");
                            fetchStudentsByPhone(phone);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void fetchStudentsByPhone(String phone) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/children?phone=eq." + phone)
                .get()
                .addHeader("apikey", SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String body = response.body().string();
                runOnUiThread(() -> parseStudentsAndPopulate(body));
            }
        });
    }

    private void parseStudentsAndPopulate(String json) {
        try {
            JSONArray array = new JSONArray(json);
            studentList.clear();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                StudentData student = new StudentData();
                student.setName(obj.optString("name"));
                student.setAge(obj.optString("age"));
                student.setPhone(obj.optString("phone"));
                student.setFatherName(obj.optString("father_name"));
                student.setMotherName(obj.optString("mother_name"));
                student.setCity(obj.optString("city"));
                student.setGender(obj.optString("gender"));
                student.setKey(obj.optString("id")); // Using ID as key
                studentList.add(student);
            }
            updateStudentListUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void updateStudentListUI() {
        if (studentList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noStudentsTextView.setVisibility(View.VISIBLE);
            noStudentsTextView.setText("No students added!");
            btnMultiNutrition.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noStudentsTextView.setVisibility(View.GONE);
            recyclerView.setLayoutManager(new LinearLayoutManager(TotalStudent.this));
            adapter = new StudentListAdapter(TotalStudent.this, studentList, new StudentListAdapter.OnItemLongClickListener() {
                @Override
                public void onItemLongClick(String key) {
                    if (isAdminLoggedIn()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(TotalStudent.this);
                        builder.setMessage("Are you sure you want to delete this student?").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteStudent(key);
                            }
                        }).setNegativeButton(android.R.string.no, null).show();
                    }
                }
            }, new OnNutritionClick() {
                @Override
                public void onNutritionClick(int pos, String studentKey, String currDate) {
                    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    showAddNutritionDialog(TotalStudent.this, pos, studentKey, date);
                }
            }, new OnStudent() {
                @Override
                public void onStudentClick(String studentKey) {
                    // In TotalStudent, clicking a student only shows nutrition data
                    Intent intent = new Intent(TotalStudent.this, NutritionDataActivity.class);
                    intent.putExtra("studentKey", studentKey);
                    startActivity(intent);
                }
            }, isAdminLoggedIn());
            
            adapter.setSelectionChangeListener(() -> {
                if (adapter.getSelectedStudentKeys().isEmpty()) {
                    btnMultiNutrition.setVisibility(View.GONE);
                } else {
                    btnMultiNutrition.setVisibility(View.VISIBLE);
                }
            });
            
            recyclerView.setAdapter(adapter);
        }
    }


    @SuppressLint("NotifyDataSetChanged")
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
                runOnUiThread(() -> Toast.makeText(TotalStudent.this, "Failed to delete student", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(TotalStudent.this, "Student deleted successfully", Toast.LENGTH_SHORT).show();
                        fetchDataFromBackend();
                    }
                });
            }
        });
    }

    public void showAddNutritionDialog(Context context, int pos, String studentKey, String currDate) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add_nutrition);

        Spinner spinnerFood = dialog.findViewById(R.id.spinnerFood);
        EditText editTextCustomFood = dialog.findViewById(R.id.editTextCustomFood);
        Button buttonSubmit = dialog.findViewById(R.id.buttonSubmitNutrition);
        Button btnUploadPhoto = dialog.findViewById(R.id.btnUploadPhoto);
        ivNutritionImage = dialog.findViewById(R.id.ivNutritionImage);

        String[] items = new String[]{"Khichdi", "Kathol", "Other"};
        ArrayAdapter<String> adapterFood = new ArrayAdapter<>(this, R.layout.spinner_item_dark, items);
        spinnerFood.setAdapter(adapterFood);

        spinnerFood.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (items[position].equals("Other")) {
                    editTextCustomFood.setVisibility(View.VISIBLE);
                } else {
                    editTextCustomFood.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                editTextCustomFood.setVisibility(View.GONE);
            }
        });

        btnUploadPhoto.setOnClickListener(v -> {
            selectImage();
        });

        buttonSubmit.setOnClickListener(v -> {
            String selectedFood;
            if (spinnerFood.getSelectedItem().toString().equals("Other")) {
                selectedFood = editTextCustomFood.getText().toString().trim();
                if (selectedFood.isEmpty()) {
                    Toast.makeText(context, "Please enter food", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                selectedFood = (String) spinnerFood.getSelectedItem();
            }

            if (imageUri != null) {
                progressDialog.setMessage("Uploading...");
                progressDialog.show();
                uploadNutritionImageAndSave(studentKey, currDate, selectedFood, imageUri, dialog);
            } else {
                progressDialog.setMessage("Saving...");
                progressDialog.show();
                saveNutritionData(studentKey, selectedFood, currDate, "", dialog);
            }
        });

        dialog.show();
    }

    private void showAddMultiNutritionDialog(List<String> studentKeys, String currDate) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_nutrition);

        Spinner spinnerFood = dialog.findViewById(R.id.spinnerFood);
        EditText editTextCustomFood = dialog.findViewById(R.id.editTextCustomFood);
        Button buttonSubmit = dialog.findViewById(R.id.buttonSubmitNutrition);
        Button btnUploadPhoto = dialog.findViewById(R.id.btnUploadPhoto);
        ivNutritionImage = dialog.findViewById(R.id.ivNutritionImage);

        String[] items = new String[]{"Khichdi", "Kathol", "Other"};
        ArrayAdapter<String> adapterFood = new ArrayAdapter<>(this, R.layout.spinner_item_dark, items);
        spinnerFood.setAdapter(adapterFood);

        spinnerFood.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (items[position].equals("Other")) {
                    editTextCustomFood.setVisibility(View.VISIBLE);
                } else {
                    editTextCustomFood.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                editTextCustomFood.setVisibility(View.GONE);
            }
        });

        btnUploadPhoto.setOnClickListener(v -> {
            selectImage();
        });

        buttonSubmit.setOnClickListener(v -> {
            String selectedFood;
            if (spinnerFood.getSelectedItem().toString().equals("Other")) {
                selectedFood = editTextCustomFood.getText().toString().trim();
                if (selectedFood.isEmpty()) {
                    Toast.makeText(this, "Please enter food", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                selectedFood = (String) spinnerFood.getSelectedItem();
            }

            if (imageUri != null) {
                progressDialog.setMessage("Uploading...");
                progressDialog.show();
                uploadMultiNutritionImageAndSave(studentKeys, currDate, selectedFood, imageUri, dialog);
            } else {
                progressDialog.setMessage("Saving...");
                progressDialog.show();
                saveMultiNutritionData(studentKeys, selectedFood, currDate, "", dialog);
            }
        });

        dialog.show();
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, CAMERA_PICK);
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, GALLERY_PICK);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_PICK && data != null) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (ivNutritionImage != null) {
                    ivNutritionImage.setImageBitmap(bitmap);
                    ivNutritionImage.setVisibility(View.VISIBLE);
                }
                imageUri = getImageUri(this, bitmap);
            } else if (requestCode == GALLERY_PICK && data != null) {
                imageUri = data.getData();
                if (ivNutritionImage != null) {
                    ivNutritionImage.setImageURI(imageUri);
                    ivNutritionImage.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Nutrition_" + System.currentTimeMillis(), null);
        return Uri.parse(path);
    }

    private void uploadNutritionImageAndSave(String studentKey, String date, String food, Uri uri, Dialog dialog) {
        String fileName = "nutrition_" + System.currentTimeMillis() + ".jpg";
        SupabaseStorageHelper.uploadImage(this, uri, fileName, new SupabaseStorageHelper.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                saveNutritionData(studentKey, food, date, imageUrl, dialog);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(TotalStudent.this, "Upload Failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void uploadMultiNutritionImageAndSave(List<String> studentKeys, String date, String food, Uri uri, Dialog dialog) {
        String fileName = "nutrition_" + System.currentTimeMillis() + ".jpg";
        SupabaseStorageHelper.uploadImage(this, uri, fileName, new SupabaseStorageHelper.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                saveMultiNutritionData(studentKeys, food, date, imageUrl, dialog);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(TotalStudent.this, "Upload Failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void saveNutritionData(String studentKey, String food, String date, String imageUrl, Dialog dialog) {
        OkHttpClient client = new OkHttpClient();
        try {
            JSONObject json = new JSONObject();
            json.put("child_id", studentKey);
            json.put("food_name", food);
            json.put("date", date);
            json.put("image_url", imageUrl);

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/nutrition")
                    .post(body)
                    .addHeader("apikey", SupabaseConfig.API_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(TotalStudent.this, "Failed to save nutrition data", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    runOnUiThread(() -> {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        if (response.isSuccessful()) {
                            Toast.makeText(TotalStudent.this, "Nutrition data added", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            try {
                                Toast.makeText(TotalStudent.this, "Error: " + response.body().string(), Toast.LENGTH_LONG).show();
                            } catch (Exception ignored) {}
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    private void saveMultiNutritionData(List<String> studentKeys, String food, String date, String imageUrl, Dialog dialog) {
        OkHttpClient client = new OkHttpClient();
        try {
            JSONArray jsonArray = new JSONArray();
            for (String studentKey : studentKeys) {
                JSONObject json = new JSONObject();
                json.put("child_id", studentKey);
                json.put("food_name", food);
                json.put("date", date);
                json.put("image_url", imageUrl);
                jsonArray.put(json);
            }

            RequestBody body = RequestBody.create(jsonArray.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/nutrition")
                    .post(body)
                    .addHeader("apikey", SupabaseConfig.API_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(TotalStudent.this, "Failed to save nutrition data", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    runOnUiThread(() -> {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        if (response.isSuccessful()) {
                            Toast.makeText(TotalStudent.this, "Nutrition data added for selected students", Toast.LENGTH_SHORT).show();
                            for (StudentData student : studentList) {
                                student.setSelected(false);
                            }
                            adapter.notifyDataSetChanged();
                            btnMultiNutrition.setVisibility(View.GONE);
                            dialog.dismiss();
                        } else {
                            try {
                                Toast.makeText(TotalStudent.this, "Error: " + response.body().string(), Toast.LENGTH_LONG).show();
                            } catch (Exception ignored) {}
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    private boolean isAdminLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isAdminLoggedIn", false);
    }
}
