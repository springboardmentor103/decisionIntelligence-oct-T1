import { ArrowLeft, CheckCircle2 } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { useDecisions } from '../context/DecisionContext'
import {
  getDecisionOptions,
  getVoteStatus,
  submitVote as submitVoteToBackend,
} from '../services/decisionService'

export default function Vote() {
  const { decisionId } = useParams()
  console.log('Vote page ID:', decisionId)
  const navigate = useNavigate()
  const location = useLocation()
  const { decisions, votes } = useDecisions()
  const decision = decisions.find((item) => String(item.id) === decisionId)
  const [selected, setSelected] = useState('')
  const [confirming, setConfirming] = useState(false)
  const [error, setError] = useState('')
  const [backendOptions, setBackendOptions] = useState([])
const [loading, setLoading] = useState(true)
const [hasVoted, setHasVoted] = useState(false)
useEffect(() => {
  const loadPollData = async () => {
    try {
      setLoading(true)

      const [options, voteStatus] = await Promise.all([
  getDecisionOptions(decisionId),
  getVoteStatus(decisionId),
])

      setBackendOptions(options || [])
      setHasVoted(voteStatus.hasVoted)
    } catch (error) {
      console.error('Failed to load poll data:', error)
      setError('Unable to load voting options. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  if (decisionId) {
    loadPollData()
  }
}, [decisionId])

  if (!decision) {
    return <div className="page-block"><div className="panel-card empty-state"><h2>Decision not found</h2><Link to="/explore" className="button button--primary">Explore decisions</Link></div></div>
  }
  if (loading) {
  return (
    <div className="page-block">
      <div className="panel-card empty-state">
        <p>Loading voting options...</p>
      </div>
    </div>
  )
}

  const existingVote = votes[decision.id]
  const submitVote = async() => {
    if (!selected) {
      setError('Select an option before continuing.')
      return
    }
    if (confirming) {
      try {
  await submitVoteToBackend(decision.id, [
    {
      optionId: Number(selected),
      rating: null,
    },
  ])

  navigate(`/decision/${decision.id}`, {
  state: {
    ...location.state?.fromState,
    voteConfirmed: true,
  },
})
} catch (error) {
  console.error('Vote submission failed:', error)
  setError(
    error.response?.data?.message ||
    'Unable to submit your vote. Please try again.'
  )
}
return
    }
    setError('')
    setConfirming(true)
  }

  return (
    <div className="page-block">
      <Link
  to={`/decision/${decision.id}`}
  state={location.state?.fromState || {}}
  className="back-link"
>
  <ArrowLeft size={15} /> Back to decision
</Link>
      <PageHeading title="Cast your vote" description={decision.title} />
      {hasVoted||existingVote ? (
        <section className="panel-card vote-confirmed">
          <CheckCircle2 size={26} />
          <h2>Vote already recorded</h2>
          <p>Your vote has already been recorded. Each participant can vote once.</p>
          <Link className="button button--outline" to={`/decision/${decision.id}`}>Return to decision</Link>
        </section>
      ) : decision.status !== 'Active' ? (
        <section className="panel-card empty-state"><h2>Voting is closed</h2><p>This decision is not accepting votes right now.</p></section>
      ) : (
        <section className="panel-card vote-panel">
          <h2>Choose one option</h2>
          <p className="muted-copy">Your choice can only be submitted once.</p>
          <div className="vote-options">
            {backendOptions.map((option) => (
              <label className={`vote-option ${selected === String(option.id) ? 'selected' : ''}`} key={option.id}>
                <input type="radio" name="vote-option" value={String(option.id)} checked={selected === String(option.id)} onChange={() => { setSelected(String(option.id)); setConfirming(false); setError('') }} />
                <span><strong>{option.title}</strong><small>{option.description}</small></span>
              </label>
            ))}
          </div>
          {confirming ? <div className="vote-confirmation" role="status"><strong> Confirm your vote for “{backendOptions.find((option) => String(option.id) === selected)?.title}”?</strong><span>You won’t be able to change it after submitting.</span></div> : null}
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
