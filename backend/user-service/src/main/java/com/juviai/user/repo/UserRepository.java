package com.juviai.user.repo;

import com.juviai.common.crypto.SearchableHashConverter;
import com.juviai.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // ── Hash-based lookups (email & mobile are encrypted; query via blind index) ──

    Optional<User> findByEmailHash(String emailHash);
    boolean existsByEmailHash(String emailHash);

    Optional<User> findByMobileHash(String mobileHash);
    boolean existsByMobileHash(String mobileHash);

    // ── Convenience wrappers: callers pass plaintext, hashing happens here ────────

    default Optional<User> findByEmailIgnoreCase(String email) {
        return findByEmailHash(SearchableHashConverter.hash(
                email != null ? email.toLowerCase().trim() : null));
    }

    default boolean existsByEmailIgnoreCase(String email) {
        return existsByEmailHash(SearchableHashConverter.hash(
                email != null ? email.toLowerCase().trim() : null));
    }

    default Optional<User> findByMobile(String mobile) {
        return findByMobileHash(SearchableHashConverter.hash(mobile));
    }

    default boolean existsByMobile(String mobile) {
        return existsByMobileHash(SearchableHashConverter.hash(mobile));
    }

    Optional<User> findByUsername(String username);
    Optional<User> findByPasswordSetupToken(String token);

    /**
     * Admin search: NOTE — full-text search on encrypted firstName/lastName/email is not
     * possible with simple SQL LIKE. This query falls back to username (not encrypted)
     * for text search. For a full encrypted-field search, implement application-side
     * filtering or use PostgreSQL pgcrypto with deterministic encryption.
     */
    @Query(value = """
        select distinct u from User u
        join u.roles r
        where (:q is null or
               lower(u.username) like lower(concat('%', :q, '%')))
          and (:role is null or r.name = :role)
          and (:b2bUnitId is null or r.b2bUnitId = :b2bUnitId)
        """,
            countQuery = """
        select count(distinct u.id) from User u
        join u.roles r
        where (:q is null or
               lower(u.username) like lower(concat('%', :q, '%')))
          and (:role is null or r.name = :role)
          and (:b2bUnitId is null or r.b2bUnitId = :b2bUnitId)
        """)
    Page<User> search(
            @Param("b2bUnitId") UUID b2bUnitId,
            @Param("q") String q,
            @Param("role") String role,
            Pageable pageable
    );
}
