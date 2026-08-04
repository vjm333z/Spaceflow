import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import Layout from './components/Layout'
import RoomsPage from './pages/RoomsPage'
import BookingPage from './pages/BookingPage'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'

// Layout(공통 헤더) 아래에 각 페이지를 중첩 배치한다.
const router = createBrowserRouter([
  {
    element: <Layout />,
    children: [
      { path: '/', element: <RoomsPage /> },
      { path: '/rooms/:roomId/book', element: <BookingPage /> },
      { path: '/login', element: <LoginPage /> },
      { path: '/signup', element: <SignupPage /> },
    ],
  },
])

export default function App() {
  return <RouterProvider router={router} />
}
