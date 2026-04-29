package com.juviai.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client that user-service uses to reach commerce-service for
 * {@link com.juviai.user.domain.ScopeType#STORE} validation and parent
 * (B2B_UNIT) lookup from {@code StoreScopeResolver}.
 *
 * <p>Only the two smallest endpoints on {@code StoreController} are consumed:
 * {@code GET /api/stores/{id}/b2b-unit-id} (returns a single UUID) and
 * {@code GET /api/stores/{id}} (used as a cheap existence probe — a 2xx
 * response is sufficient).</p>
 */
@FeignClient(
        name = "commerce-service",
        contextId = "store-scope-client",
        configuration = com.juviai.user.config.FeignClientConfig.class
)
public interface StoreClient {

    /**
     * Returns the UUID of the B2B unit that owns the store, or {@code null} for
     * a standalone store. Used by {@code StoreScopeResolver.parentOf} to lift
     * the scope cursor from STORE to B2B_UNIT during cascade role checks.
     *
     * @param storeId store UUID
     * @return b2bUnitId UUID or {@code null}
     */
    @GetMapping("/api/stores/{storeId}/b2b-unit-id")
    UUID getB2bUnitId(@PathVariable("storeId") UUID storeId);

    /**
     * Existence probe. Returning any 2xx body is sufficient; the content is
     * ignored. The scope resolver treats any {@link feign.FeignException.NotFound}
     * or connection failure as "does not exist / unreachable".
     *
     * @param storeId store UUID
     * @return opaque body (ignored); caller only checks the HTTP status
     */
    @GetMapping("/api/stores/{storeId}")
    Object getById(@PathVariable("storeId") UUID storeId);
}
