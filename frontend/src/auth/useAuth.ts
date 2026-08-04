import { useSyncExternalStore } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { getMe } from '../api/auth'
import { getToken, setToken as storeSetToken, subscribe } from './authStore'

/**
 * 인증 상태 훅.
 * 토큰을 반응형 스토어(useSyncExternalStore)로 구독하므로, 로그인/로그아웃 시 즉시 재렌더된다.
 * 토큰이 있으면 /auth/me로 현재 사용자를 조회한다.
 */
export function useAuth() {
  const queryClient = useQueryClient()
  const token = useSyncExternalStore(subscribe, getToken)

  const { data: user } = useQuery({
    queryKey: ['me'],
    queryFn: getMe,
    enabled: !!token, // 토큰 있을 때만 조회
    retry: false, // 만료 등으로 실패해도 재시도 안 함
  })

  function setToken(newToken: string) {
    storeSetToken(newToken) // 스토어 변경 → 구독자 재렌더 → me 조회 활성화
  }

  function logout() {
    storeSetToken(null)
    queryClient.setQueryData(['me'], null)
  }

  return { user: token ? user ?? null : null, setToken, logout }
}
