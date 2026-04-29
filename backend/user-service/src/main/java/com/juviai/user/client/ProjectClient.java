package com.juviai.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client that user-service uses to reach project-service for scope
 * validation and parent lookup from {@code ProjectScopeResolver}.
 *
 * <p>Only the subset of the project API needed by the RBAC machinery is
 * declared here — the full {@code ProjectData} payload is reused to keep the
 * surface small; only the {@code b2bUnitId} field is consumed.</p>
 *
 * <p>Failures propagate as Feign exceptions; the resolver is expected to catch
 * them and fall back to the permissive "don't know — don't block" policy so a
 * temporary project-service outage never prevents role checks from resolving
 * against direct assignments (which don't need the Feign call at all).</p>
 */
@FeignClient(
        name = "project-service",
        contextId = "project-scope-client",
        configuration = com.juviai.user.config.FeignClientConfig.class
)
public interface ProjectClient {

    /**
     * Fetches the project body; used both to verify the project exists (any
     * 2xx response) and to read {@code b2bUnitId} for parent-scope resolution.
     *
     * @param projectId project UUID
     * @return lightweight project view with at least {@code id} and {@code b2bUnitId}
     */
    @GetMapping("/api/projects/{projectId}")
    ProjectLite getProject(@PathVariable("projectId") UUID projectId);

    /**
     * Flattened view of {@code ProjectData} — only fields the scope resolver
     * needs. Declared inside the client to avoid pulling project-service DTOs
     * into user-service.
     */
    class ProjectLite {
        private UUID id;
        private UUID b2bUnitId;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public UUID getB2bUnitId() { return b2bUnitId; }
        public void setB2bUnitId(UUID b2bUnitId) { this.b2bUnitId = b2bUnitId; }
    }
}
