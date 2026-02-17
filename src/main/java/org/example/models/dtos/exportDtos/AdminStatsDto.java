package org.example.models.dtos.exportDtos;

public class AdminStatsDto {
    private long currentlyProcessing;
    private double successRate;
    private String totalVectors;
    private long totalChats;
    private long activeUsers;

    public AdminStatsDto(long currentlyProcessing, double successRate, String totalVectors, long totalChats, long activeUsers) {
        this.currentlyProcessing = currentlyProcessing;
        this.successRate = successRate;
        this.totalVectors = totalVectors;
        this.totalChats = totalChats;
        this.activeUsers = activeUsers;
    }

    public long getTotalChats() {
        return totalChats;
    }

    public void setTotalChats(long totalChats) {
        this.totalChats = totalChats;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getCurrentlyProcessing() {
        return currentlyProcessing;
    }

    public void setCurrentlyProcessing(long currentlyProcessing) {
        this.currentlyProcessing = currentlyProcessing;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public String getTotalVectors() {
        return totalVectors;
    }

    public void setTotalVectors(String totalVectors) {
        this.totalVectors = totalVectors;
    }
}
