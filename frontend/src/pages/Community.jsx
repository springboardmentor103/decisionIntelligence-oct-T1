import { MessageCircle, ThumbsUp } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { useDecisions } from '../context/DecisionContext'

export default function Community() {
  const { posts, toggleLike, addPost, addReply } = useDecisions()
  const [query, setQuery] = useState('')
  const [category, setCategory] = useState('All')
  const [showComposer, setShowComposer] = useState(false)
  const [replyingTo, setReplyingTo] = useState('')
  const [replyDraft, setReplyDraft] = useState('')
  const [draft, setDraft] = useState({ title: '', body: '', category: 'Career' })
  const filtered = posts.filter((post) =>
    (category === 'All' || post.category === category) &&
    `${post.title} ${post.body} ${post.author}`.toLowerCase().includes(query.toLowerCase()),
  )

  const createPost = (event) => {
    event.preventDefault()
    if (!draft.title.trim() || !draft.body.trim()) return
    addPost({
      ...draft,
      id: `post-${Date.now()}`,
      author: 'You',
      initials: 'YO',
      time: 'Just now',
      likes: 0,
      replies: 0,
    })
    setDraft({ title: '', body: '', category: 'Career' })
    setShowComposer(false)
  }

  const createReply = (event, postId) => {
    event.preventDefault()
    if (!replyDraft.trim()) return
    addReply(postId, replyDraft.trim(), 'You')
    setReplyDraft('')
    setReplyingTo('')
  }

  return (
    <div className="page-block community-page">
      <PageHeading
        title="Community"
        description="Learn from others and talk through decision-making."
        actions={<><Link to="/communities" className="button button--outline">Browse Communities</Link><button type="button" className="button button--primary" onClick={() => setShowComposer((show) => !show)}>Create Discussion</button></>}
      />
      <div className="community-toolbar">
        <input className="plain-search" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="⌕  Search discussions" />
        <select value={category} onChange={(event) => setCategory(event.target.value)} aria-label="Filter discussions by category">
          {['All', 'Career', 'Technology', 'Campus', 'Finance', 'Travel'].map((item) => <option key={item}>{item}</option>)}
        </select>
      </div>
      {showComposer ? (
        <form className="panel-card discussion-composer" onSubmit={createPost}>
          <h2>Start a discussion</h2>
          <input required value={draft.title} onChange={(event) => setDraft({ ...draft, title: event.target.value })} placeholder="What would you like the community to discuss?" />
          <textarea required rows="3" value={draft.body} onChange={(event) => setDraft({ ...draft, body: event.target.value })} placeholder="Add context or ask a question" />
          <div><select value={draft.category} onChange={(event) => setDraft({ ...draft, category: event.target.value })}>{['Career', 'Technology', 'Campus', 'Finance', 'Travel'].map((item) => <option key={item}>{item}</option>)}</select><button className="button button--primary button--small" type="submit">Post discussion</button></div>
        </form>
      ) : null}
      <div className="discussion-list">
        {filtered.map((post) => (
          <article className="panel-card discussion-card" key={post.id}>
            <div className="discussion-author">
              <span className="mini-avatar">{post.initials}</span>
              <span><strong>{post.author}</strong><small>{post.time}</small></span>
              <span className="discussion-category">{post.category}</span>
            </div>
            <h2>{post.title}</h2>
            <p className="discussion-body">{post.body}</p>
            {post.reply ? <div className="discussion-reply"><span className="mini-avatar mini-avatar--blue">{post.replyAuthor?.slice(0, 2)}</span><p><strong>{post.replyAuthor}</strong> {post.reply}</p></div> : null}
            <div className="discussion-actions">
              <button type="button" className={post.liked ? 'liked' : ''} onClick={() => toggleLike(post.id)}><ThumbsUp size={14} /> {post.likes} Like</button>
              <button type="button" onClick={() => setReplyingTo((current) => current === post.id ? '' : post.id)}><MessageCircle size={14} /> {post.replies} Reply</button>
            </div>
            {replyingTo === post.id ? (
              <form className="reply-form" onSubmit={(event) => createReply(event, post.id)}>
                <input aria-label="Write a reply" value={replyDraft} onChange={(event) => setReplyDraft(event.target.value)} placeholder="Write a reply..." />
                <button type="submit" className="button button--primary button--small">Reply</button>
              </form>
            ) : null}
          </article>
        ))}
        {filtered.length === 0 ? <div className="panel-card empty-state"><p>No discussions match your search.</p></div> : null}
      </div>
    </div>
  )
}
