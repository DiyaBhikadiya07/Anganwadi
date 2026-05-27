package com.example.anganwadi.Pojo;

public class Register {

    private String Name;
    private String Email;
    private String CreatePass;
    private String RePass;
    private String Phone;
    private String City;
    private String Gender;
    private String imageUrl;

    private String key;

    public Register() {
    }

    public Register(String name, String email, String createPass, String rePass, String phone, String city, String gender, String imageUrl, String key) {
        this.Name = name;
        this.Email = email;
        this.CreatePass = createPass;
        this.RePass = rePass;
        this.Phone = phone;
        this.City = city;
        this.Gender = gender;
        this.imageUrl = imageUrl;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getCreatePass() {
        return CreatePass;
    }

    public void setCreatePass(String createPass) {
        CreatePass = createPass;
    }

    public String getRePass() {
        return RePass;
    }

    public void setRePass(String rePass) {
        RePass = rePass;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }
}
