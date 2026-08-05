import { api } from './client'
import type { Room } from '../types'

export interface RoomForm {
  name: string
  capacity: number
  basePricePerHour: number
}

export async function getMyRooms(): Promise<Room[]> {
  const { data } = await api.get<Room[]>('/owner/rooms')
  return data
}

export async function createRoom(form: RoomForm): Promise<Room> {
  const { data } = await api.post<Room>('/owner/rooms', form)
  return data
}

export async function updateRoom(id: number, form: RoomForm): Promise<Room> {
  const { data } = await api.put<Room>(`/owner/rooms/${id}`, form)
  return data
}
