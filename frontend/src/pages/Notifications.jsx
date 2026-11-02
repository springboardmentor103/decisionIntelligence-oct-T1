import { Bell, CalendarClock, MessageCircle, Megaphone, UserRoundPlus, Vote } from 'lucide-react'
import { useState } from 'react'
import PageHeading from '../components/PageHeading'
import { useDecisions } from '../context/DecisionContext'

const typeIcons = { vote: Vote, reply: MessageCircle, invite: UserRoundPlus, deadline: CalendarClock, results: Megaphone }

export default function Notifications() {
  const { notifications, markAllRead } = useDecisions()
  const [filter, setFilter] = useState('All')
  const shown = notifications.filter((notification) => filter === 'All' || notification.unread)

  return (
    <div className="page-block">
      <PageHeading
        title="Notifications"
        actions={<button type="button" className="button button--outline" onClick={markAllRead}>Mark All as Read</button>}
      />
      <div className="sort-tabs notification-tabs">
        {['All', 'Unread'].map((item) => <button type="button" key={item} className={`filter-pill ${filter === item ? 'active' : ''}`} onClick={() => setFilter(item)}>{item}</button>)}
      </div>
      <div className="notification-list">
        {shown.map((notification) => {
          const Icon = typeIcons[notification.type] || Bell
          return (
            <article key={notification.id} className={`notification-item ${notification.unread ? 'unread' : ''}`}>
              <span className="notification-type-icon"><Icon size={16} /></span>
              <div><p>{notification.text}</p><small>{notification.time}</small></div>
              {notification.unread ? <span className="unread-dot" aria-label="Unread" /> : null}
            </article>
          )
        })}
        {shown.length === 0 ? <div className="panel-card empty-state"><p>You’re all caught up.</p></div> : null}
      </div>
    </div>
  )
}
