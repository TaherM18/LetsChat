package com.example.letschat.model;

public class ContactModel {
    private  String photoUri, name, number;
    long date;
    private int callType;

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public ContactModel(String photoUri, String name, String number, long date, int callType) {
        this.photoUri = photoUri;
        this.name = name;
        this.number = number;
        this.date = date;
        this.callType = callType;
    }

    @Override
    public String toString() {
        return "ContactModel{" +
                "photoUri='" + photoUri + '\'' +
                ", name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", date='" + date + '\'' +
                ", callType=" + callType +
                '}';
    }
}
