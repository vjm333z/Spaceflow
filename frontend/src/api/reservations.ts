import { api } from './client'
import type { Reservation } from '../types'

export interface CreateReservationPayload {
  roomId: number
  startAt: string
  endAt: string
  guestName: string
  guestPhone?: string
  couponCode?: string
}

export async function createReservation(payload: CreateReservationPayload): Promise<Reservation> {
  const { data } = await api.post<Reservation>('/reservations', payload)
  return data
}

// 내 예약 목록 (로그인 필요)
export async function getMyReservations(): Promise<Reservation[]> {
  const { data } = await api.get<Reservation[]>('/reservations/mine')
  return data
}

// 예약 취소 (본인만)
export async function cancelReservation(id: number): Promise<void> {
  await api.post(`/reservations/${id}/cancel`)
}

// 모의 결제 → 예약 확정
export async function payReservation(id: number): Promise<Reservation> {
  const { data } = await api.post<Reservation>(`/reservations/${id}/pay`)
  return data
}
