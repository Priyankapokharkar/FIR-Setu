package com.example.lawayerapp.models;

public class CompareFirResponse {
    private boolean success;
    private ComparisonDetails report;
    private String message;

    public boolean isSuccess() { return success; }
    public ComparisonDetails getReport() { return report; }
    public String getMessage() { return message; }
}
