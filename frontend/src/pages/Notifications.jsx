import { Bell, CalendarClock, MessageCircle, Megaphone, Star, TriangleAlert, Vote } from 'lucide-react'
import { Link } from 'react-router-dom'
import { useState } from 'react'
import PageHeading from '../components/PageHeading'
import { useFeatures } from '../context/FeatureContext'

const typeIcons = {
  vote: Vote,
  reply: MessageCircle,
  feedback: Star,
  report: TriangleAlert,
  deadline: CalendarClock,
  results: Megaphone,
}

export default function Notifications() {
  const { notifications, markNotificationRead, markAllNotificationsRead } = useFeatures()
  const [filter, setFilter] = useState('All')
  const shown = notifications.filter((notification) => filter === 'All' || notification.unread)

  return (
    <div className="page-block feature-page">
      <PageHeading
        title="Notifications"
        description="Updates about discussions, feedback, polls, and reports."
        actions={<button type="button" className="button button--outline" onClick={markAllNotificationsRead}>Mark all as read</button>}
      />
      <p className="feature-demo-note">Demo workspace: notifications are stored in this browser.</p>
      <div className="sort-tabs notification-tabs">
        {['All', 'Unread'].map((item) => <button type="button" key={item} className={`filter-pill ${filter === item ? 'active' : ''}`} onClick={() => setFilter(item)}>{item}</button>)}
      </div>
      <div className="notification-list">
        {shown.map((notification) => {
          const Icon = typeIcons[notification.type] || Bell
          return (
            <Link
              to={notification.href || '/notifications'}
              key={notification.id}
              className={`notification-item ${notification.unread ? 'unread' : ''}`}
              onClick={() => markNotificationRead(notification.id)}
            >
              <span className="notification-type-icon"><Icon size={16} /></span>
              <span className="notification-message"><span>{notification.text}</span><small>{notification.time}</small></span>
              {notification.unread ? <span className="unread-dot" aria-label="Unread" /> : <span className="notification-read-label">Read</span>}
            </Link>
          )
        })}
        {shown.length === 0 ? <div className="panel-card empty-state"><p>You’re all caught up.</p></div> : null}
      </div>
    </div>
  )
}
