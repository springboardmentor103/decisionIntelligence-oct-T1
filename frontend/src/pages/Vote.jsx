import { ArrowLeft, CheckCircle2 } from 'lucide-react'
import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { useDecisions } from '../context/DecisionContext'

export default function Vote() {
  const { decisionId } = useParams()
  const navigate = useNavigate()
  const { decisions, votes, castVote } = useDecisions()
  const decision = decisions.find((item) => item.id === decisionId)
  const [selected, setSelected] = useState('')
  const [confirming, setConfirming] = useState(false)
  const [error, setError] = useState('')

  if (!decision) {
    return <div className="page-block"><div className="panel-card empty-state"><h2>Decision not found</h2><Link to="/explore" className="button button--primary">Explore decisions</Link></div></div>
  }

  const existingVote = votes[decision.id]
  const submitVote = () => {
    if (!selected) {
      setError('Select an option before continuing.')
      return
    }
    if (confirming) {
      const saved = castVote(decision.id, selected)
      if (saved) navigate(`/decision/${decision.id}`, { state: { voteConfirmed: true } })
      else setError('A vote has already been recorded for this decision.')
      return
    }
    setError('')
    setConfirming(true)
  }

  return (
    <div className="page-block">
      <Link to={`/decision/${decision.id}`} className="back-link"><ArrowLeft size={15} /> Back to decision</Link>
      <PageHeading title="Cast your vote" description={decision.title} />
      {existingVote ? (
        <section className="panel-card vote-confirmed">
          <CheckCircle2 size={26} />
          <h2>Vote already recorded</h2>
          <p>You selected <strong>{existingVote}</strong>. Each participant can vote once.</p>
          <Link className="button button--outline" to={`/decision/${decision.id}`}>Return to decision</Link>
        </section>
      ) : decision.status !== 'Active' ? (
        <section className="panel-card empty-state"><h2>Voting is closed</h2><p>This decision is not accepting votes right now.</p></section>
      ) : (
        <section className="panel-card vote-panel">
          <h2>Choose one option</h2>
          <p className="muted-copy">Your choice can only be submitted once.</p>
          <div className="vote-options">
            {decision.options.map((option) => (
              <label className={`vote-option ${selected === option.title ? 'selected' : ''}`} key={option.title}>
                <input type="radio" name="vote-option" value={option.title} checked={selected === option.title} onChange={() => { setSelected(option.title); setConfirming(false); setError('') }} />
                <span><strong>{option.title}</strong><small>{option.description}</small></span>
              </label>
            ))}
          </div>
          {confirming ? <div className="vote-confirmation" role="status"><strong>Confirm your vote for “{selected}”?</strong><span>You won’t be able to change it after submitting.</span></div> : null}
          {error ? <p className="form-alert" role="alert">{error}</p> : null}
          <div className="form-footer-actions">
            {confirming ? <button type="button" className="button button--outline" onClick={() => setConfirming(false)}>Change selection</button> : null}
            <button type="button" className="button button--primary" onClick={submitVote}>{confirming ? 'Confirm Vote' : 'Continue'}</button>
          </div>
        </section>
      )}
    </div>
  )
}
