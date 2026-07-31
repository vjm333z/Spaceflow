-- 예약 도메인 기본 스키마 (M1)
-- 계층: tenant(사업자) 1─N space(지점) 1─N room(방) 1─N reservation(예약)

-- 사업자(테넌트): 멀티테넌시의 최상위. 이 아래로 모든 데이터가 갈린다.
create table tenant (
    id         bigint generated always as identity primary key,
    name       varchar(100) not null,
    created_at timestamptz  not null default now()
);

-- 지점/공간: 특정 사업자 소유
create table space (
    id         bigint generated always as identity primary key,
    tenant_id  bigint       not null references tenant (id),
    name       varchar(100) not null,
    created_at timestamptz  not null default now()
);
create index idx_space_tenant on space (tenant_id);

-- 방: 예약 대상 단위
create table room (
    id         bigint generated always as identity primary key,
    space_id   bigint       not null references space (id),
    name       varchar(100) not null,
    capacity   int          not null default 1,
    created_at timestamptz  not null default now()
);
create index idx_room_space on room (space_id);

-- 예약: 특정 방을 특정 시간구간(start_at ~ end_at)에 잡는다
create table reservation (
    id          bigint generated always as identity primary key,
    room_id     bigint      not null references room (id),
    start_at    timestamptz not null,
    end_at      timestamptz not null,
    status      varchar(20) not null default 'CONFIRMED',
    guest_name  varchar(50) not null,
    guest_phone varchar(30),
    version     bigint      not null default 0,
    created_at  timestamptz not null default now(),
    -- 종료는 시작보다 뒤여야 한다 (잘못된 구간 차단)
    constraint chk_reservation_time check (start_at < end_at),
    -- 허용 상태값 문서화 겸 방어
    constraint chk_reservation_status check (status in ('PENDING', 'CONFIRMED', 'CANCELLED'))
);
-- 방별 시간구간 조회(겹침 판정)에 쓸 인덱스
create index idx_reservation_room_time on reservation (room_id, start_at, end_at);
