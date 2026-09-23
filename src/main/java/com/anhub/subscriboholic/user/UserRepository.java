package com.anhub.subscriboholic.user;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM User u 
        WHERE u.emailVerified = false 
          AND u.createdAt < :cutoffDate
    """)
    int deleteUnverifiedUsersOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}
