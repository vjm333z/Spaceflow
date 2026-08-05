-- 예약을 로그인 사용자와 연결(내 예약/취소용). 비로그인 예약도 허용하므로 nullable.
alter table reservation add column user_id bigint references app_user (id);
create index idx_reservation_user on reservation (user_id);
