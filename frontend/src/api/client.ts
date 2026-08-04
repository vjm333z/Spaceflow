import axios from 'axios'
import { getToken, setToken } from '../auth/authStore'

// 모든 API 요청의 공통 설정. baseURL '/api'는 Vite 프록시가 백엔드로 넘긴다.
export const api = axios.create({
  baseURL: '/api',
})

// 로그인 후 저장된 JWT가 있으면 Authorization 헤더에 자동으로 붙인다.
api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 401(토큰 만료/무효)이면 토큰을 제거한다.
// → 공개 엔드포인트는 만료 토큰 때문에 막히지 않고 익명으로 재요청되어 정상 동작한다.
api.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401 && getToken()) {
      setToken(null)
    }
    return Promise.reject(error)
  },
)
