package com.juviai.leave.repo;

import com.juviai.leave.domain.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, UUID> {
    List<LeaveType> findByB2bUnitIdAndActiveTrueOrderByName(UUID b2bUnitId);
    Optional<LeaveType> findByB2bUnitIdAndCodeIgnoreCase(UUID b2bUnitId, String code);
    boolean existsByB2bUnitIdAndCodeIgnoreCase(UUID b2bUnitId, String code);
}
