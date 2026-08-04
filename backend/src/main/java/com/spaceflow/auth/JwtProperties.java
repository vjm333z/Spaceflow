package com.spaceflow.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 설정 (application.yml의 app.jwt.*).
 * secret은 HS256용 대칭키(최소 32바이트). 운영에서는 환경변수로 주입한다.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long expirationMinutes
) {
}
