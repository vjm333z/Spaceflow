package com.spaceflow.auth;

/** 로그인 성공 시 발급되는 액세스 토큰 응답. */
public record TokenResponse(
        String accessToken,
        String tokenType,       // "Bearer"
        long expiresInSeconds
) {
}
