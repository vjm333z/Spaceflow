import { api } from './client'
import type { Room, PriceQuote } from '../types'

export async function getRooms(): Promise<Room[]> {
  const { data } = await api.get<Room[]>('/rooms')
  return data
}

// axios가 params의 '+09:00'을 %2B로 인코딩해주므로 시각을 그대로 넘기면 된다.
export async function getQuote(roomId: number, start: string, end: string): Promise<PriceQuote> {
  const { data } = await api.get<PriceQuote>(`/rooms/${roomId}/quote`, {
    params: { start, end },
  })
  return data
}
