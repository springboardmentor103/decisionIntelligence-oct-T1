import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import DashboardLayout from './layouts/DashboardLayout'
import Dashboard from './pages/Dashboard'
import NotFound from './pages/NotFound'
import Profile from './pages/Profile'
import RoleWorkspace from './pages/RoleWorkspace'
import ForgotPassword from './pages/auth/ForgotPassword'
import OAuthCallback from './pages/auth/OAuthCallback'
import Login from './pages/auth/Login'
import Register from './pages/auth/Register'
import ResetPassword from './pages/auth/ResetPassword'
import Unauthorized from './pages/Unauthorized'
import './App.css'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/reset-password" element={<ResetPassword />} />
      <Route path="/oauth/callback" element={<OAuthCallback />} />

      <Route path="/" element={<Navigate to="/dashboard" replace />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<DashboardLayout />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/profile" element={<Profile />} />
          <Route
            path="/moderation"
            element={
              <ProtectedRoute requiredRole="moderator">
                <RoleWorkspace
                  title="Moderation"
                  description="Review community content and support healthy discussion."
                />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <ProtectedRoute requiredRole="admin">
                <RoleWorkspace
                  title="Administration"
                  description="Manage platform settings and user access."
                />
              </ProtectedRoute>
            }
          />
        </Route>
      </Route>

      <Route path="/unauthorized" element={<Unauthorized />} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}

export default App
