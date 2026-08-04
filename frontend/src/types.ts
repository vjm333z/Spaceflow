// 백엔드 응답 DTO와 1:1 대응하는 타입

export interface Room {
  id: number
  name: string
  capacity: number
  basePricePerHour: number
}

export interface PriceLine {
  label: string
  amount: number
}

export interface PriceQuote {
  roomId: number
  hours: number
  basePricePerHour: number
  baseAmount: number
  lines: PriceLine[]
  total: number
}

export interface Reservation {
  id: number
  roomId: number
  startAt: string
  endAt: string
  status: string
  guestName: string
  price: number | null
}
