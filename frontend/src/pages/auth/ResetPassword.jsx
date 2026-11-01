import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import Button from '../../components/Button'
import InputField from '../../components/InputField'
import { resetPassword } from '../../services/authService'
import { validatePassword, validatePasswordsMatch } from '../../utils/validation'

export default function ResetPassword() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token') || ''
  const [form, setForm] = useState({ password: '', confirmPassword: '' })
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const onChange = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()

    if (!token) {
      setError('This password reset link is invalid or incomplete.')
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
      await resetPassword({ token, newPassword: form.password })
      navigate('/login', {
        replace: true,
        state: { notice: 'Your password has been updated. Sign in with your new password.' },
      })
    } catch (submitError) {
      setError(submitError.message || 'Unable to reset your password.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="auth-shell">
      <div className="auth-card">
        <p className="eyebrow">Reset access</p>
        <h1>Choose a new password</h1>
        <p className="auth-subtitle">Create a new password for your DecisionHub account.</p>

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <InputField
            label="New password"
            id="new-password"
            name="password"
            type="password"
            value={form.password}
            onChange={onChange}
            autoComplete="new-password"
          />
          <InputField
            label="Confirm new password"
            id="confirm-new-password"
            name="confirmPassword"
            type="password"
            value={form.confirmPassword}
            onChange={onChange}
            autoComplete="new-password"
          />
          {error ? <p className="form-alert" role="alert">{error}</p> : null}
          <Button type="submit" className="full-width" disabled={isSubmitting}>
            {isSubmitting ? 'Updating password...' : 'Update password'}
          </Button>
        </form>

        <div className="auth-links">
          <Link to="/login">Back to sign in</Link>
        </div>
      </div>
    </div>
  )
}
