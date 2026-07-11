import api from './api'

export const AuthService = {
  login(email, password) {
    return api.post('/auth/login', { email, password })
  },
  register(email, password, confirmPassword, fullName) {
    return api.post('/auth/register', { email, password, confirmPassword, fullName })
  },
  getCurrentUser() {
    return api.get('/users/me')
  }
}
