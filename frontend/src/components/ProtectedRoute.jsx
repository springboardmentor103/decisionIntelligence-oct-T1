import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute({ children, requiredRole }) {
  const { isAuthenticated, isLoading, user } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return <p className="auth-loading" role="status">Checking your sign-in...</p>
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  const roles = user?.roles || [user?.role || 'user']

  if (requiredRole && !roles.includes(requiredRole)) {
    return <Navigate to="/unauthorized" replace />
  }

  return children ?? <Outlet />
}
