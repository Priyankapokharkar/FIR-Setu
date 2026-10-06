package com.example.lawayerapp.models;

import java.util.List;

public class DraftFirResponse {
    private boolean success;
    private String draftedText;
    private List<String> acts;
    private String message;

    public boolean isSuccess() { return success; }
    public String getDraftedText() { return draftedText; }
    public List<String> getActs() { return acts; }
    public String getMessage() { return message; }
}
