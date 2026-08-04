import { api } from './client'
import type { Reservation } from '../types'

export interface CreateReservationPayload {
  roomId: number
  startAt: string
  endAt: string
  guestName: string
  guestPhone?: string
}

export async function createReservation(payload: CreateReservationPayload): Promise<Reservation> {
  const { data } = await api.post<Reservation>('/reservations', payload)
  return data
}
