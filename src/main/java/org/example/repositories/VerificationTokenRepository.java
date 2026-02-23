package org.example.repositories;

import org.example.models.entities.UserEntity;
import org.example.models.entities.VerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationTokenEntity, Long> {
    Optional<VerificationTokenEntity> findByUser(UserEntity user);

    Optional<VerificationTokenEntity> findByToken(String token);
}
