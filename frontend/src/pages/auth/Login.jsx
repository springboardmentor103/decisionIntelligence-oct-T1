import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import Button from '../../components/Button'
import InputField from '../../components/InputField'
import { useAuth } from '../../context/AuthContext'
import { isDemoMode } from '../../services/api'
import { loginUser } from '../../services/authService'
import { validateEmail, validatePassword } from '../../utils/validation'
import AuthShowcase from '../../components/AuthShowcase'

export default function Login() {
  const navigate = useNavigate()
  const location = useLocation()
  const { authNotice, login } = useAuth()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const onChange = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()

    if (!validateEmail(form.email)) {
      setError('Please enter a valid email address.')
      return
    }

    if (!validatePassword(form.password)) {
      setError('Password must be at least 8 characters long.')
      return
    }

    setError('')
    setIsSubmitting(true)

    try {
      const response = await loginUser({ email: form.email, password: form.password })
      login(response.user, response.token)
      navigate(location.state?.from || '/dashboard', { replace: true })
    } catch (submitError) {
      setError(submitError.message || 'Unable to log in at this time.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="auth-shell auth-shell--login">
      <AuthShowcase />
      <main className="auth-main">
        <div className="auth-card">
          <h1>Welcome back</h1>
          <p className="auth-subtitle">Sign in to continue to your DecisionHub workspace.</p>
          {location.state?.notice ? <p className="form-success" role="status">{location.state.notice}</p> : null}
          {authNotice ? <p className="form-alert" role="status">{authNotice}</p> : null}
          {isDemoMode ? <p className="demo-notice" role="status">Demo mode is active. This sign-in is simulated.</p> : null}

          <form className="auth-form" onSubmit={handleSubmit} noValidate>
            <InputField
            label="Email address"
            id="email"
            type="email"
            name="email"
            value={form.email}
            onChange={onChange}
            placeholder="you@example.com"
            autoComplete="email"
            />
            <InputField
            label="Password"
            id="password"
            type="password"
            name="password"
            value={form.password}
            onChange={onChange}
            placeholder="Enter your password"
            autoComplete="current-password"
            />

            {error ? <p className="form-alert" role="alert">{error}</p> : null}

            <Button type="submit" className="full-width auth-submit" disabled={isSubmitting}>
              {isSubmitting ? 'Signing in...' : 'Sign In'}
            </Button>
          </form>

          <div className="auth-links auth-links--center">
            <Link to="/forgot-password">Forgot password?</Link>
            <span>New to DecisionHub? <Link to="/register">Create an account</Link></span>
          </div>
          {!isDemoMode && import.meta.env.VITE_GOOGLE_OAUTH_URL ? (
            <a className="button button--secondary full-width oauth-button" href={import.meta.env.VITE_GOOGLE_OAUTH_URL}>Continue with Google</a>
          ) : null}
        </div>
      </main>
    </div>
  )
}
