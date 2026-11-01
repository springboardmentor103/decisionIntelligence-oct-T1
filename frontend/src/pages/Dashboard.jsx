const stats = [
  { label: 'Active polls', value: '28', trend: '+12% from last week' },
  { label: 'Votes today', value: '1,482', trend: '+8% engagement' },
  { label: 'Community score', value: '92%', trend: 'Strong sentiment' },
]

const activities = [
  'New policy feedback round opens for the city transit proposal.',
  'Three moderator reviews are awaiting decision approval.',
  'Your community brief is ready for export and sharing.',
]

export default function Dashboard() {
  return (
    <div className="page-block">
      <div className="section-header">
        <div>
          <p className="eyebrow">Sample overview</p>
          <h1>Welcome back to DecisionHub</h1>
        </div>
        <button type="button" className="button button--secondary">
          New proposal
        </button>
      </div>

      <div className="stats-grid">
        {stats.map((stat) => (
          <article className="stat-card" key={stat.label}>
            <p>{stat.label}</p>
            <h2>{stat.value}</h2>
            <span>{stat.trend}</span>
          </article>
        ))}
      </div>

      <div className="content-grid two-column">
        <section className="panel-card">
          <div className="panel-header">
            <h3>Latest activity</h3>
            <span className="badge badge-soft">Live</span>
          </div>
          <ul className="activity-list">
            {activities.map((activity) => (
              <li key={activity}>{activity}</li>
            ))}
          </ul>
        </section>

        <section className="panel-card">
          <div className="panel-header">
            <h3>Next steps</h3>
            <span className="badge">Ready</span>
          </div>
          <div className="next-steps">
            <div>
              <strong>1.</strong>
              <span>Review community discussion summaries.</span>
            </div>
            <div>
              <strong>2.</strong>
              <span>Finalize moderator annotation checklist.</span>
            </div>
            <div>
              <strong>3.</strong>
              <span>Prepare the weekly stakeholder digest.</span>
            </div>
          </div>
        </section>
      </div>
    </div>
  )
}
