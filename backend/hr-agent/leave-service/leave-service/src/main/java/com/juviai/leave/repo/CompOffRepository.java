package com.juviai.leave.repo;

import com.juviai.leave.domain.CompOff;
import com.juviai.leave.domain.CompOffStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CompOffRepository extends JpaRepository<CompOff, UUID> {

    List<CompOff> findByEmployeeIdOrderByWorkedDateDesc(UUID employeeId);

    List<CompOff> findByEmployeeIdAndStatusOrderByWorkedDateDesc(UUID employeeId, CompOffStatus status);

    List<CompOff> findByStatusOrderByCreatedAtAsc(CompOffStatus status);

    /** Sum available (approved, non-expired) comp-off credits for an employee */
    @Query("""
        SELECT COALESCE(SUM(c.credits), 0) FROM CompOff c
        WHERE c.employeeId = :employeeId
          AND c.status = 'APPROVED'
          AND (c.expiresAt IS NULL OR c.expiresAt >= :today)
    """)
    BigDecimal sumAvailableCredits(
            @Param("employeeId") UUID employeeId,
            @Param("today")      LocalDate today);

    /** Find comp-offs that have expired but are still in APPROVED state */
    @Query("""
        SELECT c FROM CompOff c
        WHERE c.status = 'APPROVED'
          AND c.expiresAt IS NOT NULL
          AND c.expiresAt < :today
    """)
    List<CompOff> findExpired(@Param("today") LocalDate today);
}
