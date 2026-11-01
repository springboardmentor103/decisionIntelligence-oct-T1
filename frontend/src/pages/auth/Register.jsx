import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Button from '../../components/Button'
import InputField from '../../components/InputField'
import { useAuth } from '../../context/AuthContext'
import { isDemoMode } from '../../services/api'
import { registerUser } from '../../services/authService'
import {
  validateEmail,
  validateName,
  validatePassword,
  validatePasswordsMatch,
} from '../../utils/validation'

export default function Register() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [form, setForm] = useState({ name: '', email: '', password: '', confirmPassword: '' })
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const onChange = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()

    if (!validateName(form.name)) {
      setError('Name must contain at least 2 characters.')
      return
    }

    if (!validateEmail(form.email)) {
      setError('Please enter a valid email address.')
      return
    }

    if (!validatePassword(form.password)) {
      setError('Password must be at least 8 characters long.')
      return
    }

    if (!validatePasswordsMatch(form.password, form.confirmPassword)) {
      setError('Passwords do not match.')
      return
    }

    setError('')
    setIsSubmitting(true)

    try {
      const response = await registerUser({
        name: form.name,
        email: form.email,
        password: form.password,
      })
      login(response.user, response.token)
      navigate('/dashboard')
    } catch (submitError) {
      setError(submitError.message || 'Unable to create your account right now.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="auth-shell">
      <div className="auth-card">
        <p className="eyebrow">Join us</p>
        <h1>Create account</h1>
        <p className="auth-subtitle">Start collaborating on community decisions.</p>
        {isDemoMode ? (
          <p className="demo-notice" role="status">
            Demo mode is active. Account creation is simulated and no account is saved.
          </p>
        ) : null}

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <InputField
            label="Full name"
            id="name"
            name="name"
            value={form.name}
            onChange={onChange}
            placeholder="Jane Doe"
            autoComplete="name"
          />
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
            placeholder="Create a secure password"
            autoComplete="new-password"
          />
          <InputField
            label="Confirm password"
            id="confirmPassword"
            type="password"
            name="confirmPassword"
            value={form.confirmPassword}
            onChange={onChange}
            placeholder="Re-enter your password"
            autoComplete="new-password"
          />

          {error ? <p className="form-alert" role="alert">{error}</p> : null}

          <Button type="submit" className="full-width" disabled={isSubmitting}>
            {isSubmitting ? 'Creating account...' : 'Create account'}
          </Button>
        </form>

        <div className="auth-links">
          <span>Already have an account?</span>
          <Link to="/login">Sign in</Link>
        </div>
      </div>
    </div>
  )
}
