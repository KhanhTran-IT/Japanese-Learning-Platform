<template>
  <div class="login-page">
    <h2>Đăng nhập</h2>
    
    <div v-if="successMsg" class="success-alert" role="alert">
      {{ successMsg }}
    </div>

    <div v-if="errorMsg" class="error-alert" role="alert">
      {{ errorMsg }}
    </div>

    <form @submit.prevent="handleLogin" class="form">
      <div class="form-group">
        <label>Email</label>
        <input type="email" v-model="email" @input="clearMessages" required placeholder="Nhập email..." />
      </div>
      
      <div class="form-group">
        <label>Mật khẩu</label>
        <input type="password" v-model="password" @input="clearMessages" required placeholder="Nhập mật khẩu..." />
      </div>
      
      <button type="submit" class="btn-submit" :disabled="isLoading">
        {{ isLoading ? 'Đang đăng nhập...' : 'Đăng nhập' }}
      </button>
    </form>
    
    <p class="mt-4">
      Chưa có tài khoản? <router-link to="/register">Đăng ký ngay</router-link>
    </p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { AuthService } from '@/services/auth.service'
import { useAuthStore } from '@/stores/auth.store'
import { getApiErrorMessage } from '@/utils/api-error'

const email = ref('')
const password = ref('')
const errorMsg = ref('')
const successMsg = ref('')
const isLoading = ref(false)

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

onMounted(() => {
  if (route.query.registered === 'success') {
    successMsg.value = 'Đăng ký thành công! Vui lòng đăng nhập.'
    // clean up the URL query
    router.replace({ query: {} })
  }
})

const clearMessages = () => {
  errorMsg.value = ''
  successMsg.value = ''
}

const handleLogin = async () => {
  errorMsg.value = ''
  successMsg.value = ''
  
  if (!email.value.trim() || !password.value) {
    errorMsg.value = 'Vui lòng nhập đầy đủ email và mật khẩu.'
    return
  }

  isLoading.value = true
  
  try {
    const res = await AuthService.login(email.value, password.value)
    
    if (res.data.code === 1000) {
      const { accessToken, refreshToken } = res.data.result
      authStore.setTokens(accessToken, refreshToken)
      
      // fetch user profile
      const userRes = await AuthService.getCurrentUser()
      const userData = userRes.data.result
      authStore.setUser(userData)
      
      // redirect based on role
      const userRoles = userData.roles || []
      if (userRoles.includes('ADMIN') || userRoles.includes('SUPER_ADMIN')) {
        router.push('/admin/dashboard')
      } else {
        const redirectPath = typeof route.query.redirect === 'string' ? route.query.redirect : ''
        const isSafeRedirect = redirectPath.startsWith('/') && !redirectPath.startsWith('//')
        router.push(isSafeRedirect ? redirectPath : '/student/dashboard')
      }
    } else {
      errorMsg.value = res.data.message || 'Đăng nhập thất bại'
    }
  } catch (error) {
    errorMsg.value = getApiErrorMessage(error, 'Thông tin đăng nhập không chính xác.')
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  text-align: left;
}
.login-page h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: var(--primary-color);
}
.error-alert, .success-alert {
  padding: 0.75rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  font-size: 0.875rem;
}
.error-alert {
  background-color: #fef2f2;
  color: #ef4444;
  border: 1px solid #fca5a5;
}
.success-alert {
  background-color: #f0fdf4;
  color: #16a34a;
  border: 1px solid #86efac;
}
.form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.form-group {
  text-align: left;
}
.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  font-size: 0.875rem;
}
.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.2s;
}
.form-group input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}
.btn-submit {
  padding: 0.75rem;
  background-color: var(--primary-color);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  margin-top: 0.5rem;
  transition: background-color 0.2s;
}
.btn-submit:hover:not(:disabled) {
  background-color: #2563eb;
}
.btn-submit:disabled {
  background-color: #93c5fd;
  cursor: not-allowed;
}
.mt-4 {
  margin-top: 1.5rem;
  text-align: center;
  font-size: 0.875rem;
}
a { 
  color: var(--primary-color); 
  font-weight: 500;
}
a:hover {
  text-decoration: underline;
}
</style>
