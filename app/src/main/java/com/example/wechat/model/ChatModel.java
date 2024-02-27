package com.example.wechat.model;

public class ChatModel {
    public ChatModel(String username, String message, String time, int messageCount) {
        this.username = username;
        this.message = message;
        this.time = time;
        this.messageCount = messageCount;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public int getMessageCount() {
        return messageCount;
    }

    private String username, message, time;
    private int messageCount;

}
