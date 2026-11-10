
/* eslint-disable react-refresh/only-export-components */

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react'

import { setAuthToken } from '../services/api'
import { normalizeRoles } from '../utils/roles'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [authNotice, setAuthNotice] = useState('')

  // Restore the login session when the application starts
  useEffect(() => {
    try {
      const savedToken = localStorage.getItem('decisionhub-token')
      const savedUser = localStorage.getItem('decisionhub-user')

      if (savedToken && savedUser) {
        const parsedUser = JSON.parse(savedUser)

        setAuthToken(savedToken)
        setUser({
          ...parsedUser,
          role: normalizeRoles(parsedUser.roles || parsedUser.role)[0] || 'user',
          roles: normalizeRoles(parsedUser.roles || parsedUser.role),
        })
      }
    } catch (error) {
      console.error('Unable to restore login session:', error)

      localStorage.removeItem('decisionhub-token')
      localStorage.removeItem('decisionhub-user')
      setAuthToken(null)
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Login and save user details and token
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

    if (token) {
      localStorage.setItem('decisionhub-token', token)
      localStorage.setItem('decisionhub-user', JSON.stringify(nextUser))
    } else {
      localStorage.removeItem('decisionhub-token')
      localStorage.removeItem('decisionhub-user')
    }
  }, [])

  // Logout and clear saved session
  const logout = useCallback(() => {
    setUser(null)
    setAuthToken(null)
    setAuthNotice('')

    localStorage.removeItem('decisionhub-token')
    localStorage.removeItem('decisionhub-user')
  }, [])

  const updateUser = useCallback((updates) => {
    setUser((current) => {
      if (!current) return current

      const updatedUser = { ...current, ...updates }
      localStorage.setItem('decisionhub-user', JSON.stringify(updatedUser))

      return updatedUser
    })
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

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider')
  }

  return context
}
