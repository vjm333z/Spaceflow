package com.spaceflow.user;

/**
 * 사용자 역할.
 * - OWNER : 사업자(사장). 소속 테넌트의 공간·방·예약을 관리
 * - GUEST : 손님. 예약하는 사람
 */
public enum Role {
    OWNER,
    GUEST
}
