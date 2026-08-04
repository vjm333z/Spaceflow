import { useQuery } from '@tanstack/react-query'
import { getRooms } from '../api/rooms'

export default function RoomsPage() {
  // useQuery: 서버 데이터를 가져오고 로딩/에러 상태를 자동 관리 (캐시·재요청까지)
  const { data: rooms, isLoading, isError } = useQuery({
    queryKey: ['rooms'],
    queryFn: getRooms,
  })

  if (isLoading) return <p className="p-8 text-gray-500">불러오는 중…</p>
  if (isError) return <p className="p-8 text-red-600">방 목록을 불러오지 못했어요.</p>

  return (
    <div className="mx-auto max-w-3xl p-8">
      <h1 className="mb-1 text-2xl font-bold text-gray-900">예약 가능한 공간</h1>
      <p className="mb-6 text-sm text-gray-500">원하는 방을 골라 예약하세요.</p>

      <ul className="grid gap-4 sm:grid-cols-2">
        {rooms?.map((room) => (
          <li
            key={room.id}
            className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm transition hover:shadow-md"
          >
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold text-gray-900">{room.name}</h2>
              <span className="rounded-full bg-indigo-50 px-2.5 py-0.5 text-xs font-medium text-indigo-700">
                정원 {room.capacity}
              </span>
            </div>
            <p className="mt-3 text-sm text-gray-500">
              시간당{' '}
              <span className="font-semibold text-gray-900">
                {room.basePricePerHour.toLocaleString()}원
              </span>
            </p>
          </li>
        ))}
      </ul>
    </div>
  )
}
