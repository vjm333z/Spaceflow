package com.spaceflow.auth;

import com.spaceflow.user.Role;
import com.spaceflow.user.User;
import com.spaceflow.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    /** 회원가입 — 비밀번호는 BCrypt로 해싱해 저장. 자기 가입은 GUEST. */
    @Transactional
    public Long signup(SignupRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }
        User user = new User(req.email(), passwordEncoder.encode(req.password()), Role.GUEST, null);
        return userRepository.save(user).getId();
    }

    /** 로그인 — 비밀번호 검증 후 JWT 발급. */
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return issueToken(user);
    }

    private TokenResponse issueToken(User user) {
        Instant now = Instant.now();
        long expSec = jwtProperties.expirationMinutes() * 60;

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("spaceflow")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expSec))
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name());
        // OWNER만 tenantId를 토큰에 담는다 (멀티테넌시 격리에 사용)
        if (user.getTenantId() != null) {
            claims.claim("tenantId", user.getTenantId());
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
        return new TokenResponse(token, "Bearer", expSec);
    }
}
