package com.spaceflow.user;

import com.spaceflow.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 계정. password는 반드시 BCrypt 해시로 저장한다.
 */
@Entity
@Table(name = "app_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;   // BCrypt 해시

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // OWNER면 소속 테넌트 id (GUEST는 null)
    @Column(name = "tenant_id")
    private Long tenantId;

    public User(String email, String encodedPassword, Role role, Long tenantId) {
        this.email = email;
        this.password = encodedPassword;
        this.role = role;
        this.tenantId = tenantId;
    }
}
