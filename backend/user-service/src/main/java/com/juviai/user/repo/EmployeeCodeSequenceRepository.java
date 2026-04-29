package com.juviai.user.repo;

import com.juviai.user.domain.EmployeeCodeSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeCodeSequenceRepository extends JpaRepository<EmployeeCodeSequence, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from EmployeeCodeSequence s where s.b2bUnitId = :b2bUnitId")
    Optional<EmployeeCodeSequence> findByB2bUnitIdForUpdate(@Param("b2bUnitId") UUID b2bUnitId);

    Optional<EmployeeCodeSequence> findByB2bUnitId(UUID b2bUnitId);
}
