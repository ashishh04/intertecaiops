package com.juviai.user.repo;

import com.juviai.user.domain.Role;
import com.juviai.user.domain.RoleModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    
    /**
     * Find a role by name (case-insensitive)
     * @param name the name of the role to find
     * @return an Optional containing the role if found, empty otherwise
     */
    @Query("SELECT r FROM Role r WHERE LOWER(r.name) = LOWER(:name)")
    Optional<Role> findByName(@Param("name") String name);
    List<Role> findAllByName(String name);
    
    Optional<Role> findByNameAndB2bUnitId(String name, UUID b2bUnitId);
    
    List<Role> findByB2bUnitId(UUID b2bUnitId);
    
    List<Role> findByNameIn(List<String> roleNames);
    
    @Query("SELECT r FROM Role r WHERE r.b2bUnitId IS NULL OR r.b2bUnitId = :b2bUnitId")
    List<Role> findAvailableRolesForBusiness(@Param("b2bUnitId") UUID b2bUnitId);
    
    boolean existsByName(String name);
    
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findRolesByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT r FROM Role r WHERE r.tenantId = :tenantId AND r.id IN :ids")
    List<Role> findByTenantIdAndIdIn(@Param("tenantId") String tenantId, @Param("ids") List<UUID> ids);
    
    /**
     * Find all roles that are not associated with any business unit (global roles).
     *
     * @return list of global roles
     */
    List<Role> findByB2bUnitIdIsNull();

    List<Role> findByB2bUnitIdAndNameNotIn(UUID b2bUnitId, List<String> excludedRoles);

    // ------------------------------------------------------------------
    // Module-scoped queries
    //
    // A role can belong to multiple modules (HRMS, ECOMMERCE, ...). The
    // UI uses these queries to show only the roles that make sense in the
    // module the admin is currently working in.
    // ------------------------------------------------------------------

    /**
     * Find all roles tagged with the given module (ignores b2bUnitId —
     * returns both system-wide and business-specific roles).
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.modules m WHERE m = :module")
    List<Role> findByModule(@Param("module") RoleModule module);

    /**
     * Find roles tagged with the given module that are either system-wide
     * (b2bUnitId is null) or scoped to the given business unit. This is
     * the main query a module-aware admin UI should use when populating
     * an "assign role" dropdown.
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.modules m "
            + "WHERE m = :module "
            + "AND (r.b2bUnitId IS NULL OR r.b2bUnitId = :b2bUnitId)")
    List<Role> findByModuleAndBusiness(@Param("module") RoleModule module,
                                       @Param("b2bUnitId") UUID b2bUnitId);

    /**
     * Find roles that belong to any of the given modules.
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.modules m WHERE m IN :modules")
    List<Role> findByModuleIn(@Param("modules") List<RoleModule> modules);

}
