import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // 프론트(/api/...) 요청을 백엔드로 프록시 → 브라우저 입장에선 같은 출처(CORS 회피)
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
