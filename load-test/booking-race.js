import http from 'k6/http'
import { check } from 'k6'
import { Counter } from 'k6/metrics'

// 같은 방·같은 시간에 동시에 몰리는 예약 요청. 딱 1건만 성공해야 한다.
const success = new Counter('booking_success') // 201
const conflict = new Counter('booking_conflict') // 409 (이미 예약됨)
const other = new Counter('booking_other') // 그 외(있으면 문제)

export const options = {
  scenarios: {
    // 100명의 가상 사용자가 각자 1번씩, 거의 동시에 같은 슬롯을 예약 시도
    race: {
      executor: 'per-vu-iterations',
      vus: 100,
      iterations: 1,
      maxDuration: '30s',
    },
  },
}

const BASE = __ENV.BASE || 'http://localhost:8080'

// 같은 방(1), 같은 시간대를 모두가 노린다
const payload = JSON.stringify({
  roomId: 1,
  startAt: '2026-12-01T10:00:00+09:00',
  endAt: '2026-12-01T12:00:00+09:00',
  guestName: 'k6부하',
})

export default function () {
  const res = http.post(`${BASE}/api/reservations`, payload, {
    headers: { 'Content-Type': 'application/json; charset=utf-8' },
  })

  if (res.status === 201) success.add(1)
  else if (res.status === 409) conflict.add(1)
  else other.add(1)

  check(res, {
    '201 또는 409 (500 없음)': (r) => r.status === 201 || r.status === 409,
  })
}
