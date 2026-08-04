// 토큰을 반응형으로 관리하는 작은 스토어.
// localStorage는 값이 바뀌어도 React가 모르므로, 구독자에게 변경을 알려 재렌더를 유발한다.

const TOKEN_KEY = 'accessToken'
const listeners = new Set<() => void>()

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string | null): void {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
  listeners.forEach((l) => l()) // 구독 중인 컴포넌트 재렌더
}

export function subscribe(callback: () => void): () => void {
  listeners.add(callback)
  return () => listeners.delete(callback)
}
