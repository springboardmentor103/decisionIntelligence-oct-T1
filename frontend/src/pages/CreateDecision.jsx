import { Plus, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { useDecisions } from '../context/DecisionContext'

const defaultForm = {
  title: '',
  description: '',
  category: '',
  deadline: '',
  visibility: 'Community',
  options: [],
  criteria: [],
}

export default function CreateDecision() {
  const location = useLocation()
  const editing = location.state?.decision
  const navigate = useNavigate()
  const { saveDecision } = useDecisions()
  const [form, setForm] = useState(editing || defaultForm)
  const [optionDraft, setOptionDraft] = useState({ title: '', description: '', pros: '', cons: '' })
  const [criterionDraft, setCriterionDraft] = useState({ name: '', weight: '' })
  const [error, setError] = useState('')
  const totalWeight = form.criteria.reduce((total, criterion) => total + Number(criterion.weight), 0)

  const updateForm = (field, value) => setForm((current) => ({ ...current, [field]: value }))

  const addOption = () => {
    if (!optionDraft.title.trim()) {
      setError('Add an option title before adding it.')
      return
    }
    updateForm('options', [...form.options, {
      title: optionDraft.title.trim(),
      description: optionDraft.description.trim(),
      pros: optionDraft.pros.split('\n').map((item) => item.trim()).filter(Boolean),
      cons: optionDraft.cons.split('\n').map((item) => item.trim()).filter(Boolean),
      scores: {},
    }])
    setOptionDraft({ title: '', description: '', pros: '', cons: '' })
    setError('')
  }

  const addCriterion = () => {
    const weight = Number(criterionDraft.weight)
    if (!criterionDraft.name.trim() || !Number.isFinite(weight) || weight <= 0) {
      setError('Enter a criterion and a positive weight.')
      return
    }
    if (totalWeight + weight > 100) {
      setError('Criterion weights cannot total more than 100%.')
      return
    }
    updateForm('criteria', [...form.criteria, { name: criterionDraft.name.trim(), weight }])
    setCriterionDraft({ name: '', weight: '' })
    setError('')
  }

  const submit = (shouldPublish) => {
    if (!form.title.trim() || !form.category || !form.deadline) {
      setError('Complete the decision title, category and deadline.')
      return
    }
    if (shouldPublish && form.options.length < 2) {
      setError('Add at least two options before publishing.')
      return
    }
    if (form.criteria.length && totalWeight !== 100) {
      setError('Evaluation criterion weights must total 100%.')
      return
    }
    const id = form.id || `decision-${Date.now()}`
    saveDecision({
      ...form,
      id,
      title: form.title.trim(),
      creator: 'You',
      status: shouldPublish ? 'Active' : 'Draft',
      participants: form.participants || 0,
      votes: form.votes || 0,
      options: form.options.map((option) => ({ ...option, scores: option.scores || {} })),
    }, shouldPublish)
    navigate('/my-decisions')
  }

  return (
    <div className="page-block">
      <PageHeading
        title={editing ? 'Edit Decision' : 'Create Decision'}
        description="Describe the choice, add options and define how they will be judged."
      />
      <section className="form-panel panel-card">
        <h2>Basics</h2>
        <div className="form-grid">
          <label className="form-control form-control--wide">Decision Title
            <input value={form.title} onChange={(event) => updateForm('title', event.target.value)} placeholder="e.g. Which framework should we use?" />
          </label>
          <label className="form-control form-control--wide">Decision Description
            <textarea rows="3" value={form.description} onChange={(event) => updateForm('description', event.target.value)} placeholder="Give context so others can weigh in" />
          </label>
          <label className="form-control">Category
            <select value={form.category} onChange={(event) => updateForm('category', event.target.value)}>
              <option value="">Select a category</option>
              {['Career', 'Campus', 'Education', 'Finance', 'Lifestyle', 'Technology', 'Travel'].map((item) => <option key={item}>{item}</option>)}
            </select>
          </label>
          <label className="form-control">Deadline
            <input type="date" value={form.deadline} onChange={(event) => updateForm('deadline', event.target.value)} />
          </label>
          <fieldset className="form-control form-control--wide">
            <legend>Decision Type</legend>
            <div className="visibility-options">
              {[
                ['Community', 'Visible to everyone; anyone can discuss and vote.'],
                ['Private', 'Only invited people can see and vote.'],
              ].map(([value, detail]) => (
                <label key={value} className={`visibility-option ${form.visibility === value ? 'selected' : ''}`}>
                  <input type="radio" name="visibility" value={value} checked={form.visibility === value} onChange={() => updateForm('visibility', value)} />
                  <span><strong>{value}</strong><small>{detail}</small></span>
                </label>
              ))}
            </div>
          </fieldset>
        </div>
      </section>

      <section className="form-panel panel-card">
        <h2>Options <span className="muted-count">({form.options.length} added)</span></h2>
        <div className="repeat-entry option-entry">
          <label className="form-control">Option Title<input value={optionDraft.title} onChange={(event) => setOptionDraft({ ...optionDraft, title: event.target.value })} placeholder="e.g. Option A" /></label>
          <label className="form-control">Option Description<input value={optionDraft.description} onChange={(event) => setOptionDraft({ ...optionDraft, description: event.target.value })} placeholder="Short summary" /></label>
          <label className="form-control">Pros <span className="muted-count">(one per line)</span><textarea rows="2" value={optionDraft.pros} onChange={(event) => setOptionDraft({ ...optionDraft, pros: event.target.value })} placeholder="What works well about this option?" /></label>
          <label className="form-control">Cons <span className="muted-count">(one per line)</span><textarea rows="2" value={optionDraft.cons} onChange={(event) => setOptionDraft({ ...optionDraft, cons: event.target.value })} placeholder="What are the trade-offs?" /></label>
          <button type="button" className="button button--dark button--small" onClick={addOption}><Plus size={15} /> Add Option</button>
        </div>
        {form.options.length > 0 ? <ul className="created-items">{form.options.map((option, index) => <li key={`${option.title}-${index}`}><span>{option.title}<small>{option.description}</small><small>{(option.pros || []).length} pros · {(option.cons || []).length} cons</small></span><button type="button" aria-label={`Remove ${option.title}`} onClick={() => updateForm('options', form.options.filter((_, optionIndex) => optionIndex !== index))}><Trash2 size={15} /></button></li>)}</ul> : null}
      </section>

      <section className="form-panel panel-card">
        <h2>Evaluation Criteria <span className="muted-count">(weights total {totalWeight}%)</span></h2>
        <div className="repeat-entry repeat-entry--criteria">
          <label className="form-control">Criterion<input value={criterionDraft.name} onChange={(event) => setCriterionDraft({ ...criterionDraft, name: event.target.value })} placeholder="e.g. Cost" /></label>
          <label className="form-control">Criterion Weight (%)<input type="number" min="1" max="100" value={criterionDraft.weight} onChange={(event) => setCriterionDraft({ ...criterionDraft, weight: event.target.value })} placeholder="25" /></label>
          <button type="button" className="button button--dark button--small" onClick={addCriterion}><Plus size={15} /> Add Criterion</button>
        </div>
        {form.criteria.length > 0 ? <ul className="created-items">{form.criteria.map((criterion, index) => <li key={`${criterion.name}-${index}`}><span>{criterion.name}<small>{criterion.weight}% weight</small></span><button type="button" aria-label={`Remove ${criterion.name}`} onClick={() => updateForm('criteria', form.criteria.filter((_, itemIndex) => itemIndex !== index))}><Trash2 size={15} /></button></li>)}</ul> : null}
      </section>
      {error ? <p className="form-alert" role="alert">{error}</p> : null}
      <div className="form-footer-actions">
        <button type="button" className="button button--outline" onClick={() => navigate(-1)}>Cancel</button>
        <button type="button" className="button button--outline" onClick={() => submit(false)}>Save as Draft</button>
        <button type="button" className="button button--primary" onClick={() => submit(true)}>Publish Decision</button>
      </div>
    </div>
  )
}
