package com.juviai.auth.web;

import com.juviai.auth.service.AuthService;
import com.juviai.auth.web.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest req, HttpServletRequest servletRequest,
                                               @RequestHeader(value = "X-JuviAI-Tenant", required = false) String tenantHeader,
                                               @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        String tenant = StringUtils.hasText(tenantHeader) ? tenantHeader : "default";
        String ip = resolveIp(servletRequest);
        String ua = (userAgent != null) ? userAgent : "";

        return ResponseEntity.ok(authService.login(req.username, req.password, req.deviceId, ua, ip, tenant));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid RefreshRequest req,
                                                 HttpServletRequest servletRequest,
                                                 @RequestHeader(value = "X-JuviAI-Tenant", required = false) String tenantHeader,
                                                 @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        String tenant = StringUtils.hasText(tenantHeader) ? tenantHeader : "default";
        String ip = resolveIp(servletRequest);
        String ua = (userAgent != null) ? userAgent : "";

        String[] parts = req.refreshToken.split("\\.", 2);
        if (parts.length < 2) {
            return ResponseEntity.unprocessableEntity().build();
        }

        return ResponseEntity.ok(authService.refresh(req.refreshToken, req.deviceId, ua, ip, tenant));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody @Valid LogoutRequest req) {
        authService.logout(req.refreshToken, req.deviceId);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(@RequestBody @Valid LogoutAllRequest req,
                                       @RequestHeader(value = "X-JuviAI-Tenant", required = false) String tenantHeader) {
        String tenant = StringUtils.hasText(tenantHeader) ? tenantHeader : "default";
        authService.logoutAll(UUID.fromString(req.userId), tenant);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req,
                                      @RequestHeader(value = "X-JuviAI-Tenant", required = false) String tenantHeader) {
        String tenant = StringUtils.hasText(tenantHeader) ? tenantHeader : "default";
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", req.firstName);
        body.put("lastName", req.lastName);
        body.put("email", req.email);
        body.put("linkedinProfile", req.linkedinProfile);
        body.put("mobile", req.mobile);
        body.put("password", req.password);
        body.put("active", req.active);
        body.put("status", req.status);
        body.put("student", req.student);
        body.put("collegeUUID", req.collegeUUID);
        body.put("startYear", req.startYear);
        body.put("endYear", req.endYear);
        body.put("branchCode", req.branchCode);
        return ResponseEntity.ok(authService.register(body, tenant));
    }

    private String resolveIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
