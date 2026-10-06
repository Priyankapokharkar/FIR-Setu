package com.example.lawayerapp.models;

import java.util.List;

public class ApplicationsResponse {
    private boolean success;
    private List<ApplicationData> applications;
    private String message;

    public boolean isSuccess() { return success; }
    public List<ApplicationData> getApplications() { return applications; }
    public String getMessage() { return message; }
}
