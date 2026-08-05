import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { cancelReservation, getMyReservations, payReservation } from '../api/reservations'
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

export default function MyReservationsPage() {
  const { user } = useAuth()
  const queryClient = useQueryClient()

  const { data: reservations, isLoading } = useQuery({
    queryKey: ['my-reservations'],
    queryFn: getMyReservations,
    enabled: !!user,
  })
  const { data: rooms } = useQuery({ queryKey: ['rooms'], queryFn: getRooms })
  const roomName = (id: number) => rooms?.find((r) => r.id === id)?.name ?? `방 #${id}`

  const cancel = useMutation({
    mutationFn: (id: number) => cancelReservation(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-reservations'] })
      queryClient.invalidateQueries({ queryKey: ['availability'] })
    },
  })
  const pay = useMutation({
    mutationFn: (id: number) => payReservation(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['my-reservations'] }),
  })

  if (!user) {
    return (
      <p className="mx-auto max-w-2xl px-6 py-12 text-slate-500">
        로그인이 필요해요. <Link to="/login" className="font-semibold text-indigo-600 hover:underline">로그인</Link>
      </p>
    )
  }

  return (
    <div className="mx-auto max-w-2xl px-6 py-10">
      <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">내 예약</h1>
      <p className="mt-0.5 mb-6 text-sm text-slate-400">예약 내역을 확인하고 취소할 수 있어요.</p>

      {isLoading && <p className="text-slate-500">불러오는 중…</p>}

      {reservations && reservations.length === 0 && (
        <div className="rounded-2xl border border-slate-200 bg-white p-10 text-center">
          <p className="text-slate-400">아직 예약이 없어요.</p>
          <Link to="/" className="mt-3 inline-block font-semibold text-indigo-600 hover:underline">공간 둘러보기 →</Link>
        </div>
      )}

      <ul className="space-y-3">
        {reservations?.map((r) => {
          const cancelled = r.status === 'CANCELLED'
          const pending = r.status === 'PENDING'
          return (
            <li
              key={r.id}
              className={`flex items-center justify-between rounded-2xl border p-5 shadow-sm ${cancelled ? 'border-slate-200 bg-slate-50' : 'border-slate-200 bg-white'}`}
            >
              <div>
                <div className="flex items-center gap-2">
                  <span className={`font-bold ${cancelled ? 'text-slate-400 line-through' : 'text-slate-900'}`}>
                    {roomName(r.roomId)}
                  </span>
                  {cancelled && <span className="rounded-full bg-slate-200 px-2 py-0.5 text-xs font-medium text-slate-500">취소됨</span>}
                  {pending && <span className="rounded-full bg-amber-100 px-2 py-0.5 text-xs font-semibold text-amber-700">결제 대기</span>}
                  {r.status === 'CONFIRMED' && <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-semibold text-emerald-700">확정</span>}
                </div>
                <p className="mt-1 text-sm text-slate-500">
                  {fmt(r.startAt)} ~ {fmt(r.endAt).split(' ').pop()} · {r.price?.toLocaleString()}원
                </p>
              </div>
              {!cancelled && (
                <div className="flex gap-2">
                  {pending && (
                    <button
                      onClick={() => pay.mutate(r.id)}
                      disabled={pay.isPending}
                      className="rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-50"
                    >
                      결제하기
                    </button>
                  )}
                  <button
                    onClick={() => cancel.mutate(r.id)}
                    disabled={cancel.isPending}
                    className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-600 transition hover:border-rose-300 hover:bg-rose-50 hover:text-rose-600 disabled:opacity-50"
                  >
                    취소
                  </button>
                </div>
              )}
            </li>
          )
        })}
      </ul>
    </div>
  )
}
