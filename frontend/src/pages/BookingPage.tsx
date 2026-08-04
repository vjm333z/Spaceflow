import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { getQuote, getRooms } from '../api/rooms'
import { createReservation } from '../api/reservations'

// 날짜 + 시(hour)를 백엔드가 받는 ISO 문자열(+09:00)로 만든다
function iso(date: string, hour: number): string {
  return `${date}T${String(hour).padStart(2, '0')}:00:00+09:00`
}

const START_HOURS = Array.from({ length: 11 }, (_, i) => 8 + i) // 8~18시
const DURATIONS = [1, 2, 3, 4]

export default function BookingPage() {
  const { roomId } = useParams()
  const id = Number(roomId)
  const queryClient = useQueryClient()

  const [date, setDate] = useState('2026-09-12') // 토요일(주말 요금 데모)
  const [startHour, setStartHour] = useState(10)
  const [duration, setDuration] = useState(2)
  const [guestName, setGuestName] = useState('')
  const [guestPhone, setGuestPhone] = useState('')

  const startAt = iso(date, startHour)
  const endAt = iso(date, startHour + duration)

  const { data: rooms } = useQuery({ queryKey: ['rooms'], queryFn: getRooms })
  const room = rooms?.find((r) => r.id === id)

  // 시간이 바뀔 때마다 견적을 다시 가져온다 (queryKey에 startAt/endAt이 들어가서 자동 재요청)
  const { data: quote } = useQuery({
    queryKey: ['quote', id, startAt, endAt],
    queryFn: () => getQuote(id, startAt, endAt),
    enabled: !!id,
  })

  const reserve = useMutation({
    mutationFn: () => createReservation({ roomId: id, startAt, endAt, guestName, guestPhone }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reservations'] })
    },
  })

  const conflictMessage =
    axios.isAxiosError(reserve.error) && reserve.error.response?.status === 409
      ? (reserve.error.response.data as { error?: string })?.error ?? '이미 예약된 시간대입니다.'
      : null

  return (
    <div className="mx-auto max-w-2xl p-8">
      <Link to="/" className="text-sm text-indigo-600 hover:underline">
        ← 방 목록
      </Link>

      <h1 className="mt-3 text-2xl font-bold text-gray-900">{room?.name ?? '예약'}</h1>
      {room && (
        <p className="text-sm text-gray-500">
          정원 {room.capacity} · 시간당 {room.basePricePerHour.toLocaleString()}원
        </p>
      )}

      {/* 예약 성공 화면 */}
      {reserve.isSuccess ? (
        <div className="mt-6 rounded-xl border border-green-200 bg-green-50 p-6">
          <p className="text-lg font-semibold text-green-800">예약이 완료됐어요 🎉</p>
          <p className="mt-1 text-sm text-green-700">
            {date} {startHour}:00~{startHour + duration}:00 · 결제금액{' '}
            {reserve.data.price?.toLocaleString()}원
          </p>
          <button
            className="mt-4 rounded-lg bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700"
            onClick={() => reserve.reset()}
          >
            다른 시간 예약하기
          </button>
        </div>
      ) : (
        <div className="mt-6 space-y-6">
          {/* 시간 선택 */}
          <div className="grid grid-cols-3 gap-3">
            <label className="text-sm">
              <span className="mb-1 block text-gray-600">날짜</span>
              <input
                type="date"
                value={date}
                onChange={(e) => setDate(e.target.value)}
                className="w-full rounded-lg border border-gray-300 px-3 py-2"
              />
            </label>
            <label className="text-sm">
              <span className="mb-1 block text-gray-600">시작</span>
              <select
                value={startHour}
                onChange={(e) => setStartHour(Number(e.target.value))}
                className="w-full rounded-lg border border-gray-300 px-3 py-2"
              >
                {START_HOURS.map((h) => (
                  <option key={h} value={h}>
                    {h}:00
                  </option>
                ))}
              </select>
            </label>
            <label className="text-sm">
              <span className="mb-1 block text-gray-600">이용 시간</span>
              <select
                value={duration}
                onChange={(e) => setDuration(Number(e.target.value))}
                className="w-full rounded-lg border border-gray-300 px-3 py-2"
              >
                {DURATIONS.map((d) => (
                  <option key={d} value={d}>
                    {d}시간
                  </option>
                ))}
              </select>
            </label>
          </div>

          {/* 실시간 견적 */}
          {quote && (
            <div className="rounded-xl border border-gray-200 bg-white p-5">
              <div className="flex justify-between text-sm text-gray-600">
                <span>기본요금 ({quote.hours}시간)</span>
                <span>{quote.baseAmount.toLocaleString()}원</span>
              </div>
              {quote.lines.map((line, i) => (
                <div key={i} className="mt-1 flex justify-between text-sm text-gray-500">
                  <span>{line.label}</span>
                  <span className={line.amount < 0 ? 'text-blue-600' : 'text-red-500'}>
                    {line.amount > 0 ? '+' : ''}
                    {line.amount.toLocaleString()}원
                  </span>
                </div>
              ))}
              <div className="mt-3 flex justify-between border-t border-gray-100 pt-3 text-base font-bold text-gray-900">
                <span>합계</span>
                <span>{quote.total.toLocaleString()}원</span>
              </div>
            </div>
          )}

          {/* 예약자 정보 */}
          <div className="grid grid-cols-2 gap-3">
            <label className="text-sm">
              <span className="mb-1 block text-gray-600">예약자명</span>
              <input
                value={guestName}
                onChange={(e) => setGuestName(e.target.value)}
                placeholder="홍길동"
                className="w-full rounded-lg border border-gray-300 px-3 py-2"
              />
            </label>
            <label className="text-sm">
              <span className="mb-1 block text-gray-600">연락처 (선택)</span>
              <input
                value={guestPhone}
                onChange={(e) => setGuestPhone(e.target.value)}
                placeholder="010-0000-0000"
                className="w-full rounded-lg border border-gray-300 px-3 py-2"
              />
            </label>
          </div>

          {conflictMessage && (
            <p className="rounded-lg bg-red-50 px-4 py-2 text-sm text-red-700">{conflictMessage}</p>
          )}

          <button
            disabled={!guestName || reserve.isPending}
            onClick={() => reserve.mutate()}
            className="w-full rounded-xl bg-indigo-600 py-3 font-semibold text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-gray-300"
          >
            {reserve.isPending ? '예약 중…' : `${quote?.total.toLocaleString() ?? ''}원 예약하기`}
          </button>
        </div>
      )}
    </div>
  )
}
