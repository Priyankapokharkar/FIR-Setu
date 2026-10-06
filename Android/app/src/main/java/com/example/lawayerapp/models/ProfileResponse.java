package com.example.lawayerapp.models;

public class ProfileResponse {
    private boolean success;
    private UserData user;

    public boolean isSuccess() { return success; }
    public UserData getUser() { return user; }

    public static class UserData {
        private String _id;
        private String name;
        private String email;
        private String role;
        private String location;
        private String badgeNumber;

        public String get_id() { return _id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getLocation() { return location; }
        public String getBadgeNumber() { return badgeNumber; }
    }
}
