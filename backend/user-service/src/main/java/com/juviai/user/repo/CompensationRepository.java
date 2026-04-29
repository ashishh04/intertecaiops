package com.juviai.user.repo;

import com.juviai.user.domain.Compensation;
import com.juviai.user.domain.CompensationType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompensationRepository extends JpaRepository<Compensation, UUID> {

    List<Compensation> findByEmployeeIdOrderByEffectiveStartDateDesc(UUID employeeId);

    Optional<Compensation> findFirstByEmployeeIdAndActiveTrueOrderByEffectiveStartDateDesc(UUID employeeId);

    boolean existsByEmployeeId(UUID employeeId);

    boolean existsByEmployeeIdAndType(UUID employeeId, CompensationType type);

    Optional<Compensation> findFirstByEmployeeIdAndTypeAndActiveTrueOrderByEffectiveStartDateDesc(UUID employeeId, CompensationType type);
}
