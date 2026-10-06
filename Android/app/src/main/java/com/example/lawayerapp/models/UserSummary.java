package com.example.lawayerapp.models;

import androidx.annotation.NonNull;

public class UserSummary {
    private String _id;
    private String name;
    private String email;

    public String get_id() { return _id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    @NonNull
    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
}
