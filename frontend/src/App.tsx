import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import RoomsPage from './pages/RoomsPage'
import BookingPage from './pages/BookingPage'

// 화면(경로) 정의. 페이지가 늘면 여기에 추가한다.
const router = createBrowserRouter([
  { path: '/', element: <RoomsPage /> },
  { path: '/rooms/:roomId/book', element: <BookingPage /> },
])

export default function App() {
  return <RouterProvider router={router} />
}
