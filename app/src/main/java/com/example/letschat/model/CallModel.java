package com.example.letschat.model;

public class CallModel {
    private String userID;
    private String profileUrl;
    private String username;
    private String status;
    private String datetime;
    private String contact;
    private String callType;


    public String getProfileUrl() {
        return profileUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }

    public String getDatetime() {
        return datetime;
    }

    public String getContact() {
        return contact;
    }

    public String getCallType() {
        return callType;
    }


    public CallModel(String profileUrl, String username, String callType, String datetime) {
        this.profileUrl = profileUrl;
        this.username = username;
        this.callType = callType;
        this.datetime = datetime;
    }
}
