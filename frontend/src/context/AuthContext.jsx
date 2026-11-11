/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { isDemoMode, setAuthToken } from '../services/api'
import { getCurrentUser } from '../services/authService'
import { normalizeRoles } from '../utils/roles'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [isLoading, setIsLoading] = useState(!isDemoMode)
  const [authNotice, setAuthNotice] = useState('')

  useEffect(() => {
  if (isDemoMode) {
    setIsLoading(false)
    return
  }

  try {
    const token = localStorage.getItem('decisionhub-token')
    const savedUser = localStorage.getItem('decisionhub-user')

    if (token && savedUser) {
      setAuthToken(token)
      setUser(JSON.parse(savedUser))
    }
  } catch (error) {
    localStorage.removeItem('decisionhub-token')
    localStorage.removeItem('decisionhub-user')
    setAuthToken(null)
  } finally {
    setIsLoading(false)
  }
}, [])
  const login = useCallback((userData, token) => {
  const roles = normalizeRoles(userData.roles || userData.role)

  const nextUser = {
    ...userData,
    role: roles[0] || 'user',
    roles,
  }

  setUser(nextUser)
  setAuthToken(token)

  if (token) {
    localStorage.setItem('decisionhub-token', token)
    localStorage.setItem('decisionhub-user', JSON.stringify(nextUser))
  }

  setAuthNotice('')
}, [])

  const logout = useCallback(() => {
    setUser(null)
    setAuthToken(null)
    localStorage.removeItem('decisionhub-token')
    localStorage.removeItem('decisionhub-user')
  }, [])

  const updateUser = useCallback((updates) => {
    setUser((current) => (current ? { ...current, ...updates } : current))
  }, [])

  const value = useMemo(
    () => ({
      user,
      isLoading,
      authNotice,
      isAuthenticated: Boolean(user),
      login,
      logout,
      updateUser,
    }),
    [user, isLoading, authNotice, login, logout, updateUser],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider')
  }

  return context
}
