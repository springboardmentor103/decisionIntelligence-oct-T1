import { useCallback, useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { useDecisions } from '../context/DecisionContext'

export default function CompareOptions() {
  const { decisions } = useDecisions()
  const [searchParams] = useSearchParams()
  const [selectedId, setSelectedId] = useState(searchParams.get('decision') || decisions[0]?.id || '')
  const decision = decisions.find((item) => item.id === selectedId) || decisions[0]
  const [scores, setScores] = useState({})

  const criteria = useMemo(
    () => decision?.criteria?.length ? decision.criteria : [{ name: 'Overall', weight: 100 }],
    [decision],
  )
  const options = useMemo(() => decision?.options || [], [decision])
  const getScore = useCallback((option, criterion) => {
    const stored = scores[`${option.title}:${criterion.name}`]
    return stored ?? option.scores?.[criterion.name] ?? 5
  }, [scores])
  const totals = useMemo(() => options.map((option) => ({
    title: option.title,
    total: criteria.reduce((sum, criterion) => sum + getScore(option, criterion) * criterion.weight / 100, 0),
  })), [options, criteria, getScore])
  const winner = [...totals].sort((a, b) => b.total - a.total)[0]?.title

  const updateScore = (option, criterion, value) => {
    const numericValue = Math.min(10, Math.max(1, Number(value) || 1))
    setScores((current) => ({ ...current, [`${option.title}:${criterion.name}`]: numericValue }))
  }

  return (
    <div className="page-block">
      <PageHeading
        title="Compare Options"
        description="Edit scores (1–10, higher is better) and weights to see how the ranking changes."
        actions={<Link to={decision ? `/decision/${decision.id}` : '/my-decisions'} className="button button--outline button--small">Back to Decision</Link>}
      />
      {decision ? (
        <>
          <label className="decision-select-label">Decision
            <select value={selectedId} onChange={(event) => setSelectedId(event.target.value)}>
              {decisions.map((item) => <option key={item.id} value={item.id}>{item.title}</option>)}
            </select>
          </label>
          <div className="panel-card comparison-table-panel">
            <div className="table-scroll">
              <table className="data-table comparison-table">
                <thead>
                  <tr>
                    <th>Criterion</th>
                    <th>Weight %</th>
                    {options.map((option) => <th className={option.title === winner ? 'winner-column' : ''} key={option.title}>{option.title}{option.title === winner ? <small>TOP SCORE</small> : null}</th>)}
                  </tr>
                </thead>
                <tbody>
                  {criteria.map((criterion) => (
                    <tr key={criterion.name}>
                      <td>{criterion.name}</td>
                      <td>{criterion.weight}</td>
                      {options.map((option) => (
                        <td className={option.title === winner ? 'winner-column' : ''} key={option.title}>
                          <label className="score-input"><span className="sr-only">{option.title} {criterion.name} score</span>
                            <input type="number" min="1" max="10" value={getScore(option, criterion)} onChange={(event) => updateScore(option, criterion, event.target.value)} />
                          </label>
                        </td>
                      ))}
                    </tr>
                  ))}
                  <tr className="total-row">
                    <td>Weighted total</td>
                    <td>{criteria.reduce((sum, criterion) => sum + criterion.weight, 0)}%</td>
                    {totals.map((option) => <td className={option.title === winner ? 'winner-column' : ''} key={option.title}>{option.total.toFixed(2)}</td>)}
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div className="content-grid compare-insights">
            <section className="panel-card">
              <h2>Weighted score comparison</h2>
              {totals.map((option) => (
                <div className="score-bar-row" key={option.title}>
                  <div><span>{option.title}</span><span>{option.total.toFixed(2)} / 10</span></div>
                  <div className="score-bar"><span style={{ width: `${option.total * 10}%` }} className={option.title === winner ? 'score-bar-winner' : ''} /></div>
                </div>
              ))}
              <p className="score-disclaimer">The highest score is a signal, not a verdict. Review the trade-offs and the community’s view before deciding.</p>
            </section>
            <section className="panel-card">
              <h2>Strengths and weaknesses</h2>
              {options.map((option) => {
                const ranked = criteria.map((criterion) => ({ name: criterion.name, score: getScore(option, criterion) })).sort((a, b) => b.score - a.score)
                return (
                  <div className="strength-row" key={option.title}>
                    <strong>{option.title}</strong>
                    <p className="strength">↗ Strongest on {ranked[0]?.name} ({ranked[0]?.score}/10)</p>
                    <p className="weakness">↘ Weakest on {ranked.at(-1)?.name} ({ranked.at(-1)?.score}/10)</p>
                  </div>
                )
              })}
            </section>
          </div>
        </>
      ) : <div className="panel-card empty-state"><h2>Add options to compare</h2><p>Create a decision and add its alternatives to start scoring.</p></div>}
    </div>
  )
}
