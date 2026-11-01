import { NavLink } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { normalizeRoles } from '../utils/roles'

const baseLinks = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/profile', label: 'Profile' },
]

const roleLinks = {
  moderator: [{ to: '/moderation', label: 'Moderation' }],
  admin: [{ to: '/admin', label: 'Administration' }],
}

export default function Sidebar() {
  const { user } = useAuth()
  const roles = normalizeRoles(user?.roles || user?.role)

  const visibleLinks = [
    ...baseLinks,
    ...(roles.includes('moderator') ? roleLinks.moderator : []),
    ...(roles.includes('admin') ? roleLinks.admin : []),
  ]

  return (
    <aside className="sidebar">
      <nav className="sidebar-nav" aria-label="Sidebar navigation">
        {visibleLinks.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
          >
            {link.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  )
}
