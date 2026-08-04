import { api } from './client'

export interface TokenResponse {
  accessToken: string
  tokenType: string
  expiresInSeconds: number
}

export interface Me {
  userId: string
  email: string
  role: string
}

export async function login(email: string, password: string): Promise<TokenResponse> {
  const { data } = await api.post<TokenResponse>('/auth/login', { email, password })
  return data
}

export async function signup(email: string, password: string): Promise<void> {
  await api.post('/auth/signup', { email, password })
}

export async function getMe(): Promise<Me> {
  const { data } = await api.get<Me>('/auth/me')
  return data
}
