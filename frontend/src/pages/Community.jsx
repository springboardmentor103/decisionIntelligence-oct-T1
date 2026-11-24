import { useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { MessageCircle, Pencil, ThumbsUp, Trash2, TriangleAlert } from 'lucide-react'
import PageHeading from '../components/PageHeading'
import { useDecisions } from '../context/DecisionContext'
import { useFeatures } from '../context/FeatureContext'

const demoDecisionChoices = [
  { id: 'decision-1', title: 'Final-Year Project Tech Stack', category: 'Technology' },
  { id: 'decision-2', title: 'Where Should We Hold the Annual Tech Fest?', category: 'Campus' },
  { id: 'decision-5', title: 'Group Trip Destination for December', category: 'Travel' },
]

const formatDate = (value) => new Date(value).toLocaleString(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short',
})

export default function Community() {
  const { decisions } = useDecisions()
  const {
    discussions,
    userId,
    createDiscussion,
    editDiscussion,
    deleteDiscussion,
    toggleDiscussionLike,
    addComment,
    submitReport,
  } = useFeatures()
  const [searchParams] = useSearchParams()
  const choices = decisions.length ? decisions : demoDecisionChoices
  const [decisionFilter, setDecisionFilter] = useState(searchParams.get('decisionId') || 'all')
  const [query, setQuery] = useState('')
  const [composerOpen, setComposerOpen] = useState(false)
  const [draft, setDraft] = useState({ decisionId: choices[0]?.id || '', title: '', body: '' })
  const [commentDrafts, setCommentDrafts] = useState({})
  const [replyDrafts, setReplyDrafts] = useState({})
  const [replyingTo, setReplyingTo] = useState('')
  const [editingId, setEditingId] = useState('')
  const [editDraft, setEditDraft] = useState({ title: '', body: '' })
  const [reportingId, setReportingId] = useState('')
  const [reportDraft, setReportDraft] = useState({ reason: 'Spam', details: '' })
  const [error, setError] = useState('')

  const selectedDecisionId = choices.some((item) => String(item.id) === String(draft.decisionId))
    ? draft.decisionId
    : choices[0]?.id || ''

  const filtered = useMemo(() => discussions.filter((discussion) => {
    const matchesDecision = decisionFilter === 'all' || String(discussion.decisionId) === String(decisionFilter)
    const matchesQuery = `${discussion.title} ${discussion.body} ${discussion.author} ${discussion.decisionTitle}`
      .toLowerCase().includes(query.trim().toLowerCase())
    return matchesDecision && matchesQuery
  }), [decisionFilter, discussions, query])

  const handleCreate = (event) => {
    event.preventDefault()
    const decision = choices.find((item) => String(item.id) === String(selectedDecisionId))
    if (!decision || !draft.title.trim() || !draft.body.trim()) return

    createDiscussion({
      decisionId: decision.id,
      decisionTitle: decision.title,
      category: decision.category || 'General',
      title: draft.title.trim(),
      body: draft.body.trim(),
    })
    setDraft((current) => ({ ...current, title: '', body: '' }))
    setComposerOpen(false)
    setError('')
  }

  const handleComment = (event, discussionId) => {
    event.preventDefault()
    const body = commentDrafts[discussionId]?.trim()
    if (!body) return
    addComment(discussionId, body)
    setCommentDrafts((current) => ({ ...current, [discussionId]: '' }))
  }

  const handleReply = (event, discussionId, commentId) => {
    event.preventDefault()
    const body = replyDrafts[commentId]?.trim()
    if (!body) return
    addComment(discussionId, body, commentId)
    setReplyDrafts((current) => ({ ...current, [commentId]: '' }))
    setReplyingTo('')
  }

  const startEditing = (discussion) => {
    setEditingId(discussion.id)
    setEditDraft({ title: discussion.title, body: discussion.body })
    setError('')
  }

  const saveEdit = (event, discussionId) => {
    event.preventDefault()
    if (!editDraft.title.trim() || !editDraft.body.trim()) {
      setError('A discussion title and post are required.')
      return
    }
    try {
      editDiscussion(discussionId, {
        title: editDraft.title.trim(),
        body: editDraft.body.trim(),
      })
      setEditingId('')
    } catch (editError) {
      setError(editError.message)
    }
  }

  const handleDelete = (discussionId) => {
    if (!window.confirm('Delete this discussion and its comments?')) return
    try {
      deleteDiscussion(discussionId)
      setError('')
    } catch (deleteError) {
      setError(deleteError.message)
    }
  }

  const handleReport = (event, discussion) => {
    event.preventDefault()
    submitReport(discussion, reportDraft.reason, reportDraft.details.trim())
    setReportingId('')
    setReportDraft({ reason: 'Spam', details: '' })
  }

  return (
    <div className="page-block feature-page discussion-page">
      <PageHeading
        title="Discussions"
        description="Discuss decisions, share comments, and follow up on replies."
        actions={<button type="button" className="button button--primary" onClick={() => setComposerOpen((open) => !open)}>Create discussion</button>}
      />
      <p className="feature-demo-note">Demo workspace: discussions are saved only in this browser.</p>
      {error ? <p className="form-alert" role="alert">{error}</p> : null}

      <div className="feature-toolbar">
        <input className="form-input" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search discussions" aria-label="Search discussions" />
        <select className="form-input" value={decisionFilter} onChange={(event) => setDecisionFilter(event.target.value)} aria-label="Filter by decision">
          <option value="all">All decisions</option>
          {choices.map((decision) => <option value={decision.id} key={decision.id}>{decision.title}</option>)}
        </select>
      </div>

      {composerOpen ? (
        <form className="panel-card feature-form" onSubmit={handleCreate}>
          <h2>Start a decision discussion</h2>
          <label className="form-control">Related decision
            <select className="form-input" value={selectedDecisionId} onChange={(event) => setDraft((current) => ({ ...current, decisionId: event.target.value }))} required>
              {choices.map((decision) => <option value={decision.id} key={decision.id}>{decision.title}</option>)}
            </select>
          </label>
          <label className="form-control">Title
            <input className="form-input" value={draft.title} onChange={(event) => setDraft((current) => ({ ...current, title: event.target.value }))} maxLength="120" required />
          </label>
          <label className="form-control">Post
            <textarea className="form-input" rows="4" value={draft.body} onChange={(event) => setDraft((current) => ({ ...current, body: event.target.value }))} maxLength="2000" required />
          </label>
          <button type="submit" className="button button--primary">Publish discussion</button>
        </form>
      ) : null}

      <div className="feature-list">
        {filtered.map((discussion) => {
          const topLevelComments = discussion.comments.filter((comment) => !comment.parentId)
          const decisionExists = decisions.some((item) => String(item.id) === String(discussion.decisionId))
          return (
            <article id={discussion.id} className="panel-card feature-card discussion-card" key={discussion.id}>
              <div className="feature-card-meta">
                <div className="feature-avatar">{discussion.initials}</div>
                <div><strong>{discussion.author}</strong><small>{formatDate(discussion.createdAt)}{discussion.updatedAt ? ' · edited' : ''}</small></div>
                <span className="feature-status">{discussion.category}</span>
              </div>

              {editingId === discussion.id ? (
                <form className="feature-form feature-edit-form" onSubmit={(event) => saveEdit(event, discussion.id)}>
                  <label className="form-control">Title
                    <input className="form-input" value={editDraft.title} onChange={(event) => setEditDraft((current) => ({ ...current, title: event.target.value }))} required maxLength="120" />
                  </label>
                  <label className="form-control">Post
                    <textarea className="form-input" rows="3" value={editDraft.body} onChange={(event) => setEditDraft((current) => ({ ...current, body: event.target.value }))} required maxLength="2000" />
                  </label>
                  <div className="feature-actions"><button className="button button--primary button--small" type="submit">Save changes</button><button className="button button--outline button--small" type="button" onClick={() => setEditingId('')}>Cancel</button></div>
                </form>
              ) : (
                <>
                  <h2>{discussion.title}</h2>
                  <p className="feature-copy">{discussion.body}</p>
                </>
              )}

              {decisionExists
                ? <Link className="feature-related-link" to={`/decision/${discussion.decisionId}`}>Decision: {discussion.decisionTitle}</Link>
                : <span className="feature-related-link">Demo decision: {discussion.decisionTitle}</span>}
              <div className="feature-actions">
                <button className={`feature-text-button ${discussion.likedByMe ? 'is-selected' : ''}`} type="button" onClick={() => toggleDiscussionLike(discussion.id)} aria-pressed={discussion.likedByMe}><ThumbsUp size={15} /> {discussion.likes} Like</button>
                <span className="feature-count"><MessageCircle size={15} /> {discussion.comments.length} comments</span>
                {discussion.authorId === userId ? (
                  <>
                    <button className="feature-text-button" type="button" onClick={() => startEditing(discussion)} aria-label="Edit discussion"><Pencil size={14} /> Edit</button>
                    <button className="feature-text-button feature-danger" type="button" onClick={() => handleDelete(discussion.id)} aria-label="Delete discussion"><Trash2 size={14} /> Delete</button>
                  </>
                ) : (
                  <button className="feature-text-button" type="button" onClick={() => setReportingId((current) => current === discussion.id ? '' : discussion.id)}><TriangleAlert size={14} /> Report</button>
                )}
              </div>

              {reportingId === discussion.id ? (
                <form className="feature-inline-form" onSubmit={(event) => handleReport(event, discussion)}>
                  <label className="form-control">Reason
                    <select className="form-input" value={reportDraft.reason} onChange={(event) => setReportDraft((current) => ({ ...current, reason: event.target.value }))}>
                      {['Spam', 'Harassment', 'Misinformation', 'Other'].map((reason) => <option key={reason}>{reason}</option>)}
                    </select>
                  </label>
                  <label className="form-control">Details
                    <input className="form-input" value={reportDraft.details} onChange={(event) => setReportDraft((current) => ({ ...current, details: event.target.value }))} placeholder="Optional context" />
                  </label>
                  <button className="button button--outline button--small" type="submit">Submit report</button>
                </form>
              ) : null}

              <div className="feature-comments">
                {topLevelComments.map((comment) => {
                  const replies = discussion.comments.filter((item) => item.parentId === comment.id)
                  return (
                    <div className="feature-comment-thread" key={comment.id}>
                      <div className="feature-comment"><strong>{comment.author}</strong><p>{comment.body}</p><small>{formatDate(comment.createdAt)}</small></div>
                      {replies.map((reply) => <div className="feature-comment feature-comment-reply" key={reply.id}><strong>{reply.author}</strong><p>{reply.body}</p><small>{formatDate(reply.createdAt)}</small></div>)}
                      <button className="feature-text-button" type="button" onClick={() => setReplyingTo((current) => current === comment.id ? '' : comment.id)}>Reply</button>
                      {replyingTo === comment.id ? (
                        <form className="feature-inline-form" onSubmit={(event) => handleReply(event, discussion.id, comment.id)}>
                          <input className="form-input" value={replyDrafts[comment.id] || ''} onChange={(event) => setReplyDrafts((current) => ({ ...current, [comment.id]: event.target.value }))} placeholder={`Reply to ${comment.author}`} required />
                          <button className="button button--outline button--small" type="submit">Send reply</button>
                        </form>
                      ) : null}
                    </div>
                  )
                })}
                <form className="feature-inline-form" onSubmit={(event) => handleComment(event, discussion.id)}>
                  <input className="form-input" value={commentDrafts[discussion.id] || ''} onChange={(event) => setCommentDrafts((current) => ({ ...current, [discussion.id]: event.target.value }))} placeholder="Add a comment" required />
                  <button className="button button--outline button--small" type="submit">Comment</button>
                </form>
              </div>
            </article>
          )
        })}
        {filtered.length === 0 ? <div className="panel-card empty-state"><p>No discussions found for this filter.</p></div> : null}
      </div>
    </div>
  )
}
