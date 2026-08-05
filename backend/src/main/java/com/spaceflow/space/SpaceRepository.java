package com.spaceflow.space;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpaceRepository extends JpaRepository<Space, Long> {

    // 사장의 테넌트에 속한 첫 지점 (데모는 테넌트당 지점 1개)
    Optional<Space> findFirstByTenantId(Long tenantId);
}
