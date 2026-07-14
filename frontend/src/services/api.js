import axios from 'axios'
import { useAuthStore } from '@/stores/auth.store'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

let isRefreshing = false
let failedQueue = []

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

api.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.accessToken) {
      config.headers.Authorization = `Bearer ${authStore.accessToken}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // Public auth endpoints should never trigger token refresh.
    // Let the original backend error (e.g. "Email hoặc mật khẩu không đúng") pass through.
    const publicAuthPaths = ['/auth/login', '/auth/register', '/auth/refresh-token', '/auth/logout']
    const requestUrl = originalRequest?.url || ''
    const isPublicAuth = publicAuthPaths.some(path => requestUrl.includes(path))

    if (error.response?.status === 401 && !originalRequest._retry && !isPublicAuth) {
      if (isRefreshing) {
        return new Promise(function(resolve, reject) {
          failedQueue.push({ resolve, reject })
        }).then(token => {
          originalRequest.headers.Authorization = 'Bearer ' + token
          return api(originalRequest)
        }).catch(err => {
          return Promise.reject(err)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      const authStore = useAuthStore()
      try {
        const { data } = await axios.post(`${api.defaults.baseURL}/auth/refresh-token`, {
          refreshToken: authStore.refreshToken
        })
        
        const newAccessToken = data.result.accessToken
        // Preserve existing refresh token if backend doesn't return a new one
        const newRefreshToken = data.result.refreshToken || authStore.refreshToken
        
        authStore.setTokens(newAccessToken, newRefreshToken)
        
        api.defaults.headers.common['Authorization'] = 'Bearer ' + newAccessToken
        originalRequest.headers.Authorization = 'Bearer ' + newAccessToken
        
        processQueue(null, newAccessToken)
        return api(originalRequest)
      } catch (err) {
        processQueue(err, null)
        authStore.clearAuth()
        window.location.href = '/login'
        return Promise.reject(err)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default api
