package com.juviai.user.repo;

import com.juviai.user.domain.RoleAssignment;
import com.juviai.user.domain.ScopeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for the generic {@link RoleAssignment} table.
 *
 * <p>Only active (and non-expired) rows should be considered when making
 * authorization decisions, so every "is this in effect?" style query
 * filters on {@code ra.active = true}.</p>
 */
@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, UUID> {

    /** Login fan-out: all active assignments for a user. */
    @Query("SELECT ra FROM RoleAssignment ra "
            + "WHERE ra.user.id = :userId AND ra.active = true")
    List<RoleAssignment> findActiveForUser(@Param("userId") UUID userId);

    /** Who has a role at this scope (active only)? */
    @Query("SELECT ra FROM RoleAssignment ra "
            + "WHERE ra.scopeType = :scopeType "
            + "AND ((:scopeId IS NULL AND ra.scopeId IS NULL) OR ra.scopeId = :scopeId) "
            + "AND ra.active = true")
    List<RoleAssignment> findActiveAtScope(@Param("scopeType") ScopeType scopeType,
                                           @Param("scopeId") UUID scopeId);

    /** Fetch every assignment (active + inactive) for a user — admin tooling. */
    List<RoleAssignment> findByUserId(UUID userId);

    /** Specific-assignment lookup used by assign/revoke and "has role?" checks. */
    @Query("SELECT ra FROM RoleAssignment ra "
            + "WHERE ra.user.id = :userId "
            + "AND ra.role.id = :roleId "
            + "AND ra.scopeType = :scopeType "
            + "AND ((:scopeId IS NULL AND ra.scopeId IS NULL) OR ra.scopeId = :scopeId)")
    Optional<RoleAssignment> findOne(@Param("userId") UUID userId,
                                     @Param("roleId") UUID roleId,
                                     @Param("scopeType") ScopeType scopeType,
                                     @Param("scopeId") UUID scopeId);

    @Query("SELECT COUNT(ra) > 0 FROM RoleAssignment ra "
            + "WHERE ra.user.id = :userId "
            + "AND ra.role.name = :roleName "
            + "AND ra.scopeType = :scopeType "
            + "AND ((:scopeId IS NULL AND ra.scopeId IS NULL) OR ra.scopeId = :scopeId) "
            + "AND ra.active = true")
    boolean existsActiveByUserAndRoleNameAndScope(@Param("userId") UUID userId,
                                                  @Param("roleName") String roleName,
                                                  @Param("scopeType") ScopeType scopeType,
                                                  @Param("scopeId") UUID scopeId);

    /** "Which scopes (of this type) does user U have role R at?" */
    @Query("SELECT DISTINCT ra.scopeId FROM RoleAssignment ra "
            + "WHERE ra.user.id = :userId "
            + "AND ra.role.name = :roleName "
            + "AND ra.scopeType = :scopeType "
            + "AND ra.active = true")
    List<UUID> findScopeIdsForUserRoleAndType(@Param("userId") UUID userId,
                                              @Param("roleName") String roleName,
                                              @Param("scopeType") ScopeType scopeType);

    /** "Who are the admins of scope (type, id)?" */
    @Query("SELECT ra FROM RoleAssignment ra "
            + "WHERE ra.role.name = :roleName "
            + "AND ra.scopeType = :scopeType "
            + "AND ((:scopeId IS NULL AND ra.scopeId IS NULL) OR ra.scopeId = :scopeId) "
            + "AND ra.active = true")
    List<RoleAssignment> findByRoleNameAtScope(@Param("roleName") String roleName,
                                               @Param("scopeType") ScopeType scopeType,
                                               @Param("scopeId") UUID scopeId);

    @Modifying
    @Query("DELETE FROM RoleAssignment ra "
            + "WHERE ra.user.id = :userId "
            + "AND ra.role.id = :roleId "
            + "AND ra.scopeType = :scopeType "
            + "AND ((:scopeId IS NULL AND ra.scopeId IS NULL) OR ra.scopeId = :scopeId)")
    int deleteByUserRoleAndScope(@Param("userId") UUID userId,
                                 @Param("roleId") UUID roleId,
                                 @Param("scopeType") ScopeType scopeType,
                                 @Param("scopeId") UUID scopeId);
}
