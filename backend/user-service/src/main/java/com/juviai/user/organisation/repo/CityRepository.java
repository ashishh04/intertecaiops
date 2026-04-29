package com.juviai.user.organisation.repo;

import com.juviai.user.organisation.domain.City;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CityRepository extends JpaRepository<City, UUID> {
    Optional<City> findByCode(String code);
    List<City> findByActiveTrueOrderByNameAsc();

    Page<City> findByTenantId(String tenantId, Pageable pageable);

    @Query("""
            select c from City c
            where c.tenantId = :tenantId
              and (
                   :q is null
                   or lower(c.name) like lower(concat('%', :q, '%'))
                   or lower(c.code) like lower(concat('%', :q, '%'))
              )
            """)
    Page<City> searchByTenantId(@Param("tenantId") String tenantId, @Param("q") String q, Pageable pageable);

    @Query("""
            select c from City c
            join c.state s
            where lower(s.code) = lower(:stateCode)
              and (
                   :q is null
                   or lower(c.name) like lower(concat('%', :q, '%'))
                   or lower(c.code) like lower(concat('%', :q, '%'))
              )
            order by c.name asc
            """)
    List<City> findByStateCodeAndQuery(@Param("stateCode") String stateCode, @Param("q") String q);
}
