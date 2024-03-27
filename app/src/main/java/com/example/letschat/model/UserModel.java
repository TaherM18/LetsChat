package com.example.letschat.model;

import com.google.firebase.Timestamp;

public class UserModel {
    private String userId;
    private String userName;
    private String bio;
    private String phone;
    private String profileImage;
    private Timestamp createdTimestamp;
    private String fcmToken;

    public UserModel() {}

    public UserModel(String userId, String userName, String phone, String profileImage, Timestamp createdTimestamp) {
        this.userId = userId;
        this.userName = userName;
        this.phone = phone;
        this.profileImage = profileImage;
        this.createdTimestamp = createdTimestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }


    @Override
    public String toString() {
        return "UserModel{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", bio='" + bio + '\'' +
                ", phone='" + phone + '\'' +
                ", profileImage='" + profileImage + '\'' +
                ", fcmToken='" + fcmToken + '\'' +
                ", createdTimestamp=" + createdTimestamp +
                '}';
    }
}
