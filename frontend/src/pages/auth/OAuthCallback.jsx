import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { getCurrentUser } from '../../services/authService'

export default function OAuthCallback() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true

    getCurrentUser()
      .then((user) => {
        if (active) {
          login(user)
          navigate('/dashboard', { replace: true })
        }
      })
      .catch((callbackError) => {
        if (active) {
          setError(callbackError.message || 'Unable to complete Google sign-in.')
        }
      })

    return () => {
      active = false
    }
  }, [login, navigate])

  return (
    <div className="auth-shell auth-shell--centered">
      <div className="auth-card error-card">
        {error ? (
          <>
            <p className="eyebrow">Sign-in issue</p>
            <h1>Could not finish sign-in</h1>
            <p className="form-alert" role="alert">{error}</p>
            <Link to="/login" className="button button--primary button--link">
              Return to sign in
            </Link>
          </>
        ) : (
          <>
            <p className="eyebrow">DecisionHub</p>
            <h1>Completing sign-in</h1>
            <p className="auth-subtitle" role="status">Please wait while we verify your account.</p>
          </>
        )}
      </div>
    </div>
  )
}
