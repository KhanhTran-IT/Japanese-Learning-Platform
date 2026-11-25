import axios from 'axios'
import { useAuthStore } from '@/stores/auth.store'
import {
  runWithRefreshLock,
  onSignal,
  AUTH_SIGNALS,
  _resetCoordinatorState
} from './auth-coordinator'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  withCredentials: true
})

let isRefreshing = false
let failedQueue = []

export const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

export const _resetRefreshState = () => {
  isRefreshing = false
  failedQueue = []
  _resetCoordinatorState()
}

let redirectHandler = () => {
  if (typeof window !== 'undefined' && window.location) {
    if (window.location.pathname !== '/login') {
      try {
        window.location.href = '/login'
      } catch {
        // Safe fallback in restricted/test environments
      }
    }
  }
}

export const setRedirectHandler = (fn) => {
  redirectHandler = fn
}

export const resetRedirectHandler = () => {
  redirectHandler = () => {
    if (typeof window !== 'undefined' && window.location) {
      if (window.location.pathname !== '/login') {
        try {
          window.location.href = '/login'
        } catch {}
      }
    }
  }
}

const setHeader = (target, name, value) => {
  if (!target) return
  if (typeof target.headers?.set === 'function') {
    target.headers.set(name, value)
  } else {
    if (!target.headers) target.headers = {}
    target.headers[name] = value
  }
}

const removeHeader = (target, name) => {
  if (!target?.headers) return
  if (typeof target.headers.delete === 'function') {
    target.headers.delete(name)
  }
  delete target.headers[name]
  delete target.headers[name.toLowerCase()]
}

api.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.accessToken) {
      setHeader(config, 'Authorization', `Bearer ${authStore.accessToken}`)
    } else {
      removeHeader(config, 'Authorization')
    }
    return config
  },
  (error) => Promise.reject(error)
)

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error?.config
    if (!originalRequest) {
      return Promise.reject(error)
    }

    // Public auth endpoints should never trigger token refresh.
    // Let the original backend error pass through.
    const publicAuthPaths = ['/auth/login', '/auth/register', '/auth/refresh-token', '/auth/logout']
    const requestUrl = originalRequest.url || ''
    const isPublicAuth = publicAuthPaths.some(path => requestUrl.includes(path))

    if (error.response?.status === 401 && !originalRequest._retry && !isPublicAuth) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(token => {
          originalRequest._retry = true
          setHeader(originalRequest, 'Authorization', 'Bearer ' + token)
          return api(originalRequest)
        }).catch(err => {
          return Promise.reject(err)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      const authStore = useAuthStore()
      try {
        const baseURL = api.defaults?.baseURL || import.meta.env.VITE_API_BASE_URL || '/api'
        const data = await runWithRefreshLock(async () => {
          const res = await axios.post(`${baseURL}/auth/refresh-token`, {}, {
            withCredentials: true
          })
          
          if (res.data?.code !== 1000 || !res.data?.result?.accessToken) {
            throw new Error(res.data?.message || 'Refresh token failed')
          }
          return res.data
        })

        const newAccessToken = data.result.accessToken
        
        authStore.setTokens(newAccessToken)
        
        setHeader(originalRequest, 'Authorization', 'Bearer ' + newAccessToken)
        
        processQueue(null, newAccessToken)
        return api(originalRequest)
      } catch (err) {
        processQueue(err, null)
        authStore.clearAuth()
        redirectHandler()
        return Promise.reject(err)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

// Synchronize session termination across browser tabs
onSignal((signal) => {
  if (signal.type === AUTH_SIGNALS.LOGOUT || signal.type === AUTH_SIGNALS.REFRESH_FAILURE) {
    const authStore = useAuthStore()
    const hadAuth = !!(authStore.accessToken || authStore.user)
    if (hadAuth) {
      authStore.clearAuth()
      processQueue(new Error('Session terminated across tabs'), null)
      redirectHandler()
    }
  }
})

export default api
