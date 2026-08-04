import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import axios from 'axios'
import { login, signup } from '../api/auth'
import { useAuth } from '../auth/useAuth'

export default function SignupPage() {
  const navigate = useNavigate()
  const { setToken } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  const mutation = useMutation({
    mutationFn: async () => {
      await signup(email, password)
      return login(email, password) // 가입 후 자동 로그인
    },
    onSuccess: (token) => {
      setToken(token.accessToken)
      navigate('/')
    },
  })

  const errorMsg = axios.isAxiosError(mutation.error)
    ? mutation.error.response?.status === 409
      ? '이미 가입된 이메일입니다.'
      : mutation.error.response?.status === 400
        ? '이메일 형식과 비밀번호(8자 이상)를 확인해주세요.'
        : '회원가입에 실패했어요.'
    : null

  return (
    <div className="mx-auto max-w-sm p-8">
      <h1 className="mb-6 text-2xl font-bold text-gray-900">회원가입</h1>
      <form
        className="space-y-4"
        onSubmit={(e) => {
          e.preventDefault()
          mutation.mutate()
        }}
      >
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="이메일"
          className="w-full rounded-lg border border-gray-300 px-3 py-2"
        />
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="비밀번호 (8자 이상)"
          className="w-full rounded-lg border border-gray-300 px-3 py-2"
        />
        {errorMsg && <p className="text-sm text-red-600">{errorMsg}</p>}
        <button
          type="submit"
          disabled={!email || password.length < 8 || mutation.isPending}
          className="w-full rounded-xl bg-indigo-600 py-3 font-semibold text-white hover:bg-indigo-700 disabled:bg-gray-300"
        >
          {mutation.isPending ? '가입 중…' : '회원가입'}
        </button>
      </form>
      <p className="mt-4 text-center text-sm text-gray-500">
        이미 계정이 있으신가요?{' '}
        <Link to="/login" className="text-indigo-600 hover:underline">
          로그인
        </Link>
      </p>
    </div>
  )
}
