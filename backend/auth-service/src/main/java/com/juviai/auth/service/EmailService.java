package com.juviai.auth.service;

import java.util.UUID;

public interface EmailService {
    void sendOtpEmail(UUID userId, String toEmail, String otp, String tenant);
}
