-- 데모 랜딩을 풍성하게: tenant1/space1(강남점)에 방 추가
insert into room (space_id, name, capacity, base_price_per_hour) values
    (1, '8인 세미나실', 8, 20000),
    (1, '6인 회의실', 6, 15000),
    (1, '1인 포커스룸', 1, 6000);
