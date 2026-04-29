package com.juviai.auth.web.dto;

public class TokenResponse {
    public final String accessToken;
    public final String refreshToken;
    public final long expiresIn;
    public final String tokenType = "Bearer";

    public TokenResponse(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }
}
