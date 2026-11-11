import api, { isDemoMode } from './api'
import { normalizeRoles } from '../utils/roles'

const getApiErrorMessage = (error) => {
  const { response } = error
  const responseData = response?.data

  if (typeof responseData === 'string' && responseData.trim()) {
    return responseData
  }

  if (typeof responseData?.message === 'string') {
    return responseData.message
  }

  if (typeof responseData?.error === 'string') {
    return responseData.error
  }

  if (Array.isArray(responseData?.errors)) {
    return responseData.errors
      .map((item) => (typeof item === 'string' ? item : item.message))
      .filter(Boolean)
      .join(' ')
  }

  return response
    ? `Request failed with status ${response.status}.`
    : 'Unable to reach the server. Check your connection and try again.'
}

const normalizeAuthResponse = (data) => {
  const token = data?.token || data?.accessToken

  if (!token) {
    throw new Error('The server did not return a valid authentication token.')
  }

  const roles = normalizeRoles(data.role)

  return {
    token,
    user: {
      id: data.userId,
      name: data.fullName,
      email: data.email,
      role: roles[0] || 'user',
      roles,
    },
  }
}

const buildDemoUser = ({ name, email }) => ({
  id: 'demo-user',
  name: name || email.split('@')[0],
  email,
  role: 'user',
  roles: ['user'],
})

export const loginUser = async ({ email, password }) => {
  if (!email || !password) {
    throw new Error('Email and password are required.')
  }

  if (isDemoMode) {
    return {
      token: null,
      user: buildDemoUser({ email }),
    }
  }

  let response
  try {
    response = await api.post('/v1/auth/login', { email, password })
  } catch (error) {
    throw new Error(getApiErrorMessage(error), { cause: error })
  }

  return normalizeAuthResponse(response.data)
}

export const registerUser = async ({ fullName, email, password }) => {
  if (!fullName || !email || !password) {
    throw new Error('Please complete all registration fields.')
  }

  if (isDemoMode) {
    return {
      token: null,
      user: buildDemoUser({ name: fullName, email }),
    }
  }

  try {
    const response = await api.post('/v1/auth/register', {
      fullName,
      email,
      password,
    })

    return response.data
  } catch (error) {
    throw new Error(getApiErrorMessage(error), { cause: error })
  }
}
export const requestPasswordReset = async ({ email }) => {
  if (!email) {
    throw new Error('Please enter your email address.')
  }

  if (isDemoMode) {
    return {
      message: 'Demo mode: No email was sent. Configure the backend to send real reset instructions.',
    }
  }

  try {
    const response = await api.post('/auth/forgot-password', { email })
    return response.data
  } catch (error) {
    throw new Error(getApiErrorMessage(error), { cause: error })
  }
}
export const resetPassword = async ({ token, newPassword }) => {
  if (!token || !newPassword) {
    throw new Error('A reset token and new password are required.')
  }

  if (isDemoMode) {
    throw new Error('Password reset is unavailable in demo mode.')
  }

  try {
    const response = await api.post('/auth/reset-password', { token, newPassword })
    return response.data
  } catch (error) {
    throw new Error(getApiErrorMessage(error), { cause: error })
  }
}

export const getCurrentUser = async () => {
  await api.get('/auth/csrf')
  const response = await api.get('/auth/me')
  return response.data
}

export const logoutUser = async () => {
  if (isDemoMode) {
    return
  }

  await api.get('/auth/csrf')
  await api.post('/auth/logout')
}
