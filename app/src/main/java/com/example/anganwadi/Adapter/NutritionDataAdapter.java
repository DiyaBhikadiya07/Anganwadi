package com.example.anganwadi.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anganwadi.Pojo.NutritionData;
import com.example.anganwadi.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class NutritionDataAdapter extends RecyclerView.Adapter<NutritionDataAdapter.ViewHolder> {

    private final List<NutritionData> nutritionDataList;

    public NutritionDataAdapter(List<NutritionData> nutritionDataList) {
        this.nutritionDataList = nutritionDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nutrition, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NutritionData nutritionData = nutritionDataList.get(position);
        holder.dateTextView.setText(nutritionData.getDate());
        holder.foodTextView.setText(nutritionData.getFood());

        if (nutritionData.getImageUrl() != null && !nutritionData.getImageUrl().isEmpty()) {
            holder.ivNutritionImage.setVisibility(View.VISIBLE);
            Picasso.get().load(nutritionData.getImageUrl()).into(holder.ivNutritionImage);

            holder.ivNutritionImage.setOnClickListener(v -> {
                showEnlargedImageDialog(holder.itemView.getContext(), nutritionData.getImageUrl());
            });
        } else {
            holder.ivNutritionImage.setVisibility(View.GONE);
        }
    }

    private void showEnlargedImageDialog(Context context, String imageUrl) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image_view);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageView ivEnlarged = dialog.findViewById(R.id.ivEnlargedImage);
        Button btnSave = dialog.findViewById(R.id.btnSaveInDialog);

        Picasso.get().load(imageUrl).into(ivEnlarged);

        btnSave.setOnClickListener(v -> {
            saveImageToGallery(context, imageUrl);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveImageToGallery(Context context, String imageUrl) {
        Picasso.get().load(imageUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                try {
                    String savedImagePath = MediaStore.Images.Media.insertImage(
                            context.getContentResolver(),
                            bitmap,
                            "nutrition_" + System.currentTimeMillis(),
                            "Nutrition Image"
                    );
                    if (savedImagePath != null) {
                        Toast.makeText(context, "Photo saved to gallery", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to save photo", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Error saving photo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Toast.makeText(context, "Failed to download image", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return nutritionDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, foodTextView;
        ImageView ivNutritionImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            foodTextView = itemView.findViewById(R.id.foodTextView);
            ivNutritionImage = itemView.findViewById(R.id.ivNutritionImage);
        }
    }
}
