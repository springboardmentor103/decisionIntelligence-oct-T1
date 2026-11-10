import { Plus, UsersRound } from 'lucide-react'
import { Link } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { useDecisions } from '../context/DecisionContext'

export default function Polls() {
  const { polls } = useDecisions()

  return (
    <div className="page-block">
      <PageHeading
        title="Community Polls"
        description="Quick polls help your community make a choice together."
        actions={<Link to="/polls/create" className="button button--primary"><Plus size={15} /> Create Poll</Link>}
      />
      <div className="community-grid">
        {polls.map((poll) => {
          const voteCount = Object.values(poll.votes).reduce((total, count) => total + count, 0)
          return (
            <article className="panel-card community-card" key={poll.id}>
              <div className="community-card-top"><span className="category-label">Community Poll</span><span className="status-badge status-active">Open</span></div>
              <h2>{poll.title}</h2>
              <p>{poll.description}</p>
              <div className="community-card-meta"><span><UsersRound size={14} /> {voteCount} votes</span><span>Closes {new Date(`${poll.closesAt}T00:00:00`).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}</span></div>
              <Link className="button button--outline button--small" to={`/polls/${poll.id}/results`}>View Poll & Results</Link>
            </article>
          )
        })}
        {polls.length === 0 ? <div className="panel-card empty-state"><h2>No polls yet</h2><p>Create a poll to collect quick community feedback.</p></div> : null}
      </div>
    </div>
  )
}
