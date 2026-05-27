package com.example.anganwadi.Pojo;

public class StaffData {
    private String Name;
    private String Age;
    private String City;
    private String Qualification;
    private String Email;
    private String PhoneNo; // Changed to String to match Supabase 'text'
    private String key;
    private String imageUrl;
    private String gender;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public StaffData() {
    }
    public StaffData(String key, String name, String age, String city, String qualification, String email, String phoneNo, String imageUrl, String gender) {
        this.Name = name;
        this.key = key;
        this.Age = age;
        this.City = city;
        this.Qualification = qualification;
        this.Email = email;
        this.PhoneNo = phoneNo;
        this.imageUrl = imageUrl;
        this.gender = gender;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAge() {
        return Age;
    }

    public void setAge(String age) {
        Age = age;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getQualification() {
        return Qualification;
    }

    public void setQualification(String qualification) {
        Qualification = qualification;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPhoneNo() {
        return PhoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        PhoneNo = phoneNo;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
