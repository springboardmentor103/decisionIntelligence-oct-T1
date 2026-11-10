import { ArrowLeft, CheckCircle2, Vote } from 'lucide-react'
import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { useDecisions } from '../context/DecisionContext'

export default function PollResults() {
  const { pollId } = useParams()
  const { polls, votes, castPollVote } = useDecisions()
  const poll = polls.find((item) => item.id === pollId)
  const [selected, setSelected] = useState('')
  const [confirming, setConfirming] = useState(false)
  const [error, setError] = useState('')

  if (!poll) return <div className="page-block"><div className="panel-card empty-state"><h2>Poll not found</h2><Link to="/polls" className="button button--primary">Browse polls</Link></div></div>

  const totalVotes = Object.values(poll.votes).reduce((sum, count) => sum + count, 0)
  const existingVote = votes[`poll:${poll.id}`]
  const isOpen = poll.closesAt >= new Date().toISOString().slice(0, 10)
  const submitVote = () => {
    if (!confirming) {
      if (!selected) {
        setError('Choose an option before continuing.')
        return
      }
      setConfirming(true)
      setError('')
      return
    }
    if (!castPollVote(poll.id, selected)) {
      setError('A vote has already been recorded for this poll.')
      return
    }
    setConfirming(false)
  }

  return (
    <div className="page-block">
      <Link to="/polls" className="back-link"><ArrowLeft size={15} /> Back to polls</Link>
      <PageHeading title={poll.title} description={poll.description} actions={<span className={`status-badge ${isOpen ? 'status-active' : 'status-completed'}`}>{isOpen ? 'Open' : 'Closed'}</span>} />
      <section className="panel-card poll-results-panel">
        <div className="panel-heading"><div><h2>Poll results</h2><p>{totalVotes} total votes · Closes {new Date(`${poll.closesAt}T00:00:00`).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</p></div></div>
        <div className="poll-chart" role="img" aria-label={`Poll results from ${totalVotes} votes`}>
          {poll.options.map((option) => {
            const count = poll.votes[option] || 0
            const percentage = totalVotes ? Math.round(count / totalVotes * 100) : 0
            return <div className="poll-result-row" key={option}>
              <div className="poll-result-label"><span>{option}</span><strong>{percentage}% <small>({count})</small></strong></div>
              <div className="poll-result-track"><span style={{ width: `${percentage}%` }} /></div>
            </div>
          })}
        </div>
      </section>
      {existingVote ? (
        <section className="panel-card poll-vote-status"><CheckCircle2 size={18} /><p>Your vote for <strong>{existingVote}</strong> has been recorded.</p></section>
      ) : isOpen ? (
        <section className="panel-card poll-vote-panel">
          <h2><Vote size={16} /> Cast your vote</h2>
          <div className="vote-options">
            {poll.options.map((option) => <label className={`vote-option ${selected === option ? 'selected' : ''}`} key={option}>
              <input type="radio" name="poll-vote" checked={selected === option} onChange={() => { setSelected(option); setConfirming(false); setError('') }} />
              <span><strong>{option}</strong></span>
            </label>)}
          </div>
          {confirming ? <div className="vote-confirmation" role="status"><strong>Confirm your vote for “{selected}”?</strong><span>Your response can only be submitted once.</span></div> : null}
          {error ? <p className="form-alert" role="alert">{error}</p> : null}
          <div className="form-footer-actions">{confirming ? <button type="button" className="button button--outline" onClick={() => setConfirming(false)}>Change selection</button> : null}<button type="button" className="button button--primary" onClick={submitVote}>{confirming ? 'Confirm Vote' : 'Continue'}</button></div>
        </section>
      ) : <p className="muted-copy">This poll is closed and no longer accepting votes.</p>}
    </div>
  )
}
