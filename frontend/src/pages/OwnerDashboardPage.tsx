import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import axios from 'axios'
import { getMyReservations } from '../api/owner'
import { getRooms } from '../api/rooms'
import { useAuth } from '../auth/useAuth'

function fmt(isoUtc: string): string {
  return new Date(isoUtc).toLocaleString('ko-KR', {
    timeZone: 'Asia/Seoul',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export default function OwnerDashboardPage() {
  const { user } = useAuth()

  const { data: reservations, isLoading, error } = useQuery({
    queryKey: ['owner-reservations'],
    queryFn: getMyReservations,
    retry: false,
    enabled: !!user,
  })
  const { data: rooms } = useQuery({ queryKey: ['rooms'], queryFn: getRooms })
  const roomName = (id: number) => rooms?.find((r) => r.id === id)?.name ?? `방 #${id}`

  if (!user) {
    return (
      <p className="mx-auto max-w-4xl px-6 py-12 text-slate-500">
        로그인이 필요해요. <Link to="/login" className="font-semibold text-indigo-600 hover:underline">로그인</Link>
      </p>
    )
  }

  const forbidden = axios.isAxiosError(error) && error.response?.status === 403
  if (forbidden) return <p className="mx-auto max-w-4xl px-6 py-12 text-slate-500">사장님(OWNER) 계정만 볼 수 있어요.</p>
  if (isLoading) return <p className="mx-auto max-w-4xl px-6 py-12 text-slate-500">불러오는 중…</p>
  if (error) return <p className="mx-auto max-w-4xl px-6 py-12 text-rose-600">예약을 불러오지 못했어요.</p>

  const total = reservations?.reduce((sum, r) => sum + (r.price ?? 0), 0) ?? 0
  const count = reservations?.length ?? 0

  return (
    <div className="mx-auto max-w-4xl px-6 py-10">
      <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">사장님 대시보드</h1>
      <p className="mt-0.5 text-sm text-slate-400">우리 매장 예약 현황이에요.</p>

      {/* 통계 카드 */}
      <div className="mt-6 grid grid-cols-2 gap-4">
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p className="text-sm text-slate-400">총 예약</p>
          <p className="mt-1 text-3xl font-extrabold tabular-nums text-slate-900">{count}<span className="ml-1 text-base font-medium text-slate-400">건</span></p>
        </div>
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p className="text-sm text-slate-400">누적 매출</p>
          <p className="mt-1 text-3xl font-extrabold tabular-nums text-indigo-600">{total.toLocaleString()}<span className="ml-1 text-base font-medium text-slate-400">원</span></p>
        </div>
      </div>

      {count > 0 ? (
        <div className="mt-6 overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-400">
              <tr>
                <th className="px-5 py-3">방</th>
                <th className="px-5 py-3">시간</th>
                <th className="px-5 py-3">예약자</th>
                <th className="px-5 py-3 text-right">금액</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {reservations!.map((r) => (
                <tr key={r.id} className="transition hover:bg-slate-50">
                  <td className="px-5 py-3.5 font-medium text-slate-900">{roomName(r.roomId)}</td>
                  <td className="px-5 py-3.5 text-slate-500">{fmt(r.startAt)} ~ {fmt(r.endAt).split(' ').pop()}</td>
                  <td className="px-5 py-3.5 text-slate-600">{r.guestName}</td>
                  <td className="px-5 py-3.5 text-right font-semibold tabular-nums text-slate-900">{r.price?.toLocaleString()}원</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p className="mt-6 rounded-2xl border border-slate-200 bg-white p-10 text-center text-slate-400">아직 예약이 없어요.</p>
      )}
    </div>
  )
}
