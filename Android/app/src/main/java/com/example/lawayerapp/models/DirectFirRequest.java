package com.example.lawayerapp.models;

import java.util.List;

public class DirectFirRequest {
    private String targetUserId;
    private String firText;
    private List<Act> appliedActs;

    public DirectFirRequest(String targetUserId, String firText, List<Act> appliedActs) {
        this.targetUserId = targetUserId;
        this.firText = firText;
        this.appliedActs = appliedActs;
    }

    public String getTargetUserId() { return targetUserId; }
    public String getFirText() { return firText; }
    public List<Act> getAppliedActs() { return appliedActs; }
}
