
import axios from 'axios'

let authToken = null

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  timeout: 10000,
  withCredentials: true,
  withXSRFToken: true,
})

export const isDemoMode = import.meta.env.VITE_AUTH_MODE === 'demo'

export const setAuthToken = (token) => {
  authToken = token || null
}

api.interceptors.request.use((config) => {
  if (authToken) {
    config.headers.Authorization = `Bearer ${authToken}`
  }

  return config
})

export default api
