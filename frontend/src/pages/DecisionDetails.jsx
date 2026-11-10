import { ArrowLeft, CalendarDays, UsersRound } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { StatusBadge } from '../components/DecisionCard'
import { useDecisions } from '../context/DecisionContext'

export default function DecisionDetails() {
  const { decisionId } = useParams()
  const { decisions, votes } = useDecisions()
  const decision = decisions.find((item) => item.id === decisionId)
  const selectedVote = votes[decisionId]

  if (!decision) {
    return <div className="page-block"><div className="panel-card empty-state"><h2>Decision not found</h2><Link to="/explore" className="button button--primary">Explore decisions</Link></div></div>
  }

  return (
    <div className="page-block">
      <Link to="/explore" className="back-link"><ArrowLeft size={15} /> Back to decisions</Link>
      <PageHeading
        title={decision.title}
        description={decision.description}
        actions={<StatusBadge status={decision.status} />}
      />
      <div className="detail-meta"><span>{decision.category}</span><span><CalendarDays size={14} /> Deadline {new Date(`${decision.deadline}T00:00:00`).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</span><span><UsersRound size={14} /> {decision.participants} participants</span><span>{decision.visibility}</span></div>
      <section className="panel-card decision-options-panel">
        <div className="panel-heading"><h2>Options</h2><Link to={`/compare-options?decision=${decision.id}`} className="text-link">Compare options</Link></div>
        {decision.options.map((option) => (
          <article className={`detail-option ${selectedVote === option.title ? 'detail-option--selected' : ''}`} key={option.title}>
            <div><h3>{option.title}</h3><p>{option.description}</p></div>
            {selectedVote === option.title ? <span className="form-success">Your vote</span> : null}
          </article>
        ))}
        {decision.status === 'Active' && !selectedVote ? <Link to={`/vote/${decision.id}`} className="button button--primary vote-link">Cast your vote</Link> : null}
        {selectedVote ? <p className="form-success" role="status">Your vote was recorded for this session.</p> : null}
        <p className="vote-disclaimer">Votes are stored only in this browser session. Backend integration is needed for shared, permanent voting.</p>
      </section>
      <section className="panel-card decision-about-panel"><h2>Evaluation criteria</h2><div className="criteria-chips">{decision.criteria.map((criterion) => <span key={criterion.name}>{criterion.name}<strong>{criterion.weight}%</strong></span>)}</div></section>
      <section className="panel-card decision-about-panel">
        <h2>Pros and cons</h2>
        <div className="pros-cons-grid">
          {decision.options.map((option) => (
            <article className="pros-cons-card" key={option.title}>
              <h3>{option.title}</h3>
              <div><strong className="strength">Pros</strong>
                {option.pros?.length ? <ul>{option.pros.map((pro) => <li key={pro}>{pro}</li>)}</ul> : <p className="muted-copy">No pros listed.</p>}
              </div>
              <div><strong className="weakness">Cons</strong>
                {option.cons?.length ? <ul>{option.cons.map((con) => <li key={con}>{con}</li>)}</ul> : <p className="muted-copy">No cons listed.</p>}
              </div>
            </article>
          ))}
        </div>
      </section>
    </div>
  )
}
