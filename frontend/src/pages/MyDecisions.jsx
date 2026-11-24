import { Eye, Pencil, Plus, Search } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { StatusBadge } from '../components/DecisionCard'
import { useDecisions } from '../context/DecisionContext'
import {
  getMyDecisions,
  getDecisionOptions,
  deleteDecision,
} from '../services/decisionService'

const tabs = ['All', 'Draft', 'Active', 'Completed']

export default function MyDecisions() {
  const [decisions, setDecisions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState('All')
  const { removeDecision } = useDecisions()

  const navigate = useNavigate()
  const handleDelete = async (decisionId) => {
  const confirmed = window.confirm(
    'Are you sure you want to delete this decision?'
  )

  if (!confirmed) return

  try {
    await deleteDecision(decisionId)
    removeDecision(decisionId)

    setDecisions((previous) =>
  previous.filter(
    (decision) => String(decision.id) !== String(decisionId)
  )
)

removeDecision(decisionId)
    

    alert('Decision deleted successfully.')
  } catch (err) {
    console.error('Failed to delete decision:', err)
    alert(
      err.response?.data?.message ||
      'Unable to delete decision. Please try again.'
    )
  }
}

  useEffect(() => {
    const loadMyDecisions = async () => {
      try {
        setLoading(true)
        setError('')

        const myDecisions = await getMyDecisions()
        console.log(
  'Criteria received from API:',
  myDecisions.map((decision) => ({
    id: decision.id,
    title: decision.title,
    criteria: decision.criteria,
  }))
)

        const decisionsWithOptions = await Promise.all(
          (myDecisions || []).map(async (decision) => {
            const options = await getDecisionOptions(decision.id)

            return {
              ...decision,
              options: options || [],
              status:
  decision.status === 'COMPLETED' ||
  (decision.deadline && new Date(decision.deadline) < new Date())
    ? 'Completed'
    : decision.status === 'ACTIVE'
      ? 'Active'
      : decision.status === 'DRAFT'
        ? 'Draft'
        : decision.status,
            }
          })
        )

        setDecisions(decisionsWithOptions)
      } catch (err) {
        console.error('Failed to load my decisions:', err)
        setError(
          err.response?.data?.message ||
          'Unable to load your decisions. Please try again.'
        )
      } finally {
        setLoading(false)
      }
    }

    loadMyDecisions()
  }, [])

  const filtered = useMemo(
  () =>
    decisions.filter((decision) =>
      (status === 'All' || decision.status === status) &&
      decision.title?.toLowerCase().includes(query.toLowerCase())
    ),
  [decisions, status, query]
)
  
    

  return (
    <div className="page-block">
      <PageHeading
        title="My Decisions"
        description="Everything you have created, in one place."
        actions={
          <Link to="/create-decision" className="button button--primary">
            <Plus size={16} /> Create Decision
          </Link>
        }
      />

      <div className="list-toolbar">
        <label className="search-field">
          <Search size={16} />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Search my decisions"
          />
        </label>

        <div className="sort-tabs" aria-label="Filter by status">
          {tabs.map((tab) => (
            <button
              type="button"
              key={tab}
              onClick={() => setStatus(tab)}
              className={`filter-pill ${status === tab ? 'active' : ''}`}
            >
              {tab}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <div className="panel-card empty-state">
          <p>Loading your decisions...</p>
        </div>
      ) : error ? (
        <div className="panel-card empty-state">
          <p className="form-alert">{error}</p>
        </div>
      ) : (
        <div className="my-decision-list">
          {filtered.map((decision) => (
            <article className="my-decision-row" key={decision.id}>
              <div className="my-decision-main">
                <h3>
                  {decision.title}
                  <StatusBadge status={decision.status} />
                </h3>

                <p>
                  {decision.category}
                  <span> · </span>
                  <span>
                    {decision.deadline
                      ? new Date(decision.deadline).toLocaleDateString(
                          'en-US',
                          {
                            month: 'short',
                            day: 'numeric',
                            year: 'numeric',
                          }
                        )
                      : 'No deadline'}
                  </span>
                  <span> · </span>
                  {decision.options.length} options
                </p>
              </div>

              <div className="row-actions">
                <button
                  type="button"
                  className="button button--outline button--small"
                  onClick={() =>
  navigate(`/decision/${decision.id}`, {
    state: { from: '/my-decisions' },
  })
}
                >
                  <Eye size={14} /> View Details
                </button>

                <button
                  type="button"
                  className="button button--outline button--small"
                  onClick={() =>
                    navigate('/create-decision', {
                      state: { decision },
                    })
                  }
                >
                  <Pencil size={14} /> Edit
                </button>
                <button
  type="button"
  className="button button--outline button--small"
  onClick={() => handleDelete(decision.id)}
>
  Delete
</button>

                <button
                  type="button"
                  className="button button--dark button--small"
                  disabled={decision.status === 'Draft'}
                  onClick={() =>
                    navigate(
                      `/compare-options?decision=${decision.id}`
                    )
                  }
                >
                  View Results
                </button>
              </div>
            </article>
          ))}

          {filtered.length === 0 && (
            <div className="empty-state panel-card">
              <h2>No decisions in this view</h2>
              <p>Create a decision to start organizing your options.</p>
              <Link
                to="/create-decision"
                className="button button--primary"
              >
                Create Decision
              </Link>
            </div>
          )}
        </div>
      )}
    </div>
  )
}