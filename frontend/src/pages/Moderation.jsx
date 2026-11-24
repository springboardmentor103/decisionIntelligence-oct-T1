import { useState } from 'react'
import { Check, History, X } from 'lucide-react'
import PageHeading from '../components/PageHeading'
import { useFeatures } from '../context/FeatureContext'
import { isDemoMode } from '../services/api'

const formatDate = (value) => new Date(value).toLocaleString(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short',
})

export default function Moderation() {
  const { moderationItems, resolveModerationItem } = useFeatures()
  const [filter, setFilter] = useState('Pending')
  const [error, setError] = useState('')
  const visibleItems = moderationItems.filter((item) => filter === 'All' || item.status === filter)

  const decide = (itemId, status) => {
    try {
      resolveModerationItem(itemId, status)
      setError('')
    } catch (actionError) {
      setError(actionError.message)
    }
  }

  return (
    <div className="page-block feature-page">
      <PageHeading title="Moderation" description="Review reports and record moderation decisions." />
      {isDemoMode ? <p className="feature-demo-note">Demo moderation workspace: actions are stored in this browser and are not shared with a server.</p> : null}
      {error ? <p className="form-alert" role="alert">{error}</p> : null}
      <div className="sort-tabs feature-filter-tabs">
        {['Pending', 'Approved', 'Rejected', 'All'].map((status) => (
          <button type="button" className={`filter-pill ${filter === status ? 'active' : ''}`} key={status} onClick={() => setFilter(status)}>{status}</button>
        ))}
      </div>
      <div className="feature-list">
        {visibleItems.map((item) => (
          <article className="panel-card feature-card" key={item.id}>
            <div className="feature-card-meta">
              <div><strong>{item.title}</strong><small>{item.contentType} · Reported {formatDate(item.createdAt)}</small></div>
              <span className={`feature-status status-${item.status.toLowerCase()}`}>{item.status}</span>
            </div>
            <p className="feature-copy">{item.content}</p>
            <div className="moderation-details"><span><strong>Reason</strong>{item.reason}</span><span><strong>Reported by</strong>{item.reportedBy}</span></div>
            {item.status === 'Pending' ? (
              <div className="feature-actions">
                <button type="button" className="button button--primary button--small" onClick={() => decide(item.id, 'Approved')}><Check size={14} /> Approve</button>
                <button type="button" className="button button--danger-outline button--small" onClick={() => decide(item.id, 'Rejected')}><X size={14} /> Reject</button>
              </div>
            ) : null}
            <details className="moderation-history">
              <summary><History size={14} /> History ({item.history.length})</summary>
              {item.history.length ? <ul>{item.history.map((entry, index) => <li key={`${entry.at}-${index}`}><strong>{entry.status}</strong> by {entry.moderator} · {formatDate(entry.at)}</li>)}</ul> : <p>No action has been recorded.</p>}
            </details>
          </article>
        ))}
        {!visibleItems.length ? <div className="panel-card empty-state"><p>No reports in this queue.</p></div> : null}
      </div>
    </div>
  )
}
