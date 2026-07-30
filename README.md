# SpaceFlow

> 스터디룸·회의실 시간단위 예약 SaaS (멀티테넌트)

스터디카페/공유공간 사업자가 공간을 등록하고, 이용자가 웹에서 시간대를 골라
예약·결제하는 멀티테넌트 예약 플랫폼입니다.

## 이 프로젝트에서 다루는 것 (포트폴리오 포인트)

- **동시 예약 정합성** — 같은 방·같은 시간대 중복 예약을 어떻게 막는가
  (낙관적 락 / 비관적 락 / PostgreSQL `EXCLUDE` 제약 3가지 비교, 부하테스트로 증명)
- **요금정책 엔진** — 피크/오프피크·요일·연속이용 할인·쿠폰을 하드코딩 없이 데이터로 설계
- **멀티테넌시** — 여러 사업자의 데이터를 안전하게 격리
- **운영 가능한 서비스** — 통합테스트(Testcontainers), CI/CD, 구조화 로깅, 메트릭

## 기술 스택

| 레이어 | 기술 |
|--------|------|
| 언어/런타임 | Java 21 (LTS) |
| 백엔드 | Spring Boot 3, Spring Security 6, Spring Data JPA, Querydsl |
| 데이터 | PostgreSQL, Redis |
| 마이그레이션 | Flyway |
| 프론트엔드 | React, TypeScript, Vite |
| 인프라 | Docker Compose, GitHub Actions |
| 테스트 | JUnit 5, Testcontainers, k6 |

## 로컬 실행

```bash
# 1. 인프라(Postgres + Redis) 기동
docker compose up -d

# 2. 백엔드
cd backend && ./gradlew bootRun

# 3. 프론트엔드
cd frontend && npm install && npm run dev
```

## 프로젝트 구조

```
spaceflow/
├── backend/            # Spring Boot 3 API 서버
├── frontend/           # React + TypeScript SPA
├── docker-compose.yml  # 로컬 인프라 (Postgres, Redis)
└── docs/               # 아키텍처 결정 기록, 다이어그램
```

## 개발 로드맵

- [ ] **M0** 프로젝트 뼈대
- [ ] **M1** 예약 코어 + 동시성 방지
- [ ] **M2** 요금정책 엔진
- [ ] **M3** 인증 + 멀티테넌시
- [ ] **M4** 프론트엔드
- [ ] **M5** 운영/증명/배포
