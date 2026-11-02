import { Eye, Pencil, Plus, Search } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { StatusBadge } from '../components/DecisionCard'
import { useDecisions } from '../context/DecisionContext'

const tabs = ['All', 'Draft', 'Active', 'Completed']

export default function MyDecisions() {
  const { decisions } = useDecisions()
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState('All')
  const navigate = useNavigate()

  const myDecisions = useMemo(
    () => decisions.filter((decision) => decision.isMine || decision.creator === 'You'),
    [decisions],
  )
  const filtered = myDecisions.filter((decision) =>
    (status === 'All' || decision.status === status) &&
    decision.title.toLowerCase().includes(query.toLowerCase()),
  )

  return (
    <div className="page-block">
      <PageHeading
        title="My Decisions"
        description="Everything you have created, in one place."
        actions={<Link to="/create-decision" className="button button--primary"><Plus size={16} /> Create Decision</Link>}
      />
      <div className="list-toolbar">
        <label className="search-field">
          <Search size={16} />
          <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search my decisions" />
        </label>
        <div className="sort-tabs" aria-label="Filter by status">
          {tabs.map((tab) => (
            <button type="button" key={tab} onClick={() => setStatus(tab)} className={`filter-pill ${status === tab ? 'active' : ''}`}>{tab}</button>
          ))}
        </div>
      </div>
      <div className="my-decision-list">
        {filtered.map((decision) => (
          <article className="my-decision-row" key={decision.id}>
            <div className="my-decision-main">
              <h3>{decision.title} <StatusBadge status={decision.status} /></h3>
              <p>{decision.category}<span>·</span><span>{new Date(`${decision.deadline}T00:00:00`).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</span><span>·</span>{decision.options.length} options</p>
            </div>
            <div className="row-actions">
              <button type="button" className="button button--outline button--small" onClick={() => navigate(`/decision/${decision.id}`)}><Eye size={14} /> View Details</button>
              <button type="button" className="button button--outline button--small" onClick={() => navigate('/create-decision', { state: { decision } })}><Pencil size={14} /> Edit</button>
              <button type="button" className="button button--dark button--small" disabled={decision.status === 'Draft'} onClick={() => navigate(`/compare-options?decision=${decision.id}`)}>View Results</button>
            </div>
          </article>
        ))}
        {filtered.length === 0 ? (
          <div className="empty-state panel-card">
            <h2>No decisions in this view</h2>
            <p>Create a decision to start organizing your options.</p>
            <Link to="/create-decision" className="button button--primary">Create Decision</Link>
          </div>
        ) : null}
      </div>
    </div>
  )
}
