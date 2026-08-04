package com.spaceflow.config;

import com.spaceflow.user.Role;
import com.spaceflow.user.User;
import com.spaceflow.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 데모용 사장(OWNER) 계정을 앱 시작 시 없으면 생성한다.
 * (회원가입은 GUEST만 만들므로, 대시보드 시연용 OWNER를 여기서 준비.)
 * 비밀번호는 실제 인코더로 해싱해 넣는다. 운영에서는 비활성화 대상.
 */
@Component
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("owner@demo.com").isEmpty()) {
            // tenant 1(데모 스터디카페)의 사장
            userRepository.save(new User("owner@demo.com", passwordEncoder.encode("password123"), Role.OWNER, 1L));
        }
    }
}
