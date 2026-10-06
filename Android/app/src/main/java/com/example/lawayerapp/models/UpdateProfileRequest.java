package com.example.lawayerapp.models;

public class UpdateProfileRequest {
    private String name;
    private String location;
    private String badgeNumber;
    private String password;

    public UpdateProfileRequest(String name, String location, String badgeNumber, String password) {
        this.name = name;
        this.location = location;
        this.badgeNumber = badgeNumber;
        this.password = password;
    }

    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getBadgeNumber() { return badgeNumber; }
    public String getPassword() { return password; }
}
