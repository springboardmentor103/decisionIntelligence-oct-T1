import { Plus, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { useDecisions } from '../context/DecisionContext'

export default function CreatePoll() {
  const navigate = useNavigate()
  const { createPoll } = useDecisions()
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [closesAt, setClosesAt] = useState('')
  const [optionDraft, setOptionDraft] = useState('')
  const [options, setOptions] = useState([])
  const [error, setError] = useState('')

  const addOption = () => {
    const option = optionDraft.trim()
    if (!option) return
    if (options.some((item) => item.toLowerCase() === option.toLowerCase())) {
      setError('Poll options must be unique.')
      return
    }
    setOptions((current) => [...current, option])
    setOptionDraft('')
    setError('')
  }

  const submit = (event) => {
    event.preventDefault()
    if (!title.trim() || !closesAt || options.length < 2) {
      setError('Add a title, closing date and at least two options.')
      return
    }
    if (closesAt < new Date().toISOString().slice(0, 10)) {
      setError('The poll closing date cannot be in the past.')
      return
    }
    const id = `poll-${Date.now()}`
    createPoll({ id, title: title.trim(), description: description.trim(), options, votes: Object.fromEntries(options.map((option) => [option, 0])), closesAt, creator: 'You' })
    navigate(`/polls/${id}/results`)
  }

  return (
    <div className="page-block">
      <PageHeading title="Create a Poll" description="Ask one clear question and give your community options to choose from." />
      <form className="panel-card poll-form" onSubmit={submit}>
        <label className="form-control">Poll question
          <input required value={title} onChange={(event) => setTitle(event.target.value)} placeholder="e.g. Which workshop should we host?" />
        </label>
        <label className="form-control">Description
          <textarea rows="3" value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Add context to help people choose" />
        </label>
        <label className="form-control">Poll closes on
          <input required type="date" min={new Date().toISOString().slice(0, 10)} value={closesAt} onChange={(event) => setClosesAt(event.target.value)} />
        </label>
        <div className="poll-options-editor">
          <h2>Options <span className="muted-count">({options.length} added, at least 2 required)</span></h2>
          <div className="repeat-entry poll-option-entry">
            <label className="form-control">Option<input value={optionDraft} onChange={(event) => setOptionDraft(event.target.value)} onKeyDown={(event) => { if (event.key === 'Enter') { event.preventDefault(); addOption() } }} placeholder="Add an answer option" /></label>
            <button type="button" className="button button--dark button--small" onClick={addOption}><Plus size={15} /> Add Option</button>
          </div>
          <ul className="created-items">{options.map((option) => <li key={option}><span>{option}</span><button type="button" aria-label={`Remove ${option}`} onClick={() => setOptions((current) => current.filter((item) => item !== option))}><Trash2 size={15} /></button></li>)}</ul>
        </div>
        {error ? <p className="form-alert" role="alert">{error}</p> : null}
        <div className="form-footer-actions"><button type="button" className="button button--outline" onClick={() => navigate('/polls')}>Cancel</button><button type="submit" className="button button--primary">Create Poll</button></div>
      </form>
    </div>
  )
}
