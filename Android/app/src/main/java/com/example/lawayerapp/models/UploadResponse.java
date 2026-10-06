package com.example.lawayerapp.models;

public class UploadResponse {
    private boolean success;
    private String message;
    private String fileUrl;
    private ComparisonDetails report;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getFileUrl() { return fileUrl; }
    public ComparisonDetails getReport() { return report; }
}
