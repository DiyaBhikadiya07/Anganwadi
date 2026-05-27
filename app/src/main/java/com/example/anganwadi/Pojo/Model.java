package com.example.anganwadi.Pojo;

public class Model {
    int imgAdmin;
    String total;

    public Model(int imgAdmin, String total) {
        this.imgAdmin = imgAdmin;
        this.total = total;
    }

    public int getImgAdmin() {
        return imgAdmin;
    }

    public void setImgAdmin(int imgAdmin) {
        this.imgAdmin = imgAdmin;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
