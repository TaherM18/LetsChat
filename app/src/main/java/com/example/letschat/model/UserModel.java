package com.example.letschat.model;

import com.google.firebase.Timestamp;

public class UserModel {
    private String userID;
    private String userName;
    private String phone;
    private Timestamp createdTimestamp;
    private String imageProfile;
    private String imageCover;
    private String email;
    private String dateOfBirth;
    private String gender;
    private String status;
    private String bio;

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return phone;
    }

    public String getImageProfile() {
        return imageProfile;
    }

    public String getImageCover() {
        return imageCover;
    }

    public String getEmail() {
        return email;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getStatus() {
        return status;
    }

    public String getBio() {
        return bio;
    }

    public UserModel() {}

    public UserModel(String userID, String userName, String userPhone, String imageProfile, String imageCover, String email, String dateOfBirth, String gender, String status, String bio) {
        this.userID = userID;
        this.userName = userName;
        this.phone = userPhone;
        this.imageProfile = imageProfile;
        this.imageCover = imageCover;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.status = status;
        this.bio = bio;
    }

    public UserModel(String userName, String phone, Timestamp createdTimestamp) {
        this.userName = userName;
        this.phone = phone;
        this.createdTimestamp = createdTimestamp;
    }
}
