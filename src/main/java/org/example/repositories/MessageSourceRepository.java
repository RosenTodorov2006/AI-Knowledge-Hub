package org.example.repositories;

import org.example.models.entities.MessageSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageSourceRepository extends JpaRepository<MessageSource, Long> {
}
