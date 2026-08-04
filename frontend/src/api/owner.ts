import { api } from './client'
import type { Reservation } from '../types'

// 로그인한 사장의 테넌트 예약만 반환 (tenantId는 백엔드가 JWT에서 추출)
export async function getMyReservations(): Promise<Reservation[]> {
  const { data } = await api.get<Reservation[]>('/owner/reservations')
  return data
}
