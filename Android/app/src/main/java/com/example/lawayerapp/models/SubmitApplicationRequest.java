package com.example.lawayerapp.models;

import java.util.List;

public class SubmitApplicationRequest {
    private String description;
    private List<Act> suggestedActs;

    public SubmitApplicationRequest(String description, List<Act> suggestedActs) {
        this.description = description;
        this.suggestedActs = suggestedActs;
    }

    public String getDescription() { return description; }
    public List<Act> getSuggestedActs() { return suggestedActs; }
}
