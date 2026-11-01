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
    localStorage.removeItem('decisionhub-token')
    localStorage.removeItem('decisionhub-user')

    if (isDemoMode) {
      return undefined
    }

    let active = true
    getCurrentUser()
      .then((currentUser) => {
        if (active && currentUser) {
          setUser({
            ...currentUser,
            role: normalizeRoles(currentUser.roles || currentUser.role)[0] || 'user',
            roles: normalizeRoles(currentUser.roles || currentUser.role),
          })
        }
      })
      .catch((error) => {
        if (active && error.response?.status !== 401) {
          setAuthNotice(error.message || 'Unable to restore your sign-in session.')
        }
      })
      .finally(() => {
        if (active) {
          setIsLoading(false)
        }
      })

    return () => {
      active = false
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
    setAuthNotice('')
  }, [])

  const logout = useCallback(() => {
    setUser(null)
    setAuthToken(null)
    localStorage.removeItem('decisionhub-token')
    localStorage.removeItem('decisionhub-user')
  }, [])

  const value = useMemo(
    () => ({
      user,
      isLoading,
      authNotice,
      isAuthenticated: Boolean(user),
      login,
      logout,
    }),
    [user, isLoading, authNotice, login, logout],
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
