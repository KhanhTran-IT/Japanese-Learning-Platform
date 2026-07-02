<template>
  <div class="login-page">
    <h2>Đăng nhập</h2>
    <form @submit.prevent="handleLogin" class="form">
      <div class="form-group">
        <label>Email</label>
        <input type="email" v-model="email" required placeholder="nhap email..." />
      </div>
      <div class="form-group">
        <label>Mật khẩu</label>
        <input type="password" v-model="password" required placeholder="nhap mat khau..." />
      </div>
      <button type="submit" class="btn-submit">Đăng nhập</button>
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
const router = useRouter()
const authStore = useAuthStore()

const handleLogin = async () => {
  try {
    const res = await AuthService.login(email.value, password.value)
    if (res.data.code === 1000) {
      const { accessToken, refreshToken } = res.data.result
      authStore.setTokens(accessToken, refreshToken)
      // fetch user profile
      const userRes = await AuthService.getCurrentUser()
      authStore.setUser(userRes.data.result)
      
      router.push('/student/dashboard')
    } else {
      alert(res.data.message)
    }
  } catch (error) {
    alert('Đăng nhập thất bại: ' + (error.response?.data?.message || error.message))
  }
}
</script>

<style scoped>
.form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 1.5rem;
}
.form-group {
  text-align: left;
}
.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}
.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 4px;
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
}
.mt-4 { margin-top: 1rem; }
a { color: var(--primary-color); }
</style>
