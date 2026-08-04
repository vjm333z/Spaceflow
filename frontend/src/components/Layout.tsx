import { Link, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

export default function Layout() {
  const { user, logout } = useAuth()

  return (
    <div className="app-bg min-h-screen">
      <header className="sticky top-0 z-10 border-b border-slate-200/70 bg-white/80 backdrop-blur">
        <div className="mx-auto flex max-w-4xl items-center justify-between px-6 py-3.5">
          <Link to="/" className="flex items-center gap-2.5">
            <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-indigo-500 to-violet-600 shadow-sm">
              <svg viewBox="0 0 24 24" className="h-4 w-4 text-white" fill="none" stroke="currentColor" strokeWidth="2.2">
                <rect x="3" y="4" width="18" height="17" rx="3" />
                <path d="M3 9h18M8 2v4M16 2v4" strokeLinecap="round" />
              </svg>
            </span>
            <span className="text-lg font-extrabold tracking-tight text-slate-900">SpaceFlow</span>
          </Link>

          <nav className="flex items-center gap-3 text-sm">
            {user ? (
              <>
                {user.role === 'OWNER' && (
                  <Link
                    to="/owner"
                    className="rounded-lg px-3 py-1.5 font-semibold text-indigo-600 hover:bg-indigo-50"
                  >
                    대시보드
                  </Link>
                )}
                <span className="hidden text-slate-500 sm:inline">{user.email}</span>
                <button
                  onClick={logout}
                  className="rounded-lg px-3 py-1.5 text-slate-500 hover:bg-slate-100 hover:text-slate-900"
                >
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="rounded-lg px-3 py-1.5 font-medium text-slate-600 hover:bg-slate-100">
                  로그인
                </Link>
                <Link
                  to="/signup"
                  className="rounded-lg bg-slate-900 px-3.5 py-1.5 font-semibold text-white shadow-sm hover:bg-slate-700"
                >
                  회원가입
                </Link>
              </>
            )}
          </nav>
        </div>
      </header>

      <main>
        <Outlet />
      </main>
    </div>
  )
}
