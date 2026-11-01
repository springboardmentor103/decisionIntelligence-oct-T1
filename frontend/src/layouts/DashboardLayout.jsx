import { Outlet } from 'react-router-dom'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'

export default function DashboardLayout() {
  return (
    <div className="app-shell">
      <Navbar />
      <div className="shell-body">
        <Sidebar />
        <main className="content-panel">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
