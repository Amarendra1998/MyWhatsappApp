package com.example.computer.mywhatsapp.Model;

public class User {
    private String Name,Password,Phone,Image;

    public User(String name, String password) {
        Name = name;
        Password = password;
    }

    public User() {
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getIsStaff() {
        return Image;
    }

    public void setIsStaff(String isStaff) {
        Image = isStaff;
    }
}
