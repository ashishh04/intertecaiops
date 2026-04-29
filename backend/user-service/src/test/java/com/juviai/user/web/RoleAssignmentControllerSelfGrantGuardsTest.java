package com.juviai.user.web;

import com.juviai.user.domain.ScopeType;
import com.juviai.user.dto.RoleAssignmentData;
import com.juviai.user.dto.RoleAssignmentRequest;
import com.juviai.user.facade.RoleAssignmentFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the whitelist guards on the {@code /internal/self-grant} endpoint that
 * powers CLAUDE.md rules 2 and 3 ("creator becomes admin"). Because the
 * endpoint lets any authenticated caller grant themselves a scoped admin role,
 * the whitelists are the primary defense against horizontal privilege
 * escalation.
 *
 * <p>Tests run as plain method invocations against the controller — the
 * {@code @PreAuthorize("isAuthenticated()")} is covered in the integration
 * tier; here we pin the in-method whitelist logic.</p>
 */
@ExtendWith(MockitoExtension.class)
class RoleAssignmentControllerSelfGrantGuardsTest {

    @Mock
    private RoleAssignmentFacade roleAssignmentFacade;

    @InjectMocks
    private RoleAssignmentController controller;

    private Authentication authWithUserId(UUID userId) {
        // GatewayAuthFilter sets auth.getName() to the userId UUID.
        return new UsernamePasswordAuthenticationToken(userId.toString(), "N/A", List.of());
    }

    @Test
    @DisplayName("ROLE_STORE_ADMIN + STORE scope + scopeId → 201 Created, grants to caller")
    void allowsStoreAdminAtStoreScope() {
        UUID callerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        RoleAssignmentData responseData = new RoleAssignmentData();
        when(roleAssignmentFacade.assign(any(RoleAssignmentRequest.class), anyString()))
                .thenReturn(responseData);

        ResponseEntity<RoleAssignmentData> resp = controller.selfGrant(
                "ROLE_STORE_ADMIN",
                ScopeType.STORE,
                storeId,
                authWithUserId(callerId));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // The recipient must always be the caller — never the body.
        ArgumentCaptor<RoleAssignmentRequest> captor = ArgumentCaptor.forClass(RoleAssignmentRequest.class);
        verify(roleAssignmentFacade).assign(captor.capture(), anyString());
        RoleAssignmentRequest req = captor.getValue();
        assertThat(req.getUserId()).isEqualTo(callerId);
        assertThat(req.getRoleName()).isEqualTo("ROLE_STORE_ADMIN");
        assertThat(req.getScopeType()).isEqualTo(ScopeType.STORE);
        assertThat(req.getScopeId()).isEqualTo(storeId);
    }

    @Test
    @DisplayName("ROLE_BUSINESS_ADMIN + B2B_UNIT allowed (rule 2 path)")
    void allowsBusinessAdminAtB2bUnitScope() {
        UUID callerId = UUID.randomUUID();
        UUID b2bUnitId = UUID.randomUUID();
        when(roleAssignmentFacade.assign(any(RoleAssignmentRequest.class), anyString()))
                .thenReturn(new RoleAssignmentData());

        ResponseEntity<RoleAssignmentData> resp = controller.selfGrant(
                "ROLE_BUSINESS_ADMIN",
                ScopeType.B2B_UNIT,
                b2bUnitId,
                authWithUserId(callerId));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("Non-whitelisted role (ROLE_ADMIN) → 400, no assignment")
    void rejectsPlatformAdminRole() {
        UUID callerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        ResponseEntity<RoleAssignmentData> resp = controller.selfGrant(
                "ROLE_ADMIN",
                ScopeType.STORE,
                storeId,
                authWithUserId(callerId));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(roleAssignmentFacade, never()).assign(any(), anyString());
    }

    @Test
    @DisplayName("Non-whitelisted role (ROLE_USER) → 400")
    void rejectsRoleUserSelfGrant() {
        ResponseEntity<RoleAssignmentData> resp = controller.selfGrant(
                "ROLE_USER",
                ScopeType.STORE,
                UUID.randomUUID(),
                authWithUserId(UUID.randomUUID()));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(roleAssignmentFacade, never()).assign(any(), anyString());
    }

    @Test
    @DisplayName("GLOBAL scope → 400 (only scoped resources are self-grantable)")
    void rejectsGlobalScope() {
        ResponseEntity<RoleAssignmentData> resp = controller.selfGrant(
                "ROLE_STORE_ADMIN",
                ScopeType.GLOBAL,
                UUID.randomUUID(),
                authWithUserId(UUID.randomUUID()));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(roleAssignmentFacade, never()).assign(any(), anyString());
    }

    @Test
    @DisplayName("TENANT scope → 400 (tenant admin cannot be self-claimed)")
    void rejectsTenantScope() {
        ResponseEntity<RoleAssignmentData> resp = controller.selfGrant(
                "ROLE_BUSINESS_ADMIN",
                ScopeType.TENANT,
                UUID.randomUUID(),
                authWithUserId(UUID.randomUUID()));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(roleAssignmentFacade, never()).assign(any(), anyString());
    }

    @Test
    @DisplayName("Null auth → 401 and no assignment")
    void rejectsWhenAuthMissing() {
        ResponseEntity<RoleAssignmentData> resp = controller.selfGrant(
                "ROLE_STORE_ADMIN",
                ScopeType.STORE,
                UUID.randomUUID(),
                null);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(roleAssignmentFacade, never()).assign(any(), anyString());
    }
}
