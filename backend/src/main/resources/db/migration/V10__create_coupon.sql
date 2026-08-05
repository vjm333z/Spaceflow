-- 쿠폰: 요금 규칙 적용 후 최종 금액에 할인을 얹는다.
create table coupon (
    id             bigint generated always as identity primary key,
    code           varchar(40)   not null unique,
    discount_type  varchar(10)   not null,   -- PERCENT / FIXED
    discount_value numeric(12, 2) not null,
    active         boolean       not null default true,
    created_at     timestamptz   not null default now(),
    constraint chk_coupon_type check (discount_type in ('PERCENT', 'FIXED'))
);

-- 데모 쿠폰
insert into coupon (code, discount_type, discount_value) values
    ('WELCOME10', 'PERCENT', 10),   -- 10% 할인
    ('SAVE5000', 'FIXED', 5000);    -- 5,000원 할인
