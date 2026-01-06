package org.example.repositories;

import org.example.models.entities.MessageContextSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageContextSourceRepository extends JpaRepository<MessageContextSource, Long> {
}
