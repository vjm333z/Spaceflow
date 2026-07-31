-- 최종 동시성 방어: 같은 방에서 시간이 겹치는 예약을 DB가 물리적으로 거부한다.
-- 앱 로직에 버그가 있어도, 서버가 여러 대여도 무결성이 보장된다.

-- 범위 겹침(&&)과 등호(=)를 하나의 GiST 인덱스로 함께 다루기 위한 확장
create extension if not exists btree_gist;

alter table reservation
    add constraint reservation_no_overlap
    exclude using gist (
        room_id with =,
        tstzrange(start_at, end_at) with &&
    ) where (status <> 'CANCELLED');
-- tstzrange(a, b)는 기본이 [a, b) 반열린 구간 → 10~11시와 11~12시는 겹치지 않는다(우리 규칙과 일치).
