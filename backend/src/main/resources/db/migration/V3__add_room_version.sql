-- 낙관적 락을 위한 버전 컬럼.
-- 예약 시 방(room)의 version을 강제 증가시켜, 같은 방 동시 예약을 커밋 시점에 충돌로 감지한다.
alter table room add column version bigint not null default 0;
