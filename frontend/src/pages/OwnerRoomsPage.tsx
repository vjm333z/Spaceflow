import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import type { Room } from '../types'
import { createRoom, getMyRooms, updateRoom, type RoomForm } from '../api/ownerRooms'
import { useAuth } from '../auth/useAuth'

const inputCls =
  'w-full rounded-xl border border-slate-300 bg-white px-3 py-2 text-slate-900 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100'

const EMPTY: RoomForm = { name: '', capacity: 4, basePricePerHour: 10000 }

export default function OwnerRoomsPage() {
  const { user } = useAuth()
  const queryClient = useQueryClient()
  const [addForm, setAddForm] = useState<RoomForm>(EMPTY)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editForm, setEditForm] = useState<RoomForm>(EMPTY)

  const { data: rooms, isLoading } = useQuery({ queryKey: ['owner-rooms'], queryFn: getMyRooms, enabled: !!user })

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['owner-rooms'] })
    queryClient.invalidateQueries({ queryKey: ['rooms'] })
  }
  const add = useMutation({
    mutationFn: () => createRoom(addForm),
    onSuccess: () => { setAddForm(EMPTY); invalidate() },
  })
  const edit = useMutation({
    mutationFn: () => updateRoom(editingId!, editForm),
    onSuccess: () => { setEditingId(null); invalidate() },
  })

  const startEdit = (r: Room) => {
    setEditingId(r.id)
    setEditForm({ name: r.name, capacity: r.capacity, basePricePerHour: Number(r.basePricePerHour) })
  }

  if (!user) {
    return <p className="mx-auto max-w-3xl px-6 py-12 text-slate-500">로그인이 필요해요. <Link to="/login" className="font-semibold text-indigo-600">로그인</Link></p>
  }

  return (
    <div className="mx-auto max-w-3xl px-6 py-10">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">방 관리</h1>
          <p className="mt-0.5 text-sm text-slate-400">우리 매장의 방과 요금을 관리해요.</p>
        </div>
        <Link to="/owner" className="text-sm font-medium text-indigo-600 hover:underline">← 대시보드</Link>
      </div>

      {/* 방 추가 */}
      <form
        onSubmit={(e) => { e.preventDefault(); add.mutate() }}
        className="mt-6 grid grid-cols-[1fr_auto_auto_auto] items-end gap-3 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
      >
        <label className="text-sm">
          <span className="mb-1 block font-medium text-slate-600">방 이름</span>
          <input value={addForm.name} onChange={(e) => setAddForm({ ...addForm, name: e.target.value })} placeholder="4인실 C" className={inputCls} />
        </label>
        <label className="text-sm">
          <span className="mb-1 block font-medium text-slate-600">정원</span>
          <input type="number" min={1} value={addForm.capacity} onChange={(e) => setAddForm({ ...addForm, capacity: Number(e.target.value) })} className={`${inputCls} w-24`} />
        </label>
        <label className="text-sm">
          <span className="mb-1 block font-medium text-slate-600">시간당(원)</span>
          <input type="number" min={0} step={500} value={addForm.basePricePerHour} onChange={(e) => setAddForm({ ...addForm, basePricePerHour: Number(e.target.value) })} className={`${inputCls} w-32`} />
        </label>
        <button
          disabled={!addForm.name || add.isPending}
          className="rounded-xl bg-indigo-600 px-5 py-2 font-semibold text-white transition hover:bg-indigo-700 disabled:bg-slate-200 disabled:text-slate-400"
        >
          추가
        </button>
      </form>

      {/* 방 목록 */}
      {isLoading && <p className="mt-6 text-slate-500">불러오는 중…</p>}
      <ul className="mt-4 space-y-3">
        {rooms?.map((r) => (
          <li key={r.id} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            {editingId === r.id ? (
              <form onSubmit={(e) => { e.preventDefault(); edit.mutate() }} className="grid grid-cols-[1fr_auto_auto_auto_auto] items-end gap-3">
                <input value={editForm.name} onChange={(e) => setEditForm({ ...editForm, name: e.target.value })} className={inputCls} />
                <input type="number" min={1} value={editForm.capacity} onChange={(e) => setEditForm({ ...editForm, capacity: Number(e.target.value) })} className={`${inputCls} w-20`} />
                <input type="number" min={0} step={500} value={editForm.basePricePerHour} onChange={(e) => setEditForm({ ...editForm, basePricePerHour: Number(e.target.value) })} className={`${inputCls} w-28`} />
                <button className="rounded-lg bg-indigo-600 px-3 py-2 text-sm font-semibold text-white hover:bg-indigo-700">저장</button>
                <button type="button" onClick={() => setEditingId(null)} className="rounded-lg px-3 py-2 text-sm text-slate-500 hover:bg-slate-100">취소</button>
              </form>
            ) : (
              <div className="flex items-center justify-between">
                <div>
                  <span className="font-bold text-slate-900">{r.name}</span>
                  <p className="mt-0.5 text-sm text-slate-400">정원 {r.capacity}명 · 시간당 {r.basePricePerHour.toLocaleString()}원</p>
                </div>
                <button onClick={() => startEdit(r)} className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-600 transition hover:border-indigo-300 hover:text-indigo-600">
                  수정
                </button>
              </div>
            )}
          </li>
        ))}
      </ul>
    </div>
  )
}
