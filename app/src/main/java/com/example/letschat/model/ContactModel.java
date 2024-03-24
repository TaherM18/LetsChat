package com.example.letschat.model;

public class ContactModel {
    private  String profileImage, name, number;

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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public ContactModel(String profileImage, String name, String number) {
        this.profileImage = profileImage;
        this.name = name;
        this.number = number;
    }
}
