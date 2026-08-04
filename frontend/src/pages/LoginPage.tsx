import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import axios from 'axios'
import { login } from '../api/auth'
import { useAuth } from '../auth/useAuth'

const inputCls =
  'w-full rounded-xl border border-slate-300 bg-white px-3.5 py-2.5 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100'

export default function LoginPage() {
  const navigate = useNavigate()
  const { setToken } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  const mutation = useMutation({
    mutationFn: () => login(email, password),
    onSuccess: (data) => {
      setToken(data.accessToken)
      navigate('/')
    },
  })

  const errorMsg =
    axios.isAxiosError(mutation.error) && mutation.error.response?.status === 400
      ? '이메일 또는 비밀번호가 올바르지 않습니다.'
      : mutation.isError
        ? '로그인에 실패했어요.'
        : null

  return (
    <div className="mx-auto flex min-h-[70vh] max-w-sm flex-col justify-center px-6">
      <div className="rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
        <h1 className="text-xl font-extrabold tracking-tight text-slate-900">로그인</h1>
        <p className="mt-1 mb-6 text-sm text-slate-400">SpaceFlow에 오신 걸 환영해요.</p>
        <form
          className="space-y-3"
          onSubmit={(e) => {
            e.preventDefault()
            mutation.mutate()
          }}
        >
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="이메일" className={inputCls} />
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="비밀번호" className={inputCls} />
          {errorMsg && <p className="text-sm text-rose-600">{errorMsg}</p>}
          <button
            type="submit"
            disabled={!email || !password || mutation.isPending}
            className="w-full rounded-xl bg-indigo-600 py-3 font-bold text-white transition hover:bg-indigo-700 disabled:bg-slate-200 disabled:text-slate-400"
          >
            {mutation.isPending ? '로그인 중…' : '로그인'}
          </button>
        </form>
      </div>
      <p className="mt-4 text-center text-sm text-slate-500">
        계정이 없으신가요?{' '}
        <Link to="/signup" className="font-semibold text-indigo-600 hover:underline">회원가입</Link>
      </p>
    </div>
  )
}
