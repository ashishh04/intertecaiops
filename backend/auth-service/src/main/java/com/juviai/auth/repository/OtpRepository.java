package com.juviai.auth.repository;

import com.juviai.auth.entity.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<OtpEntity, UUID> {

    Optional<OtpEntity> findTopByUserIdOrderByCreatedAtDesc(UUID userId);

    /** For phone-based OTP login — lookup before a userId exists. */
    Optional<OtpEntity> findTopByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);

    @Modifying
    @Query("DELETE FROM OtpEntity o WHERE o.phoneNumber = :phone")
    void deleteByPhoneNumber(@Param("phone") String phoneNumber);
}
