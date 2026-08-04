-- 사용자 계정. (user는 Postgres 예약어라 app_user)
create table app_user (
    id         bigint generated always as identity primary key,
    email      varchar(255) not null unique,
    password   varchar(255) not null,               -- BCrypt 해시 (평문 저장 금지)
    role       varchar(20)  not null,               -- OWNER / GUEST
    tenant_id  bigint       references tenant (id),  -- OWNER면 소속 테넌트
    created_at timestamptz  not null default now(),
    constraint chk_user_role check (role in ('OWNER', 'GUEST'))
);
