package org.example.services;

import org.example.models.dtos.exportDtos.AdminStatsDto;
import org.example.models.dtos.exportDtos.ProcessingJobDto;

import java.util.List;

public interface AdminService {
    List<ProcessingJobDto> getFailedJobs();
    AdminStatsDto getSystemStats();
}
