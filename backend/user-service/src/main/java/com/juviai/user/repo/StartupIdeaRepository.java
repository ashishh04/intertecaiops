package com.juviai.user.repo;

import com.juviai.user.domain.StartupIdea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface StartupIdeaRepository extends JpaRepository<StartupIdea, UUID> {

    @Query("""
            select s from StartupIdea s
            where s.tenantId = :tenantId
              and (:categoryId is null or s.category.id = :categoryId)
            """)
    Page<StartupIdea> list(@Param("tenantId") String tenantId,
                          @Param("categoryId") UUID categoryId,
                          Pageable pageable);
}
