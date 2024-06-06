package com.example.letschat.model;

import com.example.letschat.utils.FirebaseUtil;
import com.google.firebase.Timestamp;

public class MessageModel {

    public enum MessageType {
        TEXT,
        URL,
        LOCATION,
        CONTACT,
        IMAGE,
        VIDEO,
        DOCUMENT,
        AUDIO,
        LOADING
    }

    public enum Reaction {
        LIKE,
        LOVE,
        LAUGH,
        WOW,
        SAD,
        ANGRY
    }
    private String messageId;
    private String message;
    private MessageType messageType;
    private String senderId;
    private int reaction;
    private String fileUrl;
    private String fileType;
    private boolean isRead;
    private Timestamp timestamp;


    public MessageModel() {
    }

    public MessageModel(String message, MessageType messageType) {
        this.message = message;
        this.messageType = messageType;
        this.senderId = FirebaseUtil.currentUserId();
        this.reaction = -1;
        this.timestamp = Timestamp.now();
        this.isRead = false;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int getReaction() {
        return reaction;
    }

    public void setReaction(int reaction) {
        this.reaction = reaction;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
