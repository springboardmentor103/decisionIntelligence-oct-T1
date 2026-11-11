import { Outlet } from 'react-router-dom'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'
import { useState } from 'react'

export default function DashboardLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)

  return (
    <div className="app-shell">
      <Navbar onMenuClick={() => setSidebarOpen((open) => !open)} />
      <div className="shell-body">
        <Sidebar open={sidebarOpen} onNavigate={() => setSidebarOpen(false)} />
        <main className="content-panel">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
