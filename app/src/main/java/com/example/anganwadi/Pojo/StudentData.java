package com.example.anganwadi.Pojo;

import java.io.Serializable;

public class StudentData implements Serializable {

    private String key; 
    private String Name, Age, FatherName, MotherName, Phone, City, Gender;
    private boolean isSelected = false;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public StudentData(String key, String name, String age, String fatherName, String motherName, String phone, String city, String gender) {
        this.Name = name;
        this.Age = age;
        this.FatherName = fatherName;
        this.MotherName = motherName;
        this.Phone = phone;
        this.City = city;
        this.key = key;
        this.Gender = gender;
    }

    public StudentData() {
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

    public String getFatherName() {
        return FatherName;
    }

    public void setFatherName(String fatherName) {
        FatherName = fatherName;
    }

    public String getMotherName() {
        return MotherName;
    }

    public void setMotherName(String motherName) {
        MotherName = motherName;
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
        this.Gender = gender;
    }
}
