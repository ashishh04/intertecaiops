package com.juviai.leave.repo;

import com.juviai.leave.domain.LeavePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, UUID> {

    List<LeavePolicy> findByB2bUnitIdAndActiveTrue(UUID b2bUnitId);

    List<LeavePolicy> findByLeaveTypeIdAndActiveTrue(UUID leaveTypeId);

    /**
     * Find active policies for a given leave type and org unit on a specific date.
     * Used during leave balance initialization.
     */
    @Query("""
        SELECT lp FROM LeavePolicy lp
        WHERE lp.leaveType.id = :leaveTypeId
          AND lp.b2bUnitId    = :b2bUnitId
          AND lp.active       = true
          AND lp.effectiveFrom <= :asOf
          AND (lp.effectiveTo IS NULL OR lp.effectiveTo >= :asOf)
    """)
    List<LeavePolicy> findActivePolicies(
            @Param("leaveTypeId") UUID leaveTypeId,
            @Param("b2bUnitId")   UUID b2bUnitId,
            @Param("asOf")        LocalDate asOf);

    @Query("""
        SELECT lp FROM LeavePolicy lp
        WHERE lp.b2bUnitId = :b2bUnitId
          AND lp.active    = true
          AND lp.effectiveFrom <= :asOf
          AND (lp.effectiveTo IS NULL OR lp.effectiveTo >= :asOf)
    """)
    List<LeavePolicy> findAllActivePoliciesForOrg(
            @Param("b2bUnitId") UUID b2bUnitId,
            @Param("asOf")      LocalDate asOf);
}
