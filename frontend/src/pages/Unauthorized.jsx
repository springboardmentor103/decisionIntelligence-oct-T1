import { Link } from 'react-router-dom'

export default function Unauthorized() {
  return (
    <div className="auth-shell auth-shell--centered">
      <div className="auth-card error-card">
        <p className="eyebrow">Access denied</p>
        <h1>Unauthorized</h1>
        <p>You do not have permission to view this page.</p>
        <Link to="/dashboard" className="button button--primary button--link">
          Return to dashboard
        </Link>
      </div>
    </div>
  )
}
