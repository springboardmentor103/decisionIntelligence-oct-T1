import { Bell, ChevronDown, Menu, Search } from 'lucide-react'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useDecisions } from '../context/DecisionContext'

export default function Navbar({ onMenuClick }) {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const { notifications } = useDecisions()
  const [query, setQuery] = useState('')
  const [profileMenuOpen, setProfileMenuOpen] = useState(false)
  const unreadCount = notifications.filter((notification) => notification.unread).length
  const displayName = user?.name || 'Sadhika Banu'
  const initials = displayName
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase()

  const handleSearch = (event) => {
    event.preventDefault()
    navigate(`/explore?q=${encodeURIComponent(query)}`)
  }

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <header className="topbar">
      <button className="mobile-menu" type="button" aria-label="Toggle navigation" onClick={onMenuClick}>
        <Menu size={21} />
      </button>
      <Link to="/dashboard" className="topbar-brand">
        <span className="brand-mark">D<span>!</span></span>
        <span className="topbar-brand-name">Decision<span className="brand-accent">Hub</span></span>
      </Link>
      <form className="global-search" onSubmit={handleSearch} role="search">
        <Search size={16} />
        <input
          aria-label="Search decisions, options, people"
          placeholder="Search decisions, options, people"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
        />
      </form>
      <div className="topbar-actions">
        <button
          type="button"
          className="icon-button notification-trigger"
          aria-label={`Notifications${unreadCount ? `, ${unreadCount} unread` : ''}`}
          onClick={() => navigate('/notifications')}
        >
          <Bell size={19} />
          {unreadCount > 0 ? <span className="notification-dot" /> : null}
        </button>
        <div className="profile-menu-wrap">
          <button
            type="button"
            className="profile-menu-trigger"
            aria-expanded={profileMenuOpen}
            onClick={() => setProfileMenuOpen((open) => !open)}
          >
            <span className="profile-menu-avatar">{initials}</span>
            <span className="profile-menu-name">{displayName}</span>
            <ChevronDown size={14} />
          </button>
          {profileMenuOpen ? (
            <div className="profile-menu">
              <Link to="/profile" onClick={() => setProfileMenuOpen(false)}>My profile</Link>
              <Link to="/settings" onClick={() => setProfileMenuOpen(false)}>Settings</Link>
              <button type="button" onClick={handleLogout}>Log out</button>
            </div>
          ) : null}
        </div>
      </div>
    </header>
  )
}
