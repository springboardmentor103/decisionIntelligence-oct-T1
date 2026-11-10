import { Activity, ArrowRight, CheckCircle2, ClipboardList, MessageCircle, Vote } from 'lucide-react'
import { Link } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { StatusBadge } from '../components/DecisionCard'
import { useDecisions } from '../context/DecisionContext'

const getDateKey = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const formatRelativeTime = (timestamp) => {
  const minutes = Math.floor((Date.now() - new Date(timestamp).getTime()) / 60000)
  if (minutes < 1) return 'Just now'
  if (minutes < 60) return `${minutes}m ago`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}h ago`
  return `${Math.floor(hours / 24)}d ago`
}

export default function Dashboard() {
  const { decisions, votes, activityEvents } = useDecisions()
  const myDecisions = decisions.filter((decision) =>
    !decision.isSample && (decision.isMine || decision.creator === 'You'),
  )
  const active = myDecisions.filter((decision) => decision.status === 'Active')
  const completed = myDecisions.filter((decision) => decision.status === 'Completed')
  const recentEvents = [...activityEvents]
    .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
    .slice(0, 4)
  const today = new Date()
  const dailyActivity = Array.from({ length: 7 }, (_, index) => {
    const date = new Date(today)
    date.setDate(today.getDate() - (6 - index))
    const key = getDateKey(date)
    const events = activityEvents.filter((event) => getDateKey(new Date(event.timestamp)) === key)
    return {
      key,
      label: date.toLocaleDateString(undefined, { weekday: 'short' }),
      decisions: events.filter((event) => event.type === 'decision').length,
      votes: events.filter((event) => event.type === 'vote').length,
    }
  })
  const maxActivity = Math.max(0, ...dailyActivity.flatMap((day) => [day.decisions, day.votes]))
  const stats = [
    { label: 'Total Decisions', value: myDecisions.length, caption: 'Your decisions', icon: ClipboardList },
    { label: 'Active Decisions', value: active.length, caption: 'Currently open', icon: Activity },
    { label: 'Completed Decisions', value: completed.length, caption: 'Closed decisions', icon: CheckCircle2 },
    { label: 'Votes Cast', value: Object.keys(votes).length.toLocaleString(), caption: 'Your participation', icon: Vote },
  ]

  return (
    <div className="page-block dashboard-page">
      <PageHeading
        title="Dashboard"
        description="Track your decisions and recent activity."
        actions={
          <>
            <Link to="/explore" className="button button--secondary">Explore Decisions</Link>
            <Link to="/create-decision" className="button button--primary">＋ Create New Decision</Link>
          </>
        }
      />

      <div className="stats-grid dashboard-stats">
        {stats.map(({ label, value, caption, icon: Icon }) => (
          <article className="stat-card" key={label}>
            <div className="stat-card-top">
              <p>{label}</p>
              <span className="stat-icon"><Icon size={16} /></span>
            </div>
            <h2>{value}</h2>
            <span className="stat-caption">⌁ {caption}</span>
          </article>
        ))}
      </div>

      <div className="dashboard-middle">
        <section className="panel-card activity-chart-card">
          <div className="panel-heading">
            <div>
              <h2>Decision Activity</h2>
              <p>Your decisions and votes over the last 7 days</p>
            </div>
          </div>
          <div className="activity-chart" aria-label="Decision and voting activity for the last seven days">
            <div className="chart-grid-lines" aria-hidden="true">
              <span /><span /><span /><span />
            </div>
            <div className="chart-columns" aria-hidden="true">
              {dailyActivity.map(({ key, label, decisions: decisionCount, votes: voteCount }) => (
                <div className="chart-day" key={key}>
                  <div className="chart-day-bars">
                    <span
                      className="chart-column"
                      title={`${decisionCount} decisions`}
                      style={{ height: `${maxActivity ? decisionCount / maxActivity * 100 : 0}%` }}
                    />
                    <span
                      className="chart-column chart-column--soft"
                      title={`${voteCount} votes`}
                      style={{ height: `${maxActivity ? voteCount / maxActivity * 100 : 0}%` }}
                    />
                  </div>
                  <span className="chart-day-label">{label}</span>
                </div>
              ))}
            </div>
            <div className="chart-legend" aria-hidden="true">
              <span><i />Decisions</span><span><i className="chart-legend-comments" />Votes</span>
            </div>
            {!activityEvents.length ? <p className="chart-empty-state">Your decisions and votes will appear here.</p> : null}
          </div>
        </section>

        <section className="panel-card recent-activity-card">
          <div className="panel-heading">
            <h2>Recent Activity</h2>
            <Link to="/notifications" className="text-link">View all <ArrowRight size={13} /></Link>
          </div>
          <div className="recent-activity-list">
            {recentEvents.map((event) => (
              <div className="recent-activity-item" key={`${event.type}-${event.timestamp}-${event.title}`}>
                <span className={`activity-avatar ${event.type === 'vote' ? 'avatar-blue' : 'avatar-teal'}`}>
                  {event.type === 'vote' ? 'V' : 'D'}
                </span>
                <div>
                  <p><strong>{event.type === 'vote' ? 'You voted on' : 'You created'}</strong> <b>{event.title}</b></p>
                  <small>{formatRelativeTime(event.timestamp)}</small>
                </div>
              </div>
            ))}
            {!recentEvents.length ? <p className="dashboard-empty-activity">No activity yet. Create a decision or cast a vote to get started.</p> : null}
          </div>
        </section>
      </div>

      <section className="panel-card table-panel">
        <div className="panel-heading">
          <h2>Recent Decisions</h2>
          <Link to="/my-decisions" className="text-link">View all <ArrowRight size={13} /></Link>
        </div>
        <div className="table-scroll">
          <table className="data-table">
            <thead>
              <tr><th>Decision</th><th>Category</th><th>Deadline</th><th>Status</th></tr>
            </thead>
            <tbody>
              {myDecisions.slice(0, 5).map((decision) => (
                <tr key={decision.id}>
                  <td><Link to={`/decision/${decision.id}`} className="table-title">{decision.title}</Link></td>
                  <td>{decision.category}</td>
                  <td>{new Date(`${decision.deadline}T00:00:00`).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</td>
                  <td><StatusBadge status={decision.status} /></td>
                </tr>
              ))}
              {!myDecisions.length ? <tr><td colSpan="4">No decisions created yet.</td></tr> : null}
            </tbody>
          </table>
        </div>
      </section>
      <p className="dashboard-footnote"><MessageCircle size={13} /> Dashboard data is saved in this browser and is not shared live between users yet.</p>
    </div>
  )
}
