package com.juviai.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;

import java.util.Map;
import java.util.UUID;

@Service
public class ResendEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);

    private final RestTemplate restTemplate;

    private final Environment environment;

    @Value("${skillrat.email.resend.api-key:}")
    private String apiKey;

    @Value("${skillrat.email.from:no-reply@juviai.io}")
    private String from;

    @Value("${skillrat.email.otp.subject:SkillRat Email Verification OTP}")
    private String otpSubject;

    public ResendEmailService(RestTemplate restTemplate, Environment environment) {
        this.restTemplate = restTemplate;
        this.environment = environment;
    }

    @PostConstruct
    void logConfig() {
        String[] profiles = environment != null ? environment.getActiveProfiles() : new String[0];
        String key = apiKey;
        String masked = (key == null || key.isBlank()) ? "<empty>" : (key.substring(0, Math.min(6, key.length())) + "...");
        String emailKey = environment != null ? environment.getProperty("skillrat.email.resend.api-key") : null;
        String mailKey = environment != null ? environment.getProperty("skillrat.mail.resend.api-key") : null;
        String envKey = environment != null ? environment.getProperty("RESEND_API_KEY") : null;
        String emailMasked = (emailKey == null || emailKey.isBlank()) ? "<empty>" : (emailKey.substring(0, Math.min(6, emailKey.length())) + "...");
        String mailMasked = (mailKey == null || mailKey.isBlank()) ? "<empty>" : (mailKey.substring(0, Math.min(6, mailKey.length())) + "...");
        String envMasked = (envKey == null || envKey.isBlank()) ? "<empty>" : (envKey.substring(0, Math.min(6, envKey.length())) + "...");
        log.info("Resend config loaded profiles={} apiKey={} propEmail={} propMail={} envResendApiKey={}",
                java.util.Arrays.toString(profiles), masked, emailMasked, mailMasked, envMasked);
    }

    @Override
    @Async("emailExecutor")
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void sendOtpEmail(UUID userId, String toEmail, String otp, String tenant) {
        if (!StringUtils.hasText(toEmail)) {
            return;
        }
        String normalizedApiKey = normalizeApiKey(apiKey);
        if (!StringUtils.hasText(normalizedApiKey)) {
            String envMasked = environment != null ? environment.getProperty("RESEND_API_KEY") : null;
            String m = (envMasked == null || envMasked.isBlank()) ? "<empty>" : (envMasked.substring(0, Math.min(6, envMasked.length())) + "...");
            log.warn("Resend API key not configured; skipping OTP email userId={} tenant={} envResendApiKey={}", userId, tenant, m);
            return;
        }

        String html = "<div style=\"font-family:Arial,sans-serif\">" +
                "<h2>Verify your email</h2>" +
                "<p>Your SkillRat verification code is:</p>" +
                "<p style=\"font-size:24px;font-weight:bold;letter-spacing:2px\">" + otp + "</p>" +
                "<p>This code expires in 5 minutes.</p>" +
                "</div>";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(normalizedApiKey);

        Map<String, Object> payload = Map.of(
                "from", from,
                "to", new String[]{toEmail},
                "subject", otpSubject,
                "html", html
        );

        try {
            restTemplate.postForEntity("https://api.resend.com/emails", new HttpEntity<>(payload, headers), Map.class);
            log.info("OTP email queued via Resend userId={} to={} tenant={}", userId, toEmail, tenant);
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Resend API key rejected (401). Check RESEND_API_KEY value and ensure it is not expired and does not include 'Bearer ' prefix. userId={} to={} tenant={}",
                    userId, toEmail, tenant, e);
            return;
        } catch (Exception e) {
            log.warn("Failed to send OTP email via Resend userId={} to={} tenant={} (will retry)", userId, toEmail, tenant, e);
            throw e;
        }
    }

    private String normalizeApiKey(String key) {
        if (key == null) {
            return null;
        }
        String k = key.trim();
        if (k.regionMatches(true, 0, "Bearer ", 0, 7)) {
            k = k.substring(7).trim();
        }
        return k;
    }
}
