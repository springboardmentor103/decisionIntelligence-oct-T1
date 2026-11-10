import {
  Bell,
  CircleHelp,
  GitCompareArrows,
  LayoutDashboard,
  LogOut,
  PlusCircle,
  MessageCircle,
  Settings,
  UsersRound,
  UserRound,
  Vote,
  Waypoints,
} from 'lucide-react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useDecisions } from '../context/DecisionContext'

const links = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/explore', label: 'Explore Decisions', icon: CircleHelp },
  { to: '/my-decisions', label: 'My Decisions', icon: Vote },
  { to: '/create-decision', label: 'Create Decision', icon: PlusCircle },
  { to: '/compare-options', label: 'Compare Options', icon: GitCompareArrows },
  { to: '/communities', label: 'Communities', icon: UsersRound },
  { to: '/polls', label: 'Polls', icon: Vote },
  { to: '/community', label: 'Discussions', icon: MessageCircle },
  { to: '/notifications', label: 'Notifications', icon: Bell, hasBadge: true },
  { to: '/about', label: 'About', icon: Waypoints },
  { to: '/profile', label: 'My Profile', icon: UserRound },
  { to: '/settings', label: 'Settings', icon: Settings },
]

export default function Sidebar({ open = false, onNavigate }) {
  const navigate = useNavigate()
  const { logout } = useAuth()
  const { notifications } = useDecisions()
  const unreadCount = notifications.filter((notification) => notification.unread).length

  return (
    <>
      {open ? <button className="sidebar-backdrop" type="button" aria-label="Close navigation" onClick={onNavigate} /> : null}
      <aside className={`sidebar ${open ? 'sidebar--open' : ''}`}>
        <NavLink to="/dashboard" className="sidebar-brand" onClick={onNavigate}>
          <span className="brand-mark">D<span>!</span></span>
          <span>Decision<span className="brand-accent">Hub</span></span>
        </NavLink>

        <nav className="sidebar-nav" aria-label="Main navigation">
          {links.map(({ to, label, icon: Icon, hasBadge }) => (
            <NavLink
              key={to}
              to={to}
              onClick={onNavigate}
              className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
            >
              <Icon size={17} strokeWidth={1.8} />
              <span>{label}</span>
              {hasBadge && unreadCount > 0 ? <span className="nav-badge">{unreadCount}</span> : null}
            </NavLink>
          ))}
        </nav>

        <button
          type="button"
          className="sidebar-logout"
          onClick={() => {
            logout()
            onNavigate?.()
            navigate('/login', { replace: true })
          }}
        >
          <LogOut size={17} />
          <span>Logout</span>
        </button>
      </aside>
    </>
  )
}
