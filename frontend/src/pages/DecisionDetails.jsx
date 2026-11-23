import { ArrowLeft, CalendarDays, UsersRound } from 'lucide-react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { useEffect, useState } from 'react'
import PageHeading from '../components/PageHeading'
import { StatusBadge } from '../components/DecisionCard'
import { useDecisions } from '../context/DecisionContext'
import { getDecisionCriteria } from '../services/decisionService'
export default function DecisionDetails() {
  const location = useLocation()
  const { decisionId } = useParams()

  const backPath =
    location.state?.fromState?.from ||
    location.state?.from ||
    '/explore'

  const { decisions, votes } = useDecisions()

  const decision = decisions.find(
    (item) => String(item.id) === String(decisionId)
  )

  const [criteria, setCriteria] = useState(decision?.criteria || [])

  const selectedVote = votes[decision?.id]

  useEffect(() => {
    setCriteria(decision?.criteria || [])

    if (!decisionId) return

    getDecisionCriteria(decisionId)
      .then((data) => {
        setCriteria(data || [])
      })
      .catch((error) => {
        console.error('Failed to load evaluation criteria:', error)
      })
  }, [decisionId, decision?.id])

  if (!decision) {
    return <div className="page-block"><div className="panel-card empty-state"><h2>Decision not found</h2><Link to="/explore" className="button button--primary">Explore decisions</Link></div></div>
  }

  return (
    <div className="page-block">
      <Link to={backPath} className="back-link">
  <ArrowLeft size={15} /> Back to decisions
</Link>

      <PageHeading
        title={decision.title}
        description={decision.description}
        actions={<StatusBadge status={decision.status} />}
      />
      <div className="detail-meta"><span>{decision.category}</span><span><CalendarDays size={14} /> Deadline {new Date(`${decision.deadline}T00:00:00`).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</span><span><UsersRound size={14} /> {decision.participants} participants</span><span>{decision.visibility}</span></div>
      <section className="panel-card decision-about-panel">
  
        <div className="panel-heading"><h2>Options</h2><Link to={`/compare-options?decision=${decision.id}`} className="text-link">Compare options</Link></div>
        {decision.options.map((option) => (
          <article className={`detail-option ${selectedVote === option.title ? 'detail-option--selected' : ''}`} key={option.title}>
            <div><h3>{option.title}</h3><p>{option.description}</p></div>
            {selectedVote === option.title ? <span className="form-success">Your vote</span> : null}
          </article>
        ))}
        {decision.status === 'Active' && !selectedVote ? (
      
  <Link
  to={`/vote/${decision.id}`}
  state={{
    fromState: location.state,
  }}
  className="button button--primary vote-link"
>
  Cast your vote
</Link>
) : null}
        
        {selectedVote ? <p className="form-success" role="status">Your vote was recorded for this session.</p> : null}
        
      </section>
      <section className="panel-card decision-about-panel">
  <h2>Evaluation criteria</h2>

  <div className="criteria-chips">
    {criteria.length > 0 ? (
      criteria.map((criterion) => (
        <span key={criterion.id || criterion.name}>
          {criterion.name}
          <strong>{criterion.weight}%</strong>
        </span>
      ))
    ) : (
      <p className="muted-copy">No evaluation criteria added.</p>
    )}
  </div>
</section>
    </div>
  )
}
