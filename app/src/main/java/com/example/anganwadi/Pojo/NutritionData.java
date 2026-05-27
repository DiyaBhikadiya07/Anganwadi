package com.example.anganwadi.Pojo;

public class NutritionData {
    private String food;
    private String date;
    private String imageUrl;

    public NutritionData() {
        // Default constructor required for calls to DataSnapshot.getValue(NutritionData.class)
    }

    public NutritionData(String food, String date) {
        this.food = food;
        this.date = date;
    }

    public NutritionData(String food, String date, String imageUrl) {
        this.food = food;
        this.date = date;
        this.imageUrl = imageUrl;
    }

    // Getters and setters
    public String getFood() {
        return food;
    }

    public void setFood(String food) {
        this.food = food;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
