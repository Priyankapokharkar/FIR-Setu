package com.example.lawayerapp.models;

public class SuggestActsRequest {
    private String applicationText;

    public SuggestActsRequest(String applicationText) {
        this.applicationText = applicationText;
    }

    public String getApplicationText() { return applicationText; }
}
