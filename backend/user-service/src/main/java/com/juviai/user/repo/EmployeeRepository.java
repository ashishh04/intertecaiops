package com.juviai.user.repo;

import com.juviai.user.domain.Employee;
import com.juviai.user.domain.EmploymentType;
import com.juviai.user.dto.DesignationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.UUID;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    @Query(value = """
            select e from Employee e
            left join e.reportingManager rm
            left join e.designation d
            left join e.department dep
            where e.b2bUnit.id = :b2bUnitId
            and (
                  :q is null or lower(e.employeeCode) like lower(concat('%', :q, '%'))
                         or lower(e.firstName) like lower(concat('%', :q, '%'))
                         or lower(e.lastName) like lower(concat('%', :q, '%'))
                         or lower(e.email) like lower(concat('%', :q, '%'))
                         or (d is not null and lower(d.name) like lower(concat('%', :q, '%')))
                         or (dep is not null and lower(dep.name) like lower(concat('%', :q, '%')))
                         or (rm is not null and lower(rm.firstName) like lower(concat('%', :q, '%')))
                )
            and (:etype is null or e.employmentType = :etype)
            """,
            countQuery = """
            select count(e) from Employee e
            left join e.reportingManager rm
            left join e.designation d
            left join e.department dep
            where e.b2bUnit.id = :b2bUnitId
            and (
                  :q is null or lower(e.employeeCode) like lower(concat('%', :q, '%'))
                         or lower(e.firstName) like lower(concat('%', :q, '%'))
                         or lower(e.lastName) like lower(concat('%', :q, '%'))
                         or lower(e.email) like lower(concat('%', :q, '%'))
                         or (d is not null and lower(d.name) like lower(concat('%', :q, '%')))
                         or (dep is not null and lower(dep.name) like lower(concat('%', :q, '%')))
                         or (rm is not null and lower(rm.firstName) like lower(concat('%', :q, '%')))
                )
            and (:etype is null or e.employmentType = :etype)
            """)
    Page<Employee> search(@Param("b2bUnitId") UUID b2bUnitId,
                         @Param("q") String q,
                         @Param("etype") EmploymentType employmentType,
                         Pageable pageable);

    @Query(value = """
            select concat(email, ' | tenant=', ifnull(tenant_id, 'NULL')) from users
            where b2b_unit_id is not null
              and bin_to_uuid(b2b_unit_id) = :b2bUnitId
              and dtype = 'EMPLOYEE'
            order by email
            """, nativeQuery = true)
    List<String> debugEmployeeEmailsByB2bUnitId(@Param("b2bUnitId") String b2bUnitId);

    @Query(value = """
            select concat('id=', bin_to_uuid(id), ' | tenant=', ifnull(tenant_id, 'NULL'), ' | name=', ifnull(name, 'NULL'))
            from b2b_unit
            where bin_to_uuid(id) = :b2bUnitId
            """, nativeQuery = true)
    List<String> debugB2bUnitRow(@Param("b2bUnitId") String b2bUnitId);

    @Query("select e from Employee e where e.b2bUnit.id = :b2bUnitId")
    List<Employee> findByB2bUnitId(@Param("b2bUnitId") UUID b2bUnitId);

    void deleteById(@NonNull UUID userId);

    @Query("SELECT new com.juviai.user.dto.DesignationDTO(" +
            "d.id, d.name, b.name, COUNT(e)) " +
            "FROM Employee e " +
            "JOIN e.designation d " +
            "LEFT JOIN e.band b " +      // allow null bands
            "WHERE d.b2bUnitId = :b2bUnitId " +  // moved filter to designation
            "GROUP BY d.id, d.name, b.name " +
            "ORDER BY d.name")
    List<DesignationDTO> findDesignationWithBandAndResourceCount(@Param("b2bUnitId") UUID b2bUnitId);
}


