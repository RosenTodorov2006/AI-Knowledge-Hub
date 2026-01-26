package org.example.repositories;

import aj.org.objectweb.asm.commons.Remapper;
import org.example.models.entities.Chat;
import org.example.models.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findAllByUserEntityId(Long userId);

    Optional<Chat> findByDocument(Document document);
    Optional<Chat> findByIdAndUserEntityEmail(Long id, String email);
}
