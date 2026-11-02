import { CalendarDays, UsersRound } from 'lucide-react'
import { Link } from 'react-router-dom'

export function StatusBadge({ status }) {
  return <span className={`status-badge status-${status.toLowerCase()}`}>{status}</span>
}

export function DecisionCard({ decision }) {
  return (
    <article className="decision-card">
      <div className="decision-card-topline">
        <span className="category-label">{decision.category}</span>
        <StatusBadge status={decision.status} />
      </div>
      <h3>{decision.title}</h3>
      <p className="decision-card-description">{decision.description}</p>
      <div className="decision-card-author">
        <span className="mini-avatar">{decision.creator.split(/\s+/).map((part) => part[0]).slice(0, 2).join('')}</span>
        <span>{decision.creator}</span>
      </div>
      <div className="decision-card-footer">
        <span><CalendarDays size={13} /> {new Date(`${decision.deadline}T00:00:00`).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</span>
        <span><UsersRound size={13} /> {decision.participants} participants</span>
        <Link className="button button--primary button--small" to={`/decision/${decision.id}`}>View Decision</Link>
      </div>
    </article>
  )
}

export function FilterPill({ children, active, onClick }) {
  return (
    <button type="button" className={`filter-pill ${active ? 'active' : ''}`} onClick={onClick}>
      {children}
    </button>
  )
}
