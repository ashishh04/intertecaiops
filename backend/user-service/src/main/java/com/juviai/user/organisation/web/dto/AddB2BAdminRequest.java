package com.juviai.user.organisation.web.dto;

import java.util.UUID;

/**
 * Request body for {@code POST /api/b2b/{id}/admin}.
 *
 * <p>Supply exactly one of {@code userId} or {@code email} to identify the user
 * who should be promoted to {@code ROLE_BUSINESS_ADMIN} for the target B2BUnit.
 *
 * <pre>
 * { "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6" }
 *   OR
 * { "email": "manager@restaurant.com" }
 * </pre>
 */
public class AddB2BAdminRequest {

    /** UUID of the existing user to promote. Takes priority over {@code email}. */
    private UUID userId;

    /** Email of the existing user to promote. Used when {@code userId} is null. */
    private String email;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
