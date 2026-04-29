package com.juviai.user.repo;




import com.juviai.user.domain.EmployeeOrgBand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmployeeBandRepository extends JpaRepository<EmployeeOrgBand, UUID> {
    List<EmployeeOrgBand> findByB2bUnit_Id(UUID b2bUnitId);
}
