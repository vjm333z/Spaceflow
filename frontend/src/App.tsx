import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import Layout from './components/Layout'
import RoomsPage from './pages/RoomsPage'
import BookingPage from './pages/BookingPage'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'
import OwnerDashboardPage from './pages/OwnerDashboardPage'
import MyReservationsPage from './pages/MyReservationsPage'
import OwnerRoomsPage from './pages/OwnerRoomsPage'

// Layout(공통 헤더) 아래에 각 페이지를 중첩 배치한다.
const router = createBrowserRouter([
  {
    element: <Layout />,
    children: [
      { path: '/', element: <RoomsPage /> },
      { path: '/rooms/:roomId/book', element: <BookingPage /> },
      { path: '/login', element: <LoginPage /> },
      { path: '/signup', element: <SignupPage /> },
      { path: '/owner', element: <OwnerDashboardPage /> },
      { path: '/owner/rooms', element: <OwnerRoomsPage /> },
      { path: '/my', element: <MyReservationsPage /> },
    ],
  },
])

export default function App() {
  return <RouterProvider router={router} />
}
