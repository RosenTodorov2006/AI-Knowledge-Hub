package org.example.repositories;

import org.example.models.entities.ProcessingJob;
import org.example.models.entities.enums.ProcessingJobStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessingJobRepository extends JpaRepository<ProcessingJob, Long> {
    Optional<ProcessingJob> findByDocumentId(Long documentId);
    List<ProcessingJob> findAllByStage(ProcessingJobStage  processingJobStage);
    long countByStageNotAndErrorMessageIsNull(ProcessingJobStage stage);
    long countByStage(ProcessingJobStage stage);
    long countByErrorMessageIsNotNull();
}
