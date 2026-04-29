package com.juviai.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Component
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final String userBaseUrl;

    public UserServiceClient(RestTemplate restTemplate,
                             @Value("${juviai.user.base-url:http://localhost:8081}") String userBaseUrl) {
        this.restTemplate = restTemplate;
        this.userBaseUrl = userBaseUrl;
    }

    public Map<?, ?> login(String emailOrMobile, String password, String tenant) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-JuviAI-Tenant", tenant);
        Map<String, String> body = Map.of("emailOrMobile", emailOrMobile, "password", password);
        return restTemplate.postForObject(userBaseUrl + "/api/users/login", new HttpEntity<>(body, headers), Map.class);
    }

    public Map<?, ?> signup(Map<String, Object> body, String tenant) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-JuviAI-Tenant", tenant);
        return restTemplate.postForObject(userBaseUrl + "/api/users/signup", new HttpEntity<>(body, headers), Map.class);
    }

    public void incrementTokenVersion(String userId, String tenant) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-JuviAI-Tenant", tenant);
        restTemplate.postForLocation(userBaseUrl + "/api/users/internal/tokenVersion/increment/" + userId, new HttpEntity<>(null, headers));
    }

    public Map<?, ?> authInfo(String userId, String tenant) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-JuviAI-Tenant", tenant);
        return restTemplate.getForObject(userBaseUrl + "/api/users/internal/auth/" + userId, Map.class);
    }

    public void updateStatus(String userId, String status, String tenant) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-JuviAI-Tenant", tenant);
        Map<String, String> body = Map.of("status", status);
        restTemplate.postForLocation(userBaseUrl + "/api/users/internal/status/" + userId, new HttpEntity<>(body, headers));
    }

    /**
     * Look up a user by phone number.
     * Returns null if no user is found (404 is swallowed and mapped to null).
     */
    public Map<?, ?> findByPhone(String phoneNumber, String tenant) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-JuviAI-Tenant", tenant);
        try {
            return restTemplate.exchange(
                    userBaseUrl + "/api/users/internal/phone/" + phoneNumber,
                    HttpMethod.GET,
                    new HttpEntity<>(null, headers),
                    Map.class).getBody();
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    /**
     * Auto-register a new user identified by phone number.
     * Creates a minimal user record; user-service assigns default ROLE_USER.
     * The created account is immediately ACTIVE (OTP has already been validated).
     */
    public Map<?, ?> createFromPhone(String phoneNumber, String fullName, String tenant) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-JuviAI-Tenant", tenant);
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("mobile", phoneNumber);
        body.put("fullName", fullName != null ? fullName : "");
        body.put("active", true);
        body.put("status", "ACTIVE");
        body.put("otpRegistered", true); // signals user-service: skip email OTP trigger
        return restTemplate.postForObject(
                userBaseUrl + "/api/users/internal/phone-register",
                new HttpEntity<>(body, headers),
                Map.class);
    }
}
