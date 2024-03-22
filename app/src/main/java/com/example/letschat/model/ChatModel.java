package com.example.letschat.model;

public class ChatModel {

    private String profileUrl, username, message, datetime;
    private String messageCount, userId;

    public ChatModel(String profileUrl,String username, String message, String datetime, String messageCount) {
        this.profileUrl = profileUrl;
        this.username = username;
        this.message = message;
        this.datetime = datetime;
        this.messageCount = messageCount;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public String getDateTime() {
        return datetime;
    }

    public String getMessageCount() {
        return messageCount;
    }
    public String getProfileUrl() { return profileUrl; }

}
