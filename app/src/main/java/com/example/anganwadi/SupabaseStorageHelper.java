package com.example.anganwadi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.*;
import org.json.JSONObject;

public class SupabaseStorageHelper {

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public static void uploadImage(Context context, Uri uri, String fileName, UploadCallback callback) {
        try {
            // 1. Read image data
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                callback.onError("Could not open image file");
                return;
            }
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) {
                callback.onError("Failed to decode image");
                return;
            }

            // 2. Compress image (Fast upload)
            Bitmap scaledBitmap = scaleBitmap(bitmap, 1024);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] bytes = baos.toByteArray();

            // 3. Prepare Supabase Request
            RequestBody body = RequestBody.create(bytes, MediaType.parse("image/jpeg"));
            
            // Bucket: profile-images (Ensure this exists in Supabase Storage)
            String uploadUrl = SupabaseConfig.SUPABASE_URL + "/storage/v1/object/profile-images/" + fileName;

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

            // We use PUT + x-upsert for the most reliable direct upload
            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .put(body) 
                    .addHeader("apikey", SupabaseConfig.API_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                    .addHeader("Content-Type", "image/jpeg")
                    .addHeader("x-upsert", "true")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    callback.onError("Network Error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws java.io.IOException {
                    String resBody = response.body() != null ? response.body().string() : "";
                    if (response.isSuccessful()) {
                        // Public URL construction
                        String imageUrl = SupabaseConfig.SUPABASE_URL + "/storage/v1/object/public/profile-images/" + fileName;
                        callback.onSuccess(imageUrl);
                    } else {
                        Log.e("SupabaseUpload", "Error " + response.code() + ": " + resBody);
                        String errorMsg = "Upload failed (" + response.code() + ")";
                        try {
                            JSONObject json = new JSONObject(resBody);
                            errorMsg = json.optString("message", errorMsg);
                        } catch (Exception ignored) {}
                        
                        callback.onError(errorMsg);
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Error: " + e.getMessage());
        }
    }

    private static Bitmap scaleBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= maxSize && height <= maxSize) return bitmap;
        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        return Bitmap.createScaledBitmap(bitmap, Math.round(ratio * width), Math.round(ratio * height), true);
    }
}
