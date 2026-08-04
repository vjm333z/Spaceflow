import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import axios from 'axios'
import { getMyReservations } from '../api/owner'
import { getRooms } from '../api/rooms'
import { useAuth } from '../auth/useAuth'

// ISO(UTC) 시각을 한국시간 "MM.DD HH:mm"으로 표시
function fmt(isoUtc: string): string {
  const d = new Date(isoUtc)
  return d.toLocaleString('ko-KR', {
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
      <p className="p-8 text-gray-500">
        로그인이 필요해요.{' '}
        <Link to="/login" className="text-indigo-600 hover:underline">
          로그인
        </Link>
      </p>
    )
  }

  const forbidden = axios.isAxiosError(error) && error.response?.status === 403
  if (forbidden) {
    return <p className="p-8 text-gray-500">사장님(OWNER) 계정만 볼 수 있어요.</p>
  }
  if (isLoading) return <p className="p-8 text-gray-500">불러오는 중…</p>
  if (error) return <p className="p-8 text-red-600">예약을 불러오지 못했어요.</p>

  const total = reservations?.reduce((sum, r) => sum + (r.price ?? 0), 0) ?? 0

  return (
    <div className="mx-auto max-w-3xl p-8">
      <h1 className="text-2xl font-bold text-gray-900">사장님 대시보드</h1>
      <p className="mb-6 text-sm text-gray-500">
        우리 매장 예약 {reservations?.length ?? 0}건 · 매출 {total.toLocaleString()}원
      </p>

      {reservations && reservations.length > 0 ? (
        <div className="overflow-hidden rounded-xl border border-gray-200 bg-white">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-left text-gray-500">
              <tr>
                <th className="px-4 py-3 font-medium">방</th>
                <th className="px-4 py-3 font-medium">시간</th>
                <th className="px-4 py-3 font-medium">예약자</th>
                <th className="px-4 py-3 text-right font-medium">금액</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {reservations.map((r) => (
                <tr key={r.id}>
                  <td className="px-4 py-3 text-gray-900">{roomName(r.roomId)}</td>
                  <td className="px-4 py-3 text-gray-600">
                    {fmt(r.startAt)} ~ {fmt(r.endAt).split(' ').pop()}
                  </td>
                  <td className="px-4 py-3 text-gray-600">{r.guestName}</td>
                  <td className="px-4 py-3 text-right font-medium text-gray-900">
                    {r.price?.toLocaleString()}원
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p className="rounded-xl border border-gray-200 bg-white p-8 text-center text-gray-500">
          아직 예약이 없어요.
        </p>
      )}
    </div>
  )
}
