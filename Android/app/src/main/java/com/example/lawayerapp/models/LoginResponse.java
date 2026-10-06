package com.example.lawayerapp.models;

public class LoginResponse {
    private boolean success;
    private String _id;
    private String name;
    private String email;
    private String role;
    private String token;
    private String message;

    public boolean isSuccess() { return success; }
    public String getId() { return _id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getToken() { return token; }
    public String getMessage() { return message; }
}
