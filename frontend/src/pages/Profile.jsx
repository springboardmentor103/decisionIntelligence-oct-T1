import { useAuth } from '../context/AuthContext'

export default function Profile() {
  const { user } = useAuth()

  return (
    <div className="page-block">
      <div className="section-header">
        <div>
          <p className="eyebrow">Account</p>
          <h1>Profile</h1>
        </div>
      </div>

      <div className="content-grid two-column">
        <section className="panel-card profile-card">
          <div className="profile-avatar">{user?.name?.charAt(0).toUpperCase() || 'D'}</div>
          <h3>{user?.name || 'DecisionHub User'}</h3>
          <p>{user?.email || 'user@decisionhub.local'}</p>
          <span className="badge profile-role">{user?.role || 'user'}</span>
        </section>

        <section className="panel-card">
          <div className="panel-header">
            <h3>Account settings</h3>
          </div>
          <ul className="settings-list">
            <li>Update personal information</li>
            <li>Review notification preferences</li>
            <li>Manage connected social accounts</li>
          </ul>
        </section>
      </div>
    </div>
  )
}
