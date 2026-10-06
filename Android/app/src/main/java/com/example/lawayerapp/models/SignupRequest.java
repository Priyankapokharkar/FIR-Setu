package com.example.lawayerapp.models;

public class SignupRequest {
    private String name;
    private String email;
    private String password;
    private String role;
    private String badgeNumber;

    public SignupRequest(String name, String email, String password, String role, String badgeNumber) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.badgeNumber = badgeNumber;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getBadgeNumber() { return badgeNumber; }
}
