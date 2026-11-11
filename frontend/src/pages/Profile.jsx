import { Pencil } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { useAuth } from '../context/AuthContext'
import { useDecisions } from '../context/DecisionContext'

export default function Profile() {
  const { user, updateUser } = useAuth()
  const { decisions, votes } = useDecisions()
  const [editing, setEditing] = useState(false)
  const [name, setName] = useState(user?.name || 'Sadhika Banu Dudekula')
  const [bio, setBio] = useState('')
  const initials = name.split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase()
  const voteCount = Object.keys(votes).length
  const owned = decisions.filter((decision) => decision.isMine || decision.creator === 'You')

  const saveProfile = (event) => {
    event.preventDefault()
    updateUser({ name })
    setEditing(false)
  }

  return (
    <div className="page-block">
      <PageHeading
        title="My Profile"
        actions={<button type="button" className="button button--primary" onClick={() => setEditing((current) => !current)}><Pencil size={14} /> {editing ? 'Cancel Edit' : 'Edit Profile'}</button>}
      />
      <div className="profile-layout">
        <section className="panel-card profile-summary">
          <div className="profile-big-avatar">{initials}</div>
          <h2>{name}</h2>
          <p>{user?.email || '239×1a3314@gprec.ac.in'}</p>
          <div className="profile-stat-strip">
            <div><strong>{owned.length}</strong><span>Decisions created</span></div>
            <div><strong>{voteCount || 14}</strong><span>Votes cast</span></div>
            <div><strong>27</strong><span>Comments</span></div>
          </div>
        </section>
        <form className="panel-card profile-form" onSubmit={saveProfile}>
          <div className="profile-form-grid">
            <label className="form-control">First Name<input disabled={!editing} value={name.split(' ')[0] || ''} onChange={(event) => setName(`${event.target.value} ${name.split(' ').slice(1).join(' ')}`.trim())} /></label>
            <label className="form-control">Last Name<input disabled={!editing} value={name.split(' ').slice(1).join(' ')} onChange={(event) => setName(`${name.split(' ')[0]} ${event.target.value}`.trim())} /></label>
            <label className="form-control form-control--wide">Email Address<input disabled value={user?.email || '239×1a3314@gprec.ac.in'} /></label>
            <label className="form-control form-control--wide">Bio<textarea disabled={!editing} rows="3" value={bio} onChange={(event) => setBio(event.target.value)} placeholder="Tell the community about yourself" /></label>
          </div>
          {editing ? <button className="button button--primary" type="submit">Save Profile</button> : null}
        </form>
      </div>
      <section className="panel-card profile-recent">
        <h2>Recent voting activity</h2>
        {Object.keys(votes).length ? (
          <ul>{Object.entries(votes).map(([id, option]) => {
            const decision = decisions.find((item) => item.id === id)
            return <li key={id}>You voted for <strong>{option}</strong> in <Link to={`/decision/${id}`}>{decision?.title}</Link></li>
          })}</ul>
        ) : <p>No votes yet this session. <Link className="text-link" to="/explore">Explore decisions</Link></p>}
      </section>
    </div>
  )
}
