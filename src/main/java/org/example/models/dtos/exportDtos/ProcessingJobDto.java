package org.example.models.dtos.exportDtos;

public class ProcessingJobDto {
    private String jobId;
    private String fileName;
    private String userEmail;
    private String error;
    private String timeAgo;

    private boolean extractPassed;
    private boolean chunkPassed;
    private boolean embedPassed;

    public ProcessingJobDto(String jobId, String fileName, String userEmail, String error, String timeAgo, boolean extractPassed, boolean chunkPassed, boolean embedPassed) {
        this.jobId = jobId;
        this.fileName = fileName;
        this.userEmail = userEmail;
        this.error = error;
        this.timeAgo = timeAgo;
        this.extractPassed = extractPassed;
        this.chunkPassed = chunkPassed;
        this.embedPassed = embedPassed;
    }

    public ProcessingJobDto() {
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public boolean isExtractPassed() {
        return extractPassed;
    }

    public void setExtractPassed(boolean extractPassed) {
        this.extractPassed = extractPassed;
    }

    public boolean isChunkPassed() {
        return chunkPassed;
    }

    public void setChunkPassed(boolean chunkPassed) {
        this.chunkPassed = chunkPassed;
    }

    public boolean isEmbedPassed() {
        return embedPassed;
    }

    public void setEmbedPassed(boolean embedPassed) {
        this.embedPassed = embedPassed;
    }

}
