package com.juviai.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;

@Service
public class OtpWorkflowService {

    private final OtpService otpService;
    private final UserServiceClient userServiceClient;

    public OtpWorkflowService(OtpService otpService, UserServiceClient userServiceClient) {
        this.otpService = otpService;
        this.userServiceClient = userServiceClient;
    }

    public boolean verify(UUID userId, String otp, String tenantHeader) {
        String tenant = StringUtils.hasText(tenantHeader) ? tenantHeader : "default";
        return otpService.verify(userId, otp, tenant);
    }

    public void resend(UUID userId, String tenantHeader) {
        String tenant = StringUtils.hasText(tenantHeader) ? tenantHeader : "default";
        Map<?, ?> authInfo = userServiceClient.authInfo(userId.toString(), tenant);
        if (authInfo == null || authInfo.get("email") == null) {
            throw new IllegalArgumentException("User not found");
        }
        String email = String.valueOf(authInfo.get("email"));
        otpService.resend(userId, email, tenant);
    }
}
