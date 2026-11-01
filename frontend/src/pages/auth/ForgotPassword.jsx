import { useState } from 'react'
import { Link } from 'react-router-dom'
import Button from '../../components/Button'
import InputField from '../../components/InputField'
import { isDemoMode } from '../../services/api'
import { requestPasswordReset } from '../../services/authService'
import { validateEmail } from '../../utils/validation'

export default function ForgotPassword() {
  const [email, setEmail] = useState('')
  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (event) => {
    event.preventDefault()

    if (!validateEmail(email)) {
      setError('Please enter a valid email address.')
      setStatus('')
      return
    }

    setError('')
    setIsSubmitting(true)

    try {
      const response = await requestPasswordReset({ email })
      setStatus(response.message || 'A password reset email has been sent.')
    } catch (submitError) {
      setError(submitError.message || 'Unable to send reset instructions.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="auth-shell">
      <div className="auth-card">
        <p className="eyebrow">Reset access</p>
        <h1>Forgot password</h1>
        <p className="auth-subtitle">Enter your email to receive instructions to reset your password.</p>
        {isDemoMode ? (
          <p className="demo-notice" role="status">
            Demo mode is active. Password reset is simulated; no email will be sent.
          </p>
        ) : null}

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <InputField
            label="Email address"
            id="reset-email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="you@example.com"
            autoComplete="email"
          />

          {error ? <p className="form-alert" role="alert">{error}</p> : null}
          {status ? <p className="form-success">{status}</p> : null}

          <Button type="submit" className="full-width" disabled={isSubmitting}>
            {isSubmitting ? 'Sending...' : 'Send reset link'}
          </Button>
        </form>

        <div className="auth-links">
          <Link to="/login">Back to sign in</Link>
          <Link to="/register">Create an account</Link>
        </div>
      </div>
    </div>
  )
}
