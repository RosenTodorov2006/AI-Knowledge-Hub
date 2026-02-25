package org.example.repositories;

import org.example.models.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);
    @Modifying
    @Query("UPDATE UserEntity u " +
            "set u.active = false " +
            "WHERE u.active = true " +
            "AND u.createdAt < :threshold " +
            "AND NOT EXISTS (SELECT c FROM Chat c WHERE c.userEntity = u AND c.lastMessageAt > :threshold)")
    List<UserEntity> findInactiveUsers(@Param("threshold") LocalDateTime threshold);
    long countByActiveTrue();
}

