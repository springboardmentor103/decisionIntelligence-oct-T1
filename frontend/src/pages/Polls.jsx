
import { Plus, UsersRound, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { useDecisions } from '../context/DecisionContext'
import { useAuth } from '../context/AuthContext'

export default function Polls() {
  const { polls, deletePoll } = useDecisions()
  const [deletingId, setDeletingId] = useState(null)
  const { user } = useAuth()

  const handleDelete = async (pollId) => {
    const confirmed = window.confirm(
      'Are you sure you want to delete this poll?'
    )
    if (!confirmed) return
    try {
      setDeletingId(pollId)
      await deletePoll(pollId)
    } catch (error) {
      console.error('Unable to delete poll:', error)
      alert(
        error.response?.data?.message ||
        'Unable to delete poll. Please try again.'
      )
    } finally {
      setDeletingId(null)
    }
  }

  return (
    <div className="page-block">
      <PageHeading
        title="Community Polls"
        description="Quick polls help your community make a choice together."
        actions={
          <Link to="/polls/create" className="button button--primary">
            <Plus size={15} /> Create Poll
          </Link>
        }
      />

      <div className="community-grid">
        {polls.map((poll) => {
          console.log('Logged-in user ID:', user?.userId)
console.log('Poll creator ID:', poll.creatorId)
console.log('Poll ID:', poll.id)
          const voteCount = Object.values(poll.votes || {}).reduce(
            (total, count) => total + count,
            0
          )
          console.log('Logged-in user ID:', user?.userId)
console.log('Poll creator ID:', poll.creatorId)
console.log('Poll ID:', poll.id)

          return (
            <article className="panel-card community-card" key={poll.id}>
              <div className="community-card-top">
                <span className="category-label">Community Poll</span>
                <span className="status-badge status-active">Open</span>
              </div>

              <h2>{poll.title}</h2>
              <p>{poll.description}</p>

              <div className="community-card-meta">
                <span>
                  <UsersRound size={14} /> {voteCount} votes
                </span>
                <span>
                  Closes{' '}
                  {poll.closesAt
                    ? new Date(`${poll.closesAt}T00:00:00`).toLocaleDateString(
                        'en-US',
                        { month: 'short', day: 'numeric' }
                      )
                    : 'Not specified'}
                </span>
              </div>

              <div className="community-card-meta">
                <Link
                  className="button button--outline button--small"
                  to={`/polls/${poll.id}/results`}
                >
View Poll & Results</Link>{String(poll.creatorId) === String(user?.userId) && (
  <button type="button"
    className="button button--outline button--small"
    onClick={() => handleDelete(poll.id)}
    disabled={deletingId === poll.id}
  >
    <Trash2 size={14} />
    {deletingId === poll.id ? 'Deleting...' : 'Delete'}
  </button>
)}</div>
            </article>
          )
        })}
        {polls.length === 0 && (
          <div className="panel-card empty-state">
            <h2>No polls yet</h2>
            <p>Create a poll to collect quick community feedback.</p>
          </div>
        )}
      </div>
    </div>
  )
}
