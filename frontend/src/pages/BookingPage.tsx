import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { getAvailability, getQuote, getRooms } from '../api/rooms'
import { createReservation } from '../api/reservations'

function iso(date: string, hour: number): string {
  return `${date}T${String(hour).padStart(2, '0')}:00:00+09:00`
}

// UTC ISO 시각 → 한국시간의 '시(hour)'
function kstHour(isoUtc: string): number {
  return new Date(new Date(isoUtc).getTime() + 9 * 3600 * 1000).getUTCHours()
}

const OPEN = 8
const CLOSE = 22
const START_HOURS = Array.from({ length: CLOSE - OPEN }, (_, i) => OPEN + i) // 8~21
const DURATIONS = [1, 2, 3, 4]

export default function BookingPage() {
  const { roomId } = useParams()
  const id = Number(roomId)
  const queryClient = useQueryClient()

  const [date, setDate] = useState('2026-09-12')
  const [startHour, setStartHour] = useState(9)
  const [duration, setDuration] = useState(2)
  const [guestName, setGuestName] = useState('')
  const [guestPhone, setGuestPhone] = useState('')

  const startAt = iso(date, startHour)
  const endAt = iso(date, startHour + duration)

  const { data: rooms } = useQuery({ queryKey: ['rooms'], queryFn: getRooms })
  const room = rooms?.find((r) => r.id === id)

  const { data: booked } = useQuery({
    queryKey: ['availability', id, date],
    queryFn: () => getAvailability(id, date),
    enabled: !!id,
  })

  // 예약으로 막힌 '시(hour)' 집합
  const bookedHours = new Set<number>()
  booked?.forEach((slot) => {
    for (let h = kstHour(slot.startAt); h < kstHour(slot.endAt); h++) bookedHours.add(h)
  })

  const endHour = startHour + duration
  const rangeHours = Array.from({ length: duration }, (_, i) => startHour + i)
  const overlaps = rangeHours.some((h) => bookedHours.has(h))
  const overClose = endHour > CLOSE

  const { data: quote } = useQuery({
    queryKey: ['quote', id, startAt, endAt],
    queryFn: () => getQuote(id, startAt, endAt),
    enabled: !!id,
  })

  const reserve = useMutation({
    mutationFn: () => createReservation({ roomId: id, startAt, endAt, guestName, guestPhone }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['availability', id, date] })
      queryClient.invalidateQueries({ queryKey: ['reservations'] })
    },
  })

  const conflictMessage =
    axios.isAxiosError(reserve.error)
      ? (reserve.error.response?.data as { error?: string })?.error ?? '예약에 실패했어요.'
      : null

  const inputCls =
    'w-full rounded-xl border border-slate-300 bg-white px-3.5 py-2.5 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100'

  const canReserve = !!guestName && !overlaps && !overClose && !reserve.isPending

  return (
    <div className="mx-auto max-w-xl px-6 py-10">
      <Link to="/" className="inline-flex items-center gap-1 text-sm text-slate-500 hover:text-slate-800">
        ← 방 목록
      </Link>

      <div className="mt-4 mb-6">
        <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">{room?.name ?? '예약'}</h1>
        {room && (
          <p className="mt-0.5 text-sm text-slate-400">
            정원 {room.capacity}명 · 시간당 {room.basePricePerHour.toLocaleString()}원
          </p>
        )}
      </div>

      {reserve.isSuccess ? (
        <div className="rounded-2xl border border-emerald-200 bg-emerald-50 p-8 text-center">
          <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-emerald-500 text-white">
            <svg viewBox="0 0 24 24" className="h-6 w-6" fill="none" stroke="currentColor" strokeWidth="3">
              <path d="M5 13l4 4L19 7" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </div>
          <p className="mt-4 text-lg font-bold text-emerald-900">예약이 완료됐어요</p>
          <p className="mt-1 text-sm text-emerald-700">
            {date} {startHour}:00~{endHour}:00
          </p>
          <p className="mt-3 text-2xl font-extrabold text-emerald-900">{reserve.data.price?.toLocaleString()}원</p>
          <button
            className="mt-6 rounded-xl bg-emerald-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-emerald-700"
            onClick={() => reserve.reset()}
          >
            다른 시간 예약하기
          </button>
        </div>
      ) : (
        <div className="space-y-5">
          {/* 날짜 + 시간 슬롯 */}
          <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <label className="mb-4 block text-sm">
              <span className="mb-1.5 block font-medium text-slate-600">날짜</span>
              <input type="date" value={date} onChange={(e) => setDate(e.target.value)} className={inputCls} />
            </label>

            <p className="mb-2 text-sm font-medium text-slate-600">시작 시간</p>
            <div className="grid grid-cols-7 gap-1.5">
              {START_HOURS.map((h) => {
                const isBooked = bookedHours.has(h)
                const isSelected = h === startHour
                return (
                  <button
                    key={h}
                    disabled={isBooked}
                    onClick={() => setStartHour(h)}
                    className={
                      'rounded-lg py-2 text-sm font-medium transition ' +
                      (isBooked
                        ? 'cursor-not-allowed bg-slate-100 text-slate-300 line-through'
                        : isSelected
                          ? 'bg-indigo-600 text-white shadow-sm'
                          : 'bg-white text-slate-700 ring-1 ring-slate-200 hover:ring-indigo-300')
                    }
                  >
                    {h}
                  </button>
                )
              })}
            </div>
            <div className="mt-2 flex gap-3 text-xs text-slate-400">
              <span className="inline-flex items-center gap-1"><span className="h-2.5 w-2.5 rounded-sm bg-indigo-600" /> 선택</span>
              <span className="inline-flex items-center gap-1"><span className="h-2.5 w-2.5 rounded-sm bg-slate-200" /> 예약됨</span>
            </div>

            <label className="mt-4 block text-sm">
              <span className="mb-1.5 block font-medium text-slate-600">이용 시간</span>
              <select value={duration} onChange={(e) => setDuration(Number(e.target.value))} className={inputCls}>
                {DURATIONS.map((d) => (
                  <option key={d} value={d}>{d}시간 ({startHour}:00~{startHour + d}:00)</option>
                ))}
              </select>
            </label>
          </div>

          {(overlaps || overClose) && (
            <p className="rounded-xl bg-amber-50 px-4 py-3 text-sm text-amber-700">
              {overClose ? '영업 종료(22:00)를 넘는 시간이에요.' : '선택한 시간에 이미 예약이 있어요. 다른 시간을 골라주세요.'}
            </p>
          )}

          {/* 실시간 견적 */}
          {quote && !overlaps && !overClose && (
            <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
              <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-slate-400">예상 요금</p>
              <div className="flex justify-between text-sm text-slate-600">
                <span>기본요금 · {quote.hours}시간</span>
                <span className="tabular-nums">{quote.baseAmount.toLocaleString()}원</span>
              </div>
              {quote.lines.map((line, i) => (
                <div key={i} className="mt-1.5 flex justify-between text-sm">
                  <span className="text-slate-500">{line.label}</span>
                  <span className={`tabular-nums ${line.amount < 0 ? 'text-blue-600' : 'text-rose-500'}`}>
                    {line.amount > 0 ? '+' : ''}{line.amount.toLocaleString()}원
                  </span>
                </div>
              ))}
              <div className="mt-4 flex items-baseline justify-between border-t border-slate-100 pt-4">
                <span className="font-semibold text-slate-900">합계</span>
                <span className="text-2xl font-extrabold tabular-nums text-slate-900">{quote.total.toLocaleString()}원</span>
              </div>
            </div>
          )}

          {/* 예약자 정보 */}
          <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <div className="grid grid-cols-2 gap-3">
              <label className="text-sm">
                <span className="mb-1.5 block font-medium text-slate-600">예약자명</span>
                <input value={guestName} onChange={(e) => setGuestName(e.target.value)} placeholder="홍길동" className={inputCls} />
              </label>
              <label className="text-sm">
                <span className="mb-1.5 block font-medium text-slate-600">연락처 <span className="text-slate-300">(선택)</span></span>
                <input value={guestPhone} onChange={(e) => setGuestPhone(e.target.value)} placeholder="010-0000-0000" className={inputCls} />
              </label>
            </div>
          </div>

          {conflictMessage && <p className="rounded-xl bg-rose-50 px-4 py-3 text-sm text-rose-700">{conflictMessage}</p>}

          <button
            disabled={!canReserve}
            onClick={() => reserve.mutate()}
            className="w-full rounded-2xl bg-indigo-600 py-3.5 text-base font-bold text-white shadow-sm transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-slate-200 disabled:text-slate-400"
          >
            {reserve.isPending ? '예약 중…' : quote && !overlaps && !overClose ? `${quote.total.toLocaleString()}원 예약하기` : '예약하기'}
          </button>
        </div>
      )}
    </div>
  )
}
