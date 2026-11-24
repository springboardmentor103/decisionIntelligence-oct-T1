import { useMemo, useState } from 'react'
import { Download, Filter } from 'lucide-react'
import { Link } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { useFeatures } from '../context/FeatureContext'
import { isDemoMode } from '../services/api'

const escapeCsv = (value) => {
  const text = String(value ?? '')
  const safeText = /^[=+\-@]/.test(text) ? `'${text}` : text
  return `"${safeText.replaceAll('"', '""')}"`
}

export default function Reports() {
  const { reportRows } = useFeatures()
  const [type, setType] = useState('All')
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')
  const [error, setError] = useState('')
  const types = [...new Set(reportRows.map((report) => report.type))]
  const filtered = useMemo(() => reportRows.filter((report) => (
    (type === 'All' || report.type === type)
    && (!fromDate || report.date >= fromDate)
    && (!toDate || report.date <= toDate)
  )), [fromDate, reportRows, toDate, type])

  const downloadCsv = () => {
    if (fromDate && toDate && fromDate > toDate) {
      setError('The start date must be on or before the end date.')
      return
    }
    if (!filtered.length) {
      setError('There are no reports in the selected date range to export.')
      return
    }
    const header = ['Type', 'Title', 'Details', 'Status', 'Date']
    const rows = filtered.map(({ type: reportType, title, detail, status, date }) => [reportType, title, detail, status, date])
    const csv = [header, ...rows].map((row) => row.map(escapeCsv).join(',')).join('\r\n')
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }))
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = 'decisionhub-reports.csv'
    anchor.click()
    window.setTimeout(() => URL.revokeObjectURL(url), 1000)
    setError('')
  }

  const reportLink = (report) => {
    if (report.type === 'Poll results') return '/polls/poll-campus-event/results'
    if (report.type === 'Decision report') return '/decision/decision-1'
    return '/communities'
  }

  return (
    <div className="page-block feature-page">
      <PageHeading
        title="Reports"
        description="Review decision, poll, and community participation reports."
        actions={<button type="button" className="button button--primary" onClick={downloadCsv}><Download size={15} /> Export CSV</button>}
      />
      {isDemoMode ? <p className="feature-demo-note">Demo reports are stored in this browser; exported files contain only the rows shown below.</p> : null}
      <section className="panel-card feature-report-filters">
        <div className="feature-filter-title"><Filter size={16} /><strong>Filter reports</strong></div>
        <label className="form-control">Report type
          <select className="form-input" value={type} onChange={(event) => setType(event.target.value)}>
            <option value="All">All report types</option>
            {types.map((item) => <option key={item}>{item}</option>)}
          </select>
        </label>
        <label className="form-control">From
          <input className="form-input" type="date" value={fromDate} onChange={(event) => setFromDate(event.target.value)} />
        </label>
        <label className="form-control">To
          <input className="form-input" type="date" value={toDate} onChange={(event) => setToDate(event.target.value)} />
        </label>
        <button type="button" className="button button--outline" onClick={() => { setType('All'); setFromDate(''); setToDate(''); setError('') }}>Clear filters</button>
      </section>
      {error ? <p className="form-alert" role="alert">{error}</p> : null}
      <section className="panel-card table-panel feature-report-table">
        <div className="panel-heading"><div><h2>Available reports</h2><p>{filtered.length} report{filtered.length === 1 ? '' : 's'}</p></div></div>
        <div className="table-scroll">
          <table className="data-table">
            <thead><tr><th>Report</th><th>Type</th><th>Details</th><th>Status</th><th>Date</th></tr></thead>
            <tbody>
              {filtered.map((report) => (
                <tr key={report.id}>
                  <td><Link className="table-title" to={reportLink(report)}>{report.title}</Link></td>
                  <td>{report.type}</td>
                  <td>{report.detail}</td>
                  <td><span className={`feature-status status-${report.status.toLowerCase().replaceAll(' ', '-')}`}>{report.status}</span></td>
                  <td>{report.date}</td>
                </tr>
              ))}
              {!filtered.length ? <tr><td colSpan="5">No reports match the selected filters.</td></tr> : null}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  )
}
