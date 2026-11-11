import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { useAuth } from '../context/AuthContext'

const initialPreferences = {
  votingReminders: true,
  comments: true,
  deadlines: true,
  results: false,
  profileVisible: true,
  votesPublic: false,
  findByEmail: true,
}

function Preference({ checked, onChange, children }) {
  return <label className="preference"><input type="checkbox" checked={checked} onChange={onChange} /><span>{children}</span></label>
}

export default function Settings() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [preferences, setPreferences] = useState(initialPreferences)
  const [language, setLanguage] = useState('English')
  const [saved, setSaved] = useState(false)
  const [passwords, setPasswords] = useState({ current: '', next: '', confirm: '' })
  const [passwordMessage, setPasswordMessage] = useState('')

  const toggle = (key) => {
    setPreferences((current) => ({ ...current, [key]: !current[key] }))
    setSaved(false)
  }

  const updatePassword = (event) => {
    event.preventDefault()
    setPasswordMessage(passwords.next.length < 8 ? 'New password must be at least 8 characters.' : passwords.next !== passwords.confirm ? 'New passwords do not match.' : 'Password update is ready for backend integration.')
  }

  return (
    <div className="page-block">
      <PageHeading title="Settings" description="Manage your account, notifications and privacy." />
      <section className="panel-card settings-section">
        <h2>Account</h2>
        <div className="settings-account-grid">
          <label className="form-control">Email Address<input disabled value={user?.email || '239×1a3314@gprec.ac.in'} /></label>
          <label className="form-control">Language<select value={language} onChange={(event) => setLanguage(event.target.value)}><option>English</option><option>Hindi</option><option>Telugu</option></select></label>
        </div>
      </section>
      <form className="panel-card settings-section" onSubmit={updatePassword}>
        <h2>Change Password</h2>
        <div className="password-fields">
          {[
            ['current', 'Current Password'],
            ['next', 'New Password'],
            ['confirm', 'Confirm New Password'],
          ].map(([key, label]) => (
            <label className="form-control" key={key}>{label}<input required type="password" autoComplete={key === 'current' ? 'current-password' : 'new-password'} value={passwords[key]} onChange={(event) => setPasswords({ ...passwords, [key]: event.target.value })} placeholder="••••••••" /></label>
          ))}
        </div>
        <button className="button button--outline button--small" type="submit">Update Password</button>
        {passwordMessage ? <p className="form-success">{passwordMessage}</p> : null}
      </form>
      <section className="panel-card settings-section">
        <h2>Notification preferences</h2>
        <div className="preferences-list">
          <Preference checked={preferences.votingReminders} onChange={() => toggle('votingReminders')}>Voting reminders</Preference>
          <Preference checked={preferences.comments} onChange={() => toggle('comments')}>Comments and replies</Preference>
          <Preference checked={preferences.deadlines} onChange={() => toggle('deadlines')}>Deadline reminders</Preference>
          <Preference checked={preferences.results} onChange={() => toggle('results')}>Results published</Preference>
        </div>
      </section>
      <section className="panel-card settings-section">
        <h2>Privacy</h2>
        <div className="preferences-list">
          <Preference checked={preferences.profileVisible} onChange={() => toggle('profileVisible')}>Show my profile to community members</Preference>
          <Preference checked={preferences.votesPublic} onChange={() => toggle('votesPublic')}>Show my voting activity publicly</Preference>
          <Preference checked={preferences.findByEmail} onChange={() => toggle('findByEmail')}>Allow others to find me by email</Preference>
        </div>
      </section>
      <div className="settings-footer">
        <button type="button" className="button button--danger-outline" onClick={() => { logout(); navigate('/login') }}>Logout</button>
        <button type="button" className="button button--primary" onClick={() => setSaved(true)}>{saved ? 'Settings Saved' : 'Save Settings'}</button>
      </div>
    </div>
  )
}
