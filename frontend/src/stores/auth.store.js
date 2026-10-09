import { defineStore } from 'pinia'
import axios from 'axios'
import api from '@/services/api'

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
      this.accessToken = access
    },
    clearAuth() {
      this.accessToken = null
      this.user = null
    },
    setUser(userData) {
      this.user = userData
    },
    async initAuth() {
      if (this.initialized) return

      try {
        const { data } = await axios.post(`${api.defaults.baseURL}/auth/refresh-token`, {}, {
          withCredentials: true
        })
        
        if (data.code === 1000) {
          this.accessToken = data.result.accessToken
          
          api.defaults.headers.common['Authorization'] = 'Bearer ' + this.accessToken
          
          const userRes = await api.get('/users/me')
          if (userRes.data.code === 1000) {
            this.user = userRes.data.result
          }
        }
      } catch (error) {
        this.clearAuth()
      } finally {
        this.initialized = true
      }
    }
  }
})
