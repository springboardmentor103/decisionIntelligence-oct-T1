
import { Eye, EyeOff } from 'lucide-react'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import AuthShowcase from '../../components/AuthShowcase'
import Button from '../../components/Button'
import InputField from '../../components/InputField'
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

  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
  })

  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [acceptedTerms, setAcceptedTerms] = useState(false)

  const [passwordVisible, setPasswordVisible] = useState({
    password: false,
    confirmPassword: false,
  })

  const onChange = (event) => {
    setForm((current) => ({
      ...current,
      [event.target.name]: event.target.value,
    }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()

    const name = `${form.firstName} ${form.lastName}`.trim()

    if (!validateName(form.firstName) || !validateName(form.lastName)) {
      setError('Enter your first and last name.')
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

    if (!acceptedTerms) {
      setError('Please agree to the Terms and Conditions and Privacy Policy.')
      return
    }

    setError('')
    setIsSubmitting(true)

    try {
      if (isDemoMode) {
        await registerUser({
          name,
          email: form.email,
          password: form.password,
        })

        navigate('/dashboard')
      } else {
        await registerUser({
          name,
          email: form.email,
          password: form.password,
        })

        navigate('/login', {
          state: {
            notice: 'Account created successfully. Please sign in.',
          },
        })
      }
    } catch (submitError) {
      setError(
        submitError.message || 'Unable to create your account right now.',
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  const togglePassword = (field) => {
    setPasswordVisible((current) => ({
      ...current,
      [field]: !current[field],
    }))
  }

  return (
    <div className="auth-shell">
      <AuthShowcase />

      <main className="auth-main">
        <div className="auth-card auth-card--register">
          <h1>Create your account</h1>

          <p className="auth-subtitle">
            Start making better decisions in minutes.
          </p>

          {isDemoMode ? (
            <p className="demo-notice" role="status">
              Demo mode is active. Account creation is simulated; no account is saved.
            </p>
          ) : null}

          <form
            className="auth-form register-form"
            onSubmit={handleSubmit}
            noValidate
          >
            <div className="auth-name-row">
              <InputField
                label="First Name"
                id="firstName"
                name="firstName"
                value={form.firstName}
                onChange={onChange}
                placeholder="Aarav"
                autoComplete="given-name"
              />

              <InputField
                label="Last Name"
                id="lastName"
                name="lastName"
                value={form.lastName}
                onChange={onChange}
                placeholder="Mehta"
                autoComplete="family-name"
              />
            </div>

            <InputField
              label="Email Address"
              id="email"
              type="email"
              name="email"
              value={form.email}
              onChange={onChange}
              placeholder="you@college.edu"
              autoComplete="email"
            />

            <div className="password-field-wrap">
              <InputField
                label="Password"
                id="password"
                type={passwordVisible.password ? 'text' : 'password'}
                name="password"
                value={form.password}
                onChange={onChange}
                placeholder="At least 8 characters"
                autoComplete="new-password"
              />

              <button
                type="button"
                className="password-toggle"
                aria-label={
                  passwordVisible.password ? 'Hide password' : 'Show password'
                }
                onClick={() => togglePassword('password')}
              >
                {passwordVisible.password ? (
                  <EyeOff size={16} />
                ) : (
                  <Eye size={16} />
                )}
              </button>
            </div>

            <div className="password-field-wrap">
              <InputField
                label="Confirm Password"
                id="confirmPassword"
                type={passwordVisible.confirmPassword ? 'text' : 'password'}
                name="confirmPassword"
                value={form.confirmPassword}
                onChange={onChange}
                placeholder="Re-enter your password"
                autoComplete="new-password"
              />

              <button
                type="button"
                className="password-toggle"
                aria-label={
                  passwordVisible.confirmPassword
                    ? 'Hide password'
                    : 'Show password'
                }
                onClick={() => togglePassword('confirmPassword')}
              >
                {passwordVisible.confirmPassword ? (
                  <EyeOff size={16} />
                ) : (
                  <Eye size={16} />
                )}
              </button>
            </div>

            <label className="terms-checkbox">
              <input
                type="checkbox"
                checked={acceptedTerms}
                onChange={(event) => setAcceptedTerms(event.target.checked)}
              />

              <span>
                I agree to the <a href="#terms">Terms and Conditions</a> and{' '}
                <a href="#privacy">Privacy Policy</a>
              </span>
            </label>

            {error ? (
              <p className="form-alert" role="alert">
                {error}
              </p>
            ) : null}

            <Button
              type="submit"
              className="full-width auth-submit"
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Creating account...' : 'Create Account'}
            </Button>
          </form>

          <div className="auth-links auth-links--center">
            <span>
              Already have an account? <Link to="/login">Login</Link>
            </span>
          </div>
        </div>
      </main>
    </div>
  )
}
