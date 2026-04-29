package com.juviai.auth.web;

import com.juviai.auth.service.OtpWorkflowService;
import com.juviai.auth.web.dto.ResendOtpRequest;
import com.juviai.auth.web.dto.VerifyOtpRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class OtpController {

    private final OtpWorkflowService otpWorkflowService;

    public OtpController(OtpWorkflowService otpWorkflowService) {
        this.otpWorkflowService = otpWorkflowService;
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verify(@RequestBody @Valid VerifyOtpRequest request,
                                    @RequestHeader(value = "X-JuviAI-Tenant", required = false) String tenantHeader) {
        boolean verified = otpWorkflowService.verify(request.userId, request.otp, tenantHeader);
        if (!verified) {
            return ResponseEntity.status(400).body(Map.of("verified", false));
        }
        return ResponseEntity.ok(Map.of("verified", true));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resend(@RequestBody @Valid ResendOtpRequest request,
                                    @RequestHeader(value = "X-JuviAI-Tenant", required = false) String tenantHeader) {
        otpWorkflowService.resend(request.userId, tenantHeader);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
