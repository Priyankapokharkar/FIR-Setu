package com.example.lawayerapp.models;

import java.util.List;

public class AIValidationResponse {
    private boolean success;
    private boolean isComplete;
    private List<String> missingPoints;
    private String message;

    public boolean isSuccess() { return success; }
    public boolean isComplete() { return isComplete; }
    public List<String> getMissingPoints() { return missingPoints; }
    public String getMessage() { return message; }
}
