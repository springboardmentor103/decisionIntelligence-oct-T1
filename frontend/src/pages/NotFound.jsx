import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="auth-shell auth-shell--centered">
      <div className="auth-card error-card">
        <p className="eyebrow">404</p>
        <h1>Page not found</h1>
        <p>The page you are looking for does not exist or has moved.</p>
        <Link to="/dashboard" className="button button--primary button--link">
          Back to home
        </Link>
      </div>
    </div>
  )
}
