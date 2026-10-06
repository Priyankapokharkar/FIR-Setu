package com.example.lawayerapp.models;

public class Act {
    private String actName;
    private String description;

    public Act(String actName, String description) {
        this.actName = actName;
        this.description = description;
    }

    public String getActName() { return actName; }
    public String getDescription() { return description; }
}
