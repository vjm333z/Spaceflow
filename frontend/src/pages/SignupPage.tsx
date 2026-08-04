import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import axios from 'axios'
import { login, signup } from '../api/auth'
import { useAuth } from '../auth/useAuth'

const inputCls =
  'w-full rounded-xl border border-slate-300 bg-white px-3.5 py-2.5 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100'

export default function SignupPage() {
  const navigate = useNavigate()
  const { setToken } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  const mutation = useMutation({
    mutationFn: async () => {
      await signup(email, password)
      return login(email, password)
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
    <div className="mx-auto flex min-h-[70vh] max-w-sm flex-col justify-center px-6">
      <div className="rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
        <h1 className="text-xl font-extrabold tracking-tight text-slate-900">회원가입</h1>
        <p className="mt-1 mb-6 text-sm text-slate-400">이메일로 간편하게 시작하세요.</p>
        <form
          className="space-y-3"
          onSubmit={(e) => {
            e.preventDefault()
            mutation.mutate()
          }}
        >
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="이메일" className={inputCls} />
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="비밀번호 (8자 이상)" className={inputCls} />
          {errorMsg && <p className="text-sm text-rose-600">{errorMsg}</p>}
          <button
            type="submit"
            disabled={!email || password.length < 8 || mutation.isPending}
            className="w-full rounded-xl bg-indigo-600 py-3 font-bold text-white transition hover:bg-indigo-700 disabled:bg-slate-200 disabled:text-slate-400"
          >
            {mutation.isPending ? '가입 중…' : '회원가입'}
          </button>
        </form>
      </div>
      <p className="mt-4 text-center text-sm text-slate-500">
        이미 계정이 있으신가요?{' '}
        <Link to="/login" className="font-semibold text-indigo-600 hover:underline">로그인</Link>
      </p>
    </div>
  )
}
