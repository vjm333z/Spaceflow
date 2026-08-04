import { Link, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

export default function Layout() {
  const { user, logout } = useAuth()

  return (
    <div className="min-h-screen">
      <header className="border-b border-gray-200 bg-white">
        <div className="mx-auto flex max-w-3xl items-center justify-between px-8 py-4">
          <Link to="/" className="text-lg font-bold text-indigo-600">
            SpaceFlow
          </Link>
          <nav className="flex items-center gap-4 text-sm">
            {user ? (
              <>
                {user.role === 'OWNER' && (
                  <Link to="/owner" className="font-medium text-indigo-600 hover:underline">
                    대시보드
                  </Link>
                )}
                <span className="text-gray-600">{user.email}</span>
                <button onClick={logout} className="text-gray-500 hover:text-gray-900">
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="text-gray-600 hover:text-gray-900">
                  로그인
                </Link>
                <Link
                  to="/signup"
                  className="rounded-lg bg-indigo-600 px-3 py-1.5 font-medium text-white hover:bg-indigo-700"
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
