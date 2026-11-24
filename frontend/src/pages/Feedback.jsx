import { useMemo, useState } from 'react'
import { Pencil, Star } from 'lucide-react'
import PageHeading from '../components/PageHeading'
import { useDecisions } from '../context/DecisionContext'
import { useFeatures } from '../context/FeatureContext'

const demoDecisionChoices = [
  { id: 'decision-1', title: 'Final-Year Project Tech Stack' },
  { id: 'decision-2', title: 'Where Should We Hold the Annual Tech Fest?' },
  { id: 'decision-5', title: 'Group Trip Destination for December' },
]

const dateLabel = (value) => new Date(value).toLocaleDateString(undefined, {
  year: 'numeric',
  month: 'short',
  day: 'numeric',
})

export default function Feedback() {
  const { decisions } = useDecisions()
  const { feedback, userId, createFeedback, editFeedback } = useFeatures()
  const choices = decisions.length ? decisions : demoDecisionChoices
  const [draft, setDraft] = useState({ decisionId: choices[0]?.id || '', rating: 0, message: '' })
  const [editingId, setEditingId] = useState('')
  const [error, setError] = useState('')
  const [savedNotice, setSavedNotice] = useState('')
  const ownFeedback = useMemo(() => feedback.filter((item) => item.authorId === userId), [feedback, userId])
  const averageRating = ownFeedback.length
    ? (ownFeedback.reduce((sum, item) => sum + item.rating, 0) / ownFeedback.length).toFixed(1)
    : '—'
  const selectedDecisionId = choices.some((item) => String(item.id) === String(draft.decisionId))
    ? draft.decisionId
    : choices[0]?.id || ''

  const resetForm = () => {
    setDraft({ decisionId: choices[0]?.id || '', rating: 0, message: '' })
    setEditingId('')
    setError('')
  }

  const submit = (event) => {
    event.preventDefault()
    const decision = choices.find((item) => String(item.id) === String(selectedDecisionId))
    if (!decision || !Number.isInteger(Number(draft.rating)) || Number(draft.rating) < 1 || Number(draft.rating) > 5 || !draft.message.trim()) {
      setError('Choose a decision, rating, and feedback message.')
      return
    }

    try {
      if (editingId) {
        editFeedback(editingId, {
          decisionId: decision.id,
          decisionTitle: decision.title,
          rating: Number(draft.rating),
          message: draft.message.trim(),
        })
        setSavedNotice('Your feedback was updated.')
      } else {
        createFeedback({
          decisionId: decision.id,
          decisionTitle: decision.title,
          rating: Number(draft.rating),
          message: draft.message.trim(),
        })
        setSavedNotice('Your feedback was submitted.')
      }
      resetForm()
    } catch (submitError) {
      setError(submitError.message)
    }
  }

  const startEditing = (item) => {
    setEditingId(item.id)
    setDraft({ decisionId: item.decisionId, rating: item.rating, message: item.message })
    setSavedNotice('')
    setError('')
  }

  return (
    <div className="page-block feature-page">
      <PageHeading title="Feedback" description="Rate your experience and review feedback you have submitted." />
      <p className="feature-demo-note">Demo workspace: feedback is stored in this browser only.</p>
      <section className="feature-summary-grid">
        <article className="panel-card feature-summary"><span>Your submissions</span><strong>{ownFeedback.length}</strong></article>
        <article className="panel-card feature-summary"><span>Average rating</span><strong>{averageRating}{averageRating !== '—' ? ' / 5' : ''}</strong></article>
      </section>

      <form className="panel-card feature-form" onSubmit={submit}>
        <h2>{editingId ? 'Edit feedback' : 'Share feedback'}</h2>
        <label className="form-control">Related decision
          <select className="form-input" value={selectedDecisionId} onChange={(event) => setDraft((current) => ({ ...current, decisionId: event.target.value }))} required>
            {choices.map((decision) => <option value={decision.id} key={decision.id}>{decision.title}</option>)}
          </select>
        </label>
        <fieldset className="feature-rating">
          <legend>Rating</legend>
          <div role="radiogroup" aria-label="Rating from one to five stars">
            {[1, 2, 3, 4, 5].map((rating) => (
              <button
                type="button"
                key={rating}
                className={`feature-star ${rating <= draft.rating ? 'is-selected' : ''}`}
                role="radio"
                aria-checked={draft.rating === rating}
                aria-label={`${rating} ${rating === 1 ? 'star' : 'stars'}`}
                onClick={() => setDraft((current) => ({ ...current, rating }))}
              ><Star size={23} fill={rating <= draft.rating ? 'currentColor' : 'none'} /></button>
            ))}
          </div>
          <span>{draft.rating ? `${draft.rating} out of 5` : 'Select a rating'}</span>
        </fieldset>
        <label className="form-control">Your feedback
          <textarea className="form-input" rows="4" maxLength="2000" value={draft.message} onChange={(event) => setDraft((current) => ({ ...current, message: event.target.value }))} placeholder="What worked well? What could be better?" required />
        </label>
        {error ? <p className="form-alert" role="alert">{error}</p> : null}
        {savedNotice ? <p className="form-success" role="status">{savedNotice}</p> : null}
        <div className="feature-actions">
          <button type="submit" className="button button--primary">{editingId ? 'Save changes' : 'Submit feedback'}</button>
          {editingId ? <button type="button" className="button button--outline" onClick={resetForm}>Cancel</button> : null}
        </div>
      </form>

      <section className="feature-section">
        <div className="panel-heading"><div><h2>Feedback history</h2><p>Your submitted feedback and ratings</p></div></div>
        <div className="feature-list">
          {ownFeedback.map((item) => (
            <article className="panel-card feature-card" key={item.id}>
              <div className="feature-card-meta"><div><strong>{item.decisionTitle}</strong><small>Submitted {dateLabel(item.submittedAt)}{item.updatedAt ? ' · edited' : ''}</small></div>
                <span className="feature-rating-value"><Star size={15} fill="currentColor" /> {item.rating}/5</span>
              </div>
              <p className="feature-copy">{item.message}</p>
              <button type="button" className="feature-text-button" onClick={() => startEditing(item)}><Pencil size={14} /> Edit feedback</button>
            </article>
          ))}
          {!ownFeedback.length ? <div className="panel-card empty-state"><p>No feedback submitted yet.</p></div> : null}
        </div>
      </section>
    </div>
  )
}
