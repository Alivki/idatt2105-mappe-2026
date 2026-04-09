import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const api = axios.create({
  baseURL: '/api/v1',
})

api.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

let refreshPromise: Promise<void> | null = null

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config
    if (error.response?.status !== 401 || original._retry) {
      return Promise.reject(error)
    }

    original._retry = true
    const auth = useAuthStore()
    const refreshToken = auth.getRefreshToken()

    if (!refreshToken) {
      auth.clearAuth()
      return Promise.reject(error)
    }

    if (!refreshPromise) {
      refreshPromise = axios
        .post('/api/v1/auth/refresh', { refreshToken })
        .then(({ data }) => {
          auth.setAuth(data)
        })
        .catch(() => {
          auth.clearAuth()
          window.location.href = '/login'
        })
        .finally(() => {
          refreshPromise = null
        })
    }

    await refreshPromise

    if (auth.accessToken) {
      original.headers.Authorization = `Bearer ${auth.accessToken}`
      return api(original)
    }

    return Promise.reject(error)
  },
)

export default api
