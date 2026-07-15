<template>
  <div class="register-page">
    <h2>Đăng ký tài khoản</h2>
    
    <div v-if="errorMsg" class="error-alert">
      {{ errorMsg }}
    </div>

    <form @submit.prevent="handleRegister" class="form">
      <div class="form-group">
        <label>Họ và tên</label>
        <input type="text" v-model="form.fullName" required placeholder="Nhập họ và tên..." />
      </div>

      <div class="form-group">
        <label>Email</label>
        <input type="email" v-model="form.email" required placeholder="Nhập email..." />
      </div>

      <div class="form-group">
        <label>Mật khẩu</label>
        <input type="password" v-model="form.password" required placeholder="Nhập mật khẩu (tối thiểu 8 ký tự)..." />
      </div>

      <div class="form-group">
        <label>Xác nhận mật khẩu</label>
        <input type="password" v-model="form.confirmPassword" required placeholder="Nhập lại mật khẩu..." />
      </div>

      <button type="submit" class="btn-submit" :disabled="isLoading">
        {{ isLoading ? 'Đang xử lý...' : 'Đăng ký' }}
      </button>
    </form>

    <p class="mt-4">
      Đã có tài khoản? <router-link to="/login">Đăng nhập</router-link>
    </p>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { AuthService } from '@/services/auth.service'
import { getApiErrorMessage } from '@/utils/api-error'

const router = useRouter()

const form = reactive({
  fullName: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const errorMsg = ref('')
const isLoading = ref(false)

const validateForm = () => {
  errorMsg.value = ''
  
  if (!form.fullName.trim() || !form.email.trim() || !form.password || !form.confirmPassword) {
    errorMsg.value = 'Vui lòng điền đầy đủ thông tin.'
    return false
  }
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(form.email)) {
    errorMsg.value = 'Email không hợp lệ.'
    return false
  }
  
  if (form.password.length < 8) {
    errorMsg.value = 'Mật khẩu phải có tối thiểu 8 ký tự.'
    return false
  }
  
  if (form.password !== form.confirmPassword) {
    errorMsg.value = 'Mật khẩu xác nhận không khớp.'
    return false
  }
  
  return true
}

const handleRegister = async () => {
  if (!validateForm()) return
  
  isLoading.value = true
  errorMsg.value = ''
  
  try {
    const res = await AuthService.register(form.email, form.password, form.confirmPassword, form.fullName)
    if (res.data.code === 1000) {
      alert('Đăng ký thành công! Vui lòng đăng nhập.')
      router.push('/login')
    } else {
      errorMsg.value = res.data.message || 'Đăng ký thất bại'
    }
  } catch (error) {
    errorMsg.value = getApiErrorMessage(error, 'Đăng ký thất bại. Vui lòng thử lại sau.')
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.register-page {
  text-align: left;
}
.register-page h2 {
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
