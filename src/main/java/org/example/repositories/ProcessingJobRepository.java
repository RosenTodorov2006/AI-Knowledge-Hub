package org.example.repositories;

import org.example.models.entities.ProcessingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessingJobRepository extends JpaRepository<ProcessingJob, Long> {
    Optional<ProcessingJob> findByDocumentId(Long documentId);
}
