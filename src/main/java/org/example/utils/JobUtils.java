package org.example.utils;

import org.example.models.dtos.exportDtos.ProcessingJobDto;
import org.example.models.entities.enums.ProcessingJobStage;

public final class JobUtils {
    private JobUtils() {}
    public static void enrichStageFlags(ProcessingJobDto dto, ProcessingJobStage currentStage) {
        int currentOrdinal = currentStage.ordinal();
        dto.setExtractPassed(currentOrdinal > ProcessingJobStage.PARSING.ordinal());
        dto.setChunkPassed(currentOrdinal > ProcessingJobStage.SPLIT.ordinal());
        dto.setEmbedPassed(currentStage == ProcessingJobStage.INDEXING
                || currentStage == ProcessingJobStage.COMPLETED);
    }
}
