-- 개발/데모용 샘플 데이터 (예약 테스트를 위해 방이 하나는 있어야 한다)
insert into tenant (name) values ('데모 스터디카페');
insert into space (tenant_id, name) values (1, '강남점');
insert into room (space_id, name, capacity) values (1, '4인실 A', 4);
insert into room (space_id, name, capacity) values (1, '1인실 B', 1);
