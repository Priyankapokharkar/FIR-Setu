package com.example.lawayerapp.models;

import java.util.List;

public class SuggestActsResponse {
    private boolean success;
    private List<Act> suggestedActs;
    private String message;

    public boolean isSuccess() { return success; }
    public List<Act> getSuggestedActs() { return suggestedActs; }
    public String getMessage() { return message; }
}
