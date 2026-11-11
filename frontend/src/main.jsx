import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import App from './App.jsx'
import { AuthProvider } from './context/AuthContext'
import { DecisionProvider } from './context/DecisionContext'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <DecisionProvider>
          <App />
        </DecisionProvider>
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
)
