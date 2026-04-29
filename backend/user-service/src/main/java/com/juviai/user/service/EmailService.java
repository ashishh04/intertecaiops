package com.juviai.user.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${skillrat.email.resend.api-key:}")
    private String apiKey;

    @Value("${skillrat.email.from:no-reply@juviai.io}")
    private String from;

    @Value("${skillrat.email.otp.subject:Your OTP for SkillRat}")
    private String otpSubject;
    
    @Async
    public void sendOtpEmail(String to, String otp) {
        try {
            if (!StringUtils.hasText(to)) {
                return;
            }
            if (!StringUtils.hasText(apiKey)) {
                log.warn("Resend API key not configured; skipping OTP email to={}", to);
                return;
            }

            String html = "<div style=\"font-family:Arial,sans-serif\">" +
                    "<h2>Your OTP for SkillRat</h2>" +
                    "<p>Your verification code is:</p>" +
                    "<p style=\"font-size:24px;font-weight:bold;letter-spacing:2px\">" + otp + "</p>" +
                    "<p>This code expires in 2 minutes.</p>" +
                    "</div>";

            Resend resend = new Resend(apiKey);
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject(otpSubject)
                    .html(html)
                    .build();

            resend.emails().send(params);
            log.info("OTP email queued via Resend to={}", to);
        } catch (ResendException e) {
            log.error("Failed to send OTP email via Resend to={}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP email", e);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
}
