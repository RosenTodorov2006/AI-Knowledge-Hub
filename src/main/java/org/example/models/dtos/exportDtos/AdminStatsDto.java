package org.example.models.dtos.exportDtos;

public class AdminStatsDto {
    private long currentlyProcessing;
    private double successRate;
    private String totalVectors;

    public AdminStatsDto(long currentlyProcessing, double successRate, String totalVectors) {
        this.currentlyProcessing = currentlyProcessing;
        this.successRate = successRate;
        this.totalVectors = totalVectors;
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
