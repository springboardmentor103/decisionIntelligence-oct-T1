import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { logoutUser } from '../services/authService'

export default function Navbar() {
  const navigate = useNavigate()
  const { user, logout } = useAuth()

  const handleLogout = async () => {
    logout()
    let notice = ''
    try {
      await logoutUser()
    } catch {
      notice = 'Signed out here, but the server could not clear its sign-in cookie.'
    }
    navigate('/login', { replace: true, state: { notice } })
  }

  return (
    <header className="topbar">
      <div className="brand-wrap">
        <Link to="/dashboard" className="brand-name">
          DecisionHub
        </Link>
      </div>

      <div className="topbar-user">
        <div className="user-tag">
          <span className="user-avatar">{user?.name?.charAt(0).toUpperCase() || 'D'}</span>
          <div>
            <strong>{user?.name || 'DecisionHub User'}</strong>
            <small>{user?.role || 'user'}</small>
          </div>
        </div>
        <button type="button" className="logout-button" onClick={handleLogout}>
          Log out
        </button>
      </div>
    </header>
  )
}
