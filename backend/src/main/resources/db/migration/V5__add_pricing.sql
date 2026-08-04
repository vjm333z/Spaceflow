-- 요금정책 엔진 스키마 (M2)

-- 방별 시간당 기본요금 (원). 기존 방들엔 기본값 10000 부여.
alter table room add column base_price_per_hour numeric(12, 2) not null default 10000;

-- 요금 규칙: 코드가 아닌 데이터로 규칙을 관리한다.
create table pricing_rule (
    id           bigint generated always as identity primary key,
    room_id      bigint        not null references room (id),
    rule_type    varchar(20)   not null,          -- TIME_OF_DAY / DAY_OF_WEEK / DURATION
    day_of_week  int,                             -- 1=월 ... 7=일 (DAY_OF_WEEK용)
    start_time   time,                            -- TIME_OF_DAY 시작 (영업 지역시간 기준)
    end_time     time,                            -- TIME_OF_DAY 종료
    min_hours    int,                             -- DURATION: 이 시간 이상이면 적용
    adjust_type  varchar(10)   not null,          -- PERCENT / FIXED
    adjust_value numeric(12, 2) not null,         -- PERCENT면 %, FIXED면 원
    priority     int           not null default 0,-- 적용 순서(작을수록 먼저)
    created_at   timestamptz   not null default now(),
    constraint chk_rule_type   check (rule_type in ('TIME_OF_DAY', 'DAY_OF_WEEK', 'DURATION')),
    constraint chk_adjust_type check (adjust_type in ('PERCENT', 'FIXED'))
);
create index idx_pricing_rule_room on pricing_rule (room_id, priority);

-- 데모 규칙 (room 1): 피크 +50%, 주말 +30%, 4시간 이상 -10%
insert into pricing_rule (room_id, rule_type, start_time, end_time, adjust_type, adjust_value, priority)
values (1, 'TIME_OF_DAY', '18:00', '22:00', 'PERCENT', 50, 1);

insert into pricing_rule (room_id, rule_type, day_of_week, adjust_type, adjust_value, priority)
values (1, 'DAY_OF_WEEK', 6, 'PERCENT', 30, 2),   -- 토
       (1, 'DAY_OF_WEEK', 7, 'PERCENT', 30, 2);   -- 일

insert into pricing_rule (room_id, rule_type, min_hours, adjust_type, adjust_value, priority)
values (1, 'DURATION', 4, 'PERCENT', -10, 3);
