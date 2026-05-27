package com.example.anganwadi;

import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

public class AuthService {

    public interface AuthCallback {
        void onSuccess();
        void onError(String error);
    }

    public static void login(String email, String password, AuthCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("password", password);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/auth/v1/token?grant_type=password")
                    .post(body)
                    .addHeader("apikey", SupabaseConfig.API_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network Error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            callback.onError(errorJson.optString("error_description", "Invalid Email or Password"));
                        } catch (Exception e) {
                            callback.onError("Login Failed");
                        }
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Error: " + e.getMessage());
        }
    }

    public static void register(String email, String password, AuthCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("password", password);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/auth/v1/signup")
                    .post(body)
                    .addHeader("apikey", SupabaseConfig.API_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network Error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            String msg = errorJson.optString("msg", errorJson.optString("message", "Registration Failed"));
                            callback.onError(msg);
                        } catch (Exception e) {
                            callback.onError("Registration Failed: " + response.code());
                        }
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Error: " + e.getMessage());
        }
    }

    public static void resetPassword(String email, AuthCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            
            // Proper URL building with encoding for the redirect_to parameter
            // Note: 'anganwadi://reset' must be added to Supabase 'Redirect URLs' settings
            HttpUrl url = HttpUrl.parse(SupabaseConfig.SUPABASE_URL + "/auth/v1/recover")
                    .newBuilder()
                    .addQueryParameter("redirect_to", "anganwadi://reset")
                    .build();

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("apikey", SupabaseConfig.API_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network Error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            String msg = errorJson.optString("msg", errorJson.optString("message", "Error sending reset link"));
                            callback.onError(msg);
                        } catch (Exception e) {
                            callback.onError("Error: " + response.code());
                        }
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Error: " + e.getMessage());
        }
    }
}
