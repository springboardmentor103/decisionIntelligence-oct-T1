import { Activity, ArrowRight, CheckCircle2, ClipboardList, MessageCircle, Vote } from 'lucide-react'
import { Link } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { StatusBadge } from '../components/DecisionCard'
import { useDecisions } from '../context/DecisionContext'

const recentActivity = [
  { initials: 'PN', color: 'teal', text: <><strong>Priya Nair</strong> commented on <b>Final-Year Project Tech Stack</b></>, time: '2h ago' },
  { initials: 'KS', color: 'mint', text: <><strong>Karan Shah</strong> voted on <b>Where Should We Hold the Annual Tech Fest?</b></>, time: '4h ago' },
  { initials: 'MI', color: 'teal', text: <><strong>Meera Iyer</strong> published results for <b>Monthly Budget Tracking Tool</b></>, time: '1d ago' },
  { initials: 'RD', color: 'blue', text: <><strong>Rohan Das</strong> joined <b>Group Trip Destination for December</b></>, time: '2d ago' },
]

export default function Dashboard() {
  const { decisions } = useDecisions()
  const myDecisions = decisions.filter((decision) => decision.isMine || decision.creator === 'You')
  const active = myDecisions.filter((decision) => decision.status === 'Active')
  const completed = myDecisions.filter((decision) => decision.status === 'Completed')
  const totalVotes = decisions.reduce((total, decision) => total + (decision.votes || 0), 0)
  const stats = [
    { label: 'Total Decisions', value: myDecisions.length, caption: '+2 this month', icon: ClipboardList },
    { label: 'Active Decisions', value: active.length, caption: `${active.length} closing soon`, icon: Activity },
    { label: 'Completed Decisions', value: completed.length, caption: 'Results published', icon: CheckCircle2 },
    { label: 'Community Votes', value: totalVotes.toLocaleString(), caption: 'Across all decisions', icon: Vote },
  ]
  const weekdayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']

  return (
    <div className="page-block">
      <PageHeading
        title="Dashboard"
        description="Here is what is happening with your decisions. Figures shown are sample data for demonstration."
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
            <span className="stat-caption">{label === 'Active Decisions' ? '⌁ ' : '⌁ '}{caption}</span>
          </article>
        ))}
      </div>

      <div className="dashboard-middle">
        <section className="panel-card activity-chart-card">
          <div className="panel-heading">
            <div>
              <h2>Decision Activity</h2>
              <p>Votes and comments over the last 7 days (sample)</p>
            </div>
            <button type="button" className="icon-button chart-menu" aria-label="Activity options">···</button>
          </div>
          <div className="activity-chart" aria-label="Activity chart">
            <div className="chart-weekdays">
              {weekdayLabels.map((day) => <span key={day}>{day}</span>)}
            </div>
          </div>
        </section>

        <section className="panel-card recent-activity-card">
          <div className="panel-heading">
            <h2>Recent Activity</h2>
            <Link to="/notifications" className="text-link">View all <ArrowRight size={13} /></Link>
          </div>
          <div className="recent-activity-list">
            {recentActivity.map((item, index) => (
              <div className="recent-activity-item" key={index}>
                <span className={`activity-avatar avatar-${item.color}`}>{item.initials}</span>
                <div><p>{item.text}</p><small>{item.time}</small></div>
              </div>
            ))}
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
              {decisions.slice(0, 5).map((decision) => (
                <tr key={decision.id}>
                  <td><Link to={`/decision/${decision.id}`} className="table-title">{decision.title}</Link></td>
                  <td>{decision.category}</td>
                  <td>{new Date(`${decision.deadline}T00:00:00`).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</td>
                  <td><StatusBadge status={decision.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
      <p className="dashboard-footnote"><MessageCircle size={13} /> DecisionHub helps teams turn community input into informed choices.</p>
    </div>
  )
}
