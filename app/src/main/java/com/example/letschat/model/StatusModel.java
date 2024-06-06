package com.example.letschat.model;

import com.firebase.ui.firestore.FirestoreArray;
import com.google.firebase.Timestamp;

import java.util.List;

public class StatusModel {

    private String userId;
    private String userName;
    private String profileImage;
    private String lastStatusImage;
    private Timestamp lastUpdated;
    private List<StatusImage> statusImageList;

    public StatusModel() {
    }

    public StatusModel(String userId, String userName, String profileImage, Timestamp lastUpdated, List<StatusImage> statusImageList) {
        this.userId = userId;
        this.userName = userName;
        this.lastStatusImage = profileImage;
        this.lastUpdated = lastUpdated;
        this.statusImageList = statusImageList;
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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getLastStatusImage() {
        return lastStatusImage;
    }

    public void setLastStatusImage(String lastStatusImage) {
        this.lastStatusImage = lastStatusImage;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<StatusImage> getStatusImageList() {
        return statusImageList;
    }

    public void setStatusImageList(List<StatusImage> statusImageList) {
        this.statusImageList = statusImageList;
    }

    @Override
    public String toString() {
        return "StatusModel{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", profileImage='" + profileImage + '\'' +
                ", lastStatusImage='" + lastStatusImage + '\'' +
                ", lastUpdated=" + lastUpdated +
                ", statusImageList=" + statusImageList +
                '}';
    }
}
