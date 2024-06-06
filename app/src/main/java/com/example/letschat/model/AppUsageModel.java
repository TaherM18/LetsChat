package com.example.letschat.model;

import com.google.firebase.Timestamp;

public class AppUsageModel {
    private String userId;
    private Timestamp timestamp;
    private boolean isAppStart;
    private long startTime;

    public AppUsageModel() {
        // Required empty public constructor for Firestore
    }

    public AppUsageModel(String userId, Timestamp timestamp, boolean isAppStart, long startTime) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.isAppStart = isAppStart;
        this.startTime = startTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAppStart() {
        return isAppStart;
    }

    public void setAppStart(boolean appStart) {
        isAppStart = appStart;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
