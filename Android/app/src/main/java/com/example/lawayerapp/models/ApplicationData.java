package com.example.lawayerapp.models;

import java.util.List;

public class ApplicationData {
    private String _id;
    private String description;
    private String status;
    private String rejectionReason;
    private Constable assignedConstable;
    private List<Act> suggestedActs;
    private String createdAt;
    private String generatedFirUrl;
    private boolean isOfflineFir;
    private FirData linkedFir;

    public String getId() { return _id; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
    public Constable getAssignedConstable() { return assignedConstable; }
    public List<Act> getSuggestedActs() { return suggestedActs; }
    public String getCreatedAt() { return createdAt; }
    public String getGeneratedFirUrl() { return generatedFirUrl; }
    public boolean isOfflineFir() { return isOfflineFir; }
    public FirData getLinkedFir() { return linkedFir; }

    public static class Constable {
        private String _id;
        private String name;
        private String badgeNumber;

        public String getId() { return _id; }
        public String getName() { return name; }
        public String getBadgeNumber() { return badgeNumber; }
    }

    public static class FirData {
        private String _id;
        private String firText;
        private List<Act> appliedActs;

        public String getId() { return _id; }
        public String getFirText() { return firText; }
        public List<Act> getAppliedActs() { return appliedActs; }
    }
}
