package com.example.lawayerapp.models;

import java.util.List;

public class UpdateStatusRequest {
    private String status;
    private String rejectionReason;
    private String firText;
    private List<Act> appliedActs;

    public UpdateStatusRequest(String status, String rejectionReason, String firText, List<Act> appliedActs) {
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.firText = firText;
        this.appliedActs = appliedActs;
    }

    public String getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
    public String getFirText() { return firText; }
    public List<Act> getAppliedActs() { return appliedActs; }
}
