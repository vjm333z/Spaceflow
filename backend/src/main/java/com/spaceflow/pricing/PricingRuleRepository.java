package com.spaceflow.pricing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    // 우선순위(priority) 순서대로 적용하기 위해 정렬해서 조회
    List<PricingRule> findByRoomIdOrderByPriorityAsc(Long roomId);
}
