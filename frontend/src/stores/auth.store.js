import { defineStore } from 'pinia'
import axios from 'axios'
import api from '@/services/api'
import {
  runWithRefreshLock,
  onSignal,
  notifyLogout,
  AUTH_SIGNALS
} from '@/services/auth-coordinator'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: null,
    user: null,
    initialized: false
  }),
  getters: {
    isAuthenticated: (state) => !!state.accessToken,
  },
  actions: {
    setTokens(access) {
      this.accessToken = access || null
      if (this.accessToken) {
        if (api.defaults?.headers?.common) {
          api.defaults.headers.common['Authorization'] = 'Bearer ' + this.accessToken
        }
      } else {
        if (api.defaults?.headers?.common) {
          delete api.defaults.headers.common['Authorization']
          delete api.defaults.headers.common.Authorization
        }
        if (api.defaults?.headers) {
          delete api.defaults.headers['Authorization']
          delete api.defaults.headers.Authorization
        }
      }
    },
    clearAuth(options = { broadcast: false }) {
      this.accessToken = null
      this.user = null
      if (api.defaults?.headers?.common) {
        delete api.defaults.headers.common['Authorization']
        delete api.defaults.headers.common.Authorization
      }
      if (api.defaults?.headers) {
        delete api.defaults.headers['Authorization']
        delete api.defaults.headers.Authorization
      }
      if (options?.broadcast) {
        notifyLogout()
      }
    },
    setUser(userData) {
      this.user = userData
    },
    async initAuth() {
      if (this.initialized) return
      if (this._initPromise) return this._initPromise

      this._initPromise = (async () => {
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
          
          if (data?.code === 1000 && data.result?.accessToken) {
            this.setTokens(data.result.accessToken)
            
            try {
              const userRes = await api.get('/users/me')
              if (userRes.data?.code === 1000) {
                this.user = userRes.data.result
              } else {
                this.clearAuth()
              }
            } catch {
              this.clearAuth()
            }
          } else {
            this.clearAuth()
          }
        } catch (error) {
          this.clearAuth()
        } finally {
          this.initialized = true
          this._initPromise = null
        }
      })()

      return this._initPromise
    }
  }
})

// Listen for cross-tab session signals
onSignal(async (signal) => {
  try {
    const store = useAuthStore()
    if (signal.type === AUTH_SIGNALS.LOGIN) {
      if (!store.isAuthenticated) {
        await store.initAuth()
      }
    }
  } catch {
    // In environments where active Pinia is not yet initialized
  }
})
