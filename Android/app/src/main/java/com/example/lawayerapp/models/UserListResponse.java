package com.example.lawayerapp.models;

import java.util.List;

public class UserListResponse {
    private boolean success;
    private List<UserSummary> citizens;

    public boolean isSuccess() { return success; }
    public List<UserSummary> getCitizens() { return citizens; }
}
