import { useMemo } from 'react'
import {
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  Tooltip,
} from 'chart.js'
import { Bar } from 'react-chartjs-2'
import { Activity, CircleCheck, UsersRound, Vote } from 'lucide-react'
import PageHeading from '../components/PageHeading'
import { useDecisions } from '../context/DecisionContext'
import { useFeatures } from '../context/FeatureContext'
import { isDemoMode } from '../services/api'

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend)

const demoMetrics = {
  decisions: 6,
  polls: 1,
  votes: 461,
  communities: 4,
}

export default function Analytics() {
  const { decisions, polls, communities, activityEvents } = useDecisions()
  const { discussions, feedback } = useFeatures()
  const useDemoSamples = isDemoMode && decisions.length === 0 && polls.length === 0
  const voteTotal = decisions.reduce((sum, decision) => sum + Number(decision.votes || decision.participants || 0), 0)
    + polls.reduce((sum, poll) => sum + Object.values(poll.votes || {}).reduce((count, value) => count + Number(value || 0), 0), 0)
  const stats = [
    { label: 'Total decisions', value: useDemoSamples ? demoMetrics.decisions : decisions.length, icon: Activity },
    { label: 'Total polls', value: useDemoSamples ? demoMetrics.polls : polls.length, icon: Vote },
    { label: 'Total votes', value: useDemoSamples ? demoMetrics.votes : voteTotal, icon: CircleCheck },
    { label: 'Active communities', value: useDemoSamples ? demoMetrics.communities : communities.length, icon: UsersRound },
  ]

  const categoryData = useMemo(() => {
    const categories = decisions.reduce((counts, decision) => {
      const category = decision.category || 'Other'
      counts[category] = (counts[category] || 0) + 1
      return counts
    }, {})
    if (useDemoSamples) {
      return { labels: ['Technology', 'Campus', 'Career', 'Travel', 'Finance'], values: [2, 1, 1, 1, 1] }
    }
    return { labels: Object.keys(categories), values: Object.values(categories) }
  }, [decisions, useDemoSamples])

  const participationData = useMemo(() => {
    const sources = [
      ...activityEvents.map((event) => event.timestamp),
      ...discussions.map((item) => item.createdAt),
      ...feedback.map((item) => item.submittedAt),
    ].filter(Boolean).map((value) => new Date(value))
    if (!sources.length && useDemoSamples) {
      return { labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'], values: [12, 19, 14, 26, 18, 31, 24] }
    }

    const today = new Date()
    const days = Array.from({ length: 7 }, (_, index) => {
      const date = new Date(today)
      date.setDate(today.getDate() - (6 - index))
      return date
    })
    return {
      labels: days.map((date) => date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })),
      values: days.map((day) => sources.filter((date) => date.toDateString() === day.toDateString()).length),
    }
  }, [activityEvents, discussions, feedback, useDemoSamples])

  const categoryChart = {
    labels: categoryData.labels,
    datasets: [{
      label: 'Decisions',
      data: categoryData.values,
      backgroundColor: ['#12b8a5', '#5273df', '#f0a04b', '#8169cf', '#4b98c7'],
      borderRadius: 5,
    }],
  }
  const participationChart = {
    labels: participationData.labels,
    datasets: [{
      label: 'Participation events',
      data: participationData.values,
      backgroundColor: '#5273df',
      borderRadius: 5,
    }],
  }
  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: { grid: { display: false } },
      y: { beginAtZero: true, ticks: { precision: 0 } },
    },
  }

  return (
    <div className="page-block feature-page analytics-page">
      <PageHeading title="Analytics" description="A snapshot of decisions, polls, votes, and community participation." />
      {useDemoSamples ? <p className="feature-demo-note">Demo sample data is shown. Live backend analytics are not connected in frontend-only mode.</p> : null}
      <div className="stats-grid feature-analytics-stats">
        {stats.map(({ label, value, icon: Icon }) => <article className="stat-card feature-stat-card" key={label}><div className="stat-card-top"><p>{label}</p><span className="stat-icon"><Icon size={17} /></span></div><h2>{value.toLocaleString()}</h2><span className="stat-caption">{useDemoSamples ? 'Demo sample' : 'Current workspace'}</span></article>)}
      </div>
      <div className="feature-chart-grid">
        <section className="panel-card feature-chart-card">
          <div className="panel-heading"><div><h2>Decisions by category</h2><p>Distribution across decision topics</p></div></div>
          {categoryData.labels.length ? <div className="feature-chart"><Bar data={categoryChart} options={options} /></div> : <p className="muted-copy">Create decisions to see category analytics.</p>}
        </section>
        <section className="panel-card feature-chart-card">
          <div className="panel-heading"><div><h2>Participation</h2><p>Recent activity in the workspace</p></div></div>
          {participationData.labels.length ? <div className="feature-chart" role="img" aria-label="Participation activity by day"><Bar data={participationChart} options={options} /></div> : <p className="muted-copy">Participation statistics will appear when activity is recorded.</p>}
        </section>
      </div>
      <p className="feature-data-note">{discussions.length} discussions · {feedback.length} feedback submissions. Participation totals depend on the data available in this browser.</p>
    </div>
  )
}
