package com.example.lawayerapp.models;

import java.util.List;

public class ComparisonDetails {
    private String status;
    private String originalCategory;
    private String firCategory;
    private boolean categoryMatch;
    private String recommendedIpc;
    private String recommendedBns;
    private String firIpcBns;
    private boolean sectionMatch;
    private List<String> keyConcepts;
    private List<String> missingConcepts;
    private String summaryText;
    private String originalDesc;
    private String firText;
    private int accuracyScore;
    private String source;

    public String getStatus() { return status; }
    public int getAccuracyScore() { return accuracyScore; }
    public String getOriginalCategory() { return originalCategory; }
    public String getFirCategory() { return firCategory; }
    public boolean isCategoryMatch() { return categoryMatch; }
    public String getRecommendedIpc() { return recommendedIpc; }
    public String getRecommendedBns() { return recommendedBns; }
    public String getFirIpcBns() { return firIpcBns; }
    public boolean isSectionMatch() { return sectionMatch; }
    public List<String> getKeyConcepts() { return keyConcepts; }
    public List<String> getMissingConcepts() { return missingConcepts; }
    public String getSummaryText() { return summaryText; }
    public String getOriginalDesc() { return originalDesc; }
    public String getFirText() { return firText; }
    public String getSource() { return source; }
}
