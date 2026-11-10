import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import DashboardLayout from './layouts/DashboardLayout'
import Dashboard from './pages/Dashboard'
import ExploreDecisions from './pages/ExploreDecisions'
import MyDecisions from './pages/MyDecisions'
import CreateDecision from './pages/CreateDecision'
import CompareOptions from './pages/CompareOptions'
import Community from './pages/Community'
import Communities from './pages/Communities'
import CommunityDetails from './pages/CommunityDetails'
import Vote from './pages/Vote'
import Polls from './pages/Polls'
import CreatePoll from './pages/CreatePoll'
import PollResults from './pages/PollResults'
import Notifications from './pages/Notifications'
import About from './pages/About'
import Settings from './pages/Settings'
import DecisionDetails from './pages/DecisionDetails'
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
          <Route path="/explore" element={<ExploreDecisions />} />
          <Route path="/my-decisions" element={<MyDecisions />} />
          <Route path="/create-decision" element={<CreateDecision />} />
          <Route path="/compare-options" element={<CompareOptions />} />
          <Route path="/community" element={<Community />} />
          <Route path="/communities" element={<Communities />} />
          <Route path="/communities/:communityId" element={<CommunityDetails />} />
          <Route path="/vote/:decisionId" element={<Vote />} />
          <Route path="/polls" element={<Polls />} />
          <Route path="/polls/create" element={<CreatePoll />} />
          <Route path="/polls/:pollId/results" element={<PollResults />} />
          <Route path="/notifications" element={<Notifications />} />
          <Route path="/about" element={<About />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/settings" element={<Settings />} />
          <Route path="/decision/:decisionId" element={<DecisionDetails />} />
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
