<template>
  <div class="login-page">
    <h2>Đăng nhập</h2>
    
    <div v-if="errorMsg" class="error-alert">
      {{ errorMsg }}
    </div>

    <form @submit.prevent="handleLogin" class="form">
      <div class="form-group">
        <label>Email</label>
        <input type="email" v-model="email" required placeholder="Nhập email..." />
      </div>
      
      <div class="form-group">
        <label>Mật khẩu</label>
        <input type="password" v-model="password" required placeholder="Nhập mật khẩu..." />
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { AuthService } from '@/services/auth.service'
import { useAuthStore } from '@/stores/auth.store'

const email = ref('')
const password = ref('')
const errorMsg = ref('')
const isLoading = ref(false)

const router = useRouter()
const authStore = useAuthStore()

const handleLogin = async () => {
  errorMsg.value = ''
  
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
      authStore.setUser(userRes.data.result)
      
      // redirect based on role (could be enhanced later to check userRes roles)
      router.push('/student/dashboard')
    } else {
      errorMsg.value = res.data.message || 'Đăng nhập thất bại'
    }
  } catch (error) {
    if (error.response && error.response.data) {
      errorMsg.value = error.response.data.message || 'Thông tin đăng nhập không chính xác.'
    } else {
      errorMsg.value = 'Không thể kết nối đến máy chủ.'
    }
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
.error-alert {
  background-color: #fef2f2;
  color: #ef4444;
  padding: 0.75rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  border: 1px solid #fca5a5;
  font-size: 0.875rem;
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
