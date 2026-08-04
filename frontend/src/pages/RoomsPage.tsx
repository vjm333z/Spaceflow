import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getRooms } from '../api/rooms'

export default function RoomsPage() {
  const { data: rooms, isLoading, isError } = useQuery({
    queryKey: ['rooms'],
    queryFn: getRooms,
  })

  return (
    <div className="mx-auto max-w-4xl px-6 py-12">
      {/* 히어로 */}
      <section className="mb-10">
        <span className="inline-flex items-center gap-1.5 rounded-full bg-indigo-50 px-3 py-1 text-xs font-semibold text-indigo-700 ring-1 ring-indigo-100">
          ⚡ 실시간 예약
        </span>
        <h1 className="mt-4 text-4xl font-extrabold tracking-tight text-slate-900 sm:text-[2.75rem]">
          공간을 시간 단위로,
          <br />
          <span className="bg-gradient-to-r from-indigo-600 to-violet-600 bg-clip-text text-transparent">
            간편하게 예약하세요
          </span>
        </h1>
        <p className="mt-4 max-w-lg text-base text-slate-500">
          스터디룸·회의실을 원하는 시간만 골라 바로 예약. 요일·시간대에 따라 요금이 실시간으로 계산됩니다.
        </p>
      </section>

      <h2 className="mb-4 text-sm font-semibold text-slate-400">예약 가능한 공간</h2>

      {isLoading && (
        <div className="grid gap-4 sm:grid-cols-2">
          {[0, 1].map((i) => (
            <div key={i} className="h-32 animate-pulse rounded-2xl bg-slate-100" />
          ))}
        </div>
      )}

      {isError && (
        <p className="rounded-xl bg-red-50 p-4 text-sm text-red-600">방 목록을 불러오지 못했어요.</p>
      )}

      <ul className="grid gap-4 sm:grid-cols-2">
        {rooms?.map((room) => (
          <li key={room.id}>
            <Link
              to={`/rooms/${room.id}/book`}
              className="group block rounded-2xl border border-slate-200 bg-white p-6 shadow-sm ring-1 ring-transparent transition hover:-translate-y-0.5 hover:border-indigo-200 hover:shadow-lg hover:ring-indigo-100"
            >
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-indigo-50 text-indigo-600">
                    <svg viewBox="0 0 24 24" className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M3 21h18M6 21V5a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v16" strokeLinecap="round" />
                      <circle cx="14" cy="12" r="1" fill="currentColor" />
                    </svg>
                  </span>
                  <div>
                    <h3 className="text-lg font-bold text-slate-900">{room.name}</h3>
                    <p className="text-sm text-slate-400">정원 {room.capacity}명</p>
                  </div>
                </div>
                <span className="translate-x-0 text-slate-300 transition group-hover:translate-x-1 group-hover:text-indigo-500">
                  →
                </span>
              </div>
              <div className="mt-5 flex items-baseline gap-1 border-t border-slate-100 pt-4">
                <span className="text-xl font-extrabold text-slate-900">
                  {room.basePricePerHour.toLocaleString()}원
                </span>
                <span className="text-sm text-slate-400">/ 시간</span>
              </div>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  )
}
