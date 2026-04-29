package com.juviai.auth.repo;

import com.juviai.auth.domain.RefreshTokenSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, UUID> {
    List<RefreshTokenSession> findAllByUserIdAndRevokedFalse(UUID userId);

    Optional<RefreshTokenSession> findByIdAndRevokedFalse(UUID id);

    long deleteByExpiryDateBefore(Instant now);
}
