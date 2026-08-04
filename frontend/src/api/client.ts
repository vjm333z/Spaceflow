import axios from 'axios'

// 모든 API 요청의 공통 설정. baseURL '/api'는 Vite 프록시가 백엔드로 넘긴다.
export const api = axios.create({
  baseURL: '/api',
})

// 로그인 후 저장된 JWT가 있으면 Authorization 헤더에 자동으로 붙인다.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
