-- 예약 확정 시점의 요금 스냅샷. 요금정책이 나중에 바뀌어도 과거 예약 금액은 이 값으로 보존된다.
-- 기존 예약 행은 값이 없으므로 nullable.
alter table reservation add column price numeric(12, 2);
