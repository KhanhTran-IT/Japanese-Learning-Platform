<template>
  <div class="profile-page">
    <div class="page-header">
      <h1 class="page-title">Hồ sơ cá nhân</h1>
      <p class="page-subtitle">Quản lý thông tin tài khoản và bảo mật</p>
    </div>

    <div class="profile-container">
      <!-- Cập nhật thông tin -->
      <section class="profile-section">
        <h2 class="section-title">Thông tin cơ bản</h2>
        
        <form @submit.prevent="updateProfile" class="profile-form">
          <div class="form-group">
            <label>Họ và tên</label>
            <input 
              v-model="profileForm.fullName" 
              type="text" 
              class="form-control" 
              required
              maxlength="150"
            />
          </div>

          <div class="form-group">
            <label>Số điện thoại</label>
            <input 
              v-model="profileForm.phone" 
              type="tel" 
              class="form-control"
              maxlength="30"
            />
          </div>

          <div class="form-group">
            <label>Email (Không thể thay đổi)</label>
            <input 
              :value="authStore.user?.email" 
              type="email" 
              class="form-control" 
              disabled
            />
          </div>

          <div v-if="profileError" class="alert alert-error">
            {{ profileError }}
          </div>
          <div v-if="profileSuccess" class="alert alert-success">
            {{ profileSuccess }}
          </div>

          <div class="form-actions">
            <button 
              type="submit" 
              class="btn-primary" 
              :disabled="isUpdatingProfile"
            >
              <span v-if="isUpdatingProfile" class="spinner-sm"></span>
              {{ isUpdatingProfile ? 'Đang lưu...' : 'Lưu thay đổi' }}
            </button>
          </div>
        </form>
      </section>

      <!-- Đổi mật khẩu -->
      <section class="profile-section">
        <h2 class="section-title">Đổi mật khẩu</h2>
        
        <form @submit.prevent="changePassword" class="profile-form">
          <div class="form-group">
            <label>Mật khẩu hiện tại</label>
            <input 
              v-model="passwordForm.currentPassword" 
              type="password" 
              class="form-control" 
              required
            />
          </div>

          <div class="form-group">
            <label>Mật khẩu mới</label>
            <input 
              v-model="passwordForm.newPassword" 
              type="password" 
              class="form-control" 
              required
              minlength="6"
              maxlength="100"
            />
            <small class="form-text">Mật khẩu phải từ 6 đến 100 ký tự.</small>
          </div>

          <div class="form-group">
            <label>Xác nhận mật khẩu mới</label>
            <input 
              v-model="passwordForm.confirmPassword" 
              type="password" 
              class="form-control" 
              required
              minlength="6"
              maxlength="100"
            />
          </div>

          <div v-if="passwordError" class="alert alert-error">
            {{ passwordError }}
          </div>
          <div v-if="passwordSuccess" class="alert alert-success">
            {{ passwordSuccess }}
          </div>

          <div class="form-actions">
            <button 
              type="submit" 
              class="btn-primary" 
              :disabled="isChangingPassword"
            >
              <span v-if="isChangingPassword" class="spinner-sm"></span>
              {{ isChangingPassword ? 'Đang cập nhật...' : 'Đổi mật khẩu' }}
            </button>
          </div>
        </form>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { AuthService } from '@/services/auth.service'
import { getApiErrorMessage } from '@/utils/api-error'

const authStore = useAuthStore()

// --- Profile State ---
const profileForm = reactive({
  fullName: '',
  phone: '',
  avatarUrl: ''
})
const isUpdatingProfile = ref(false)
const profileError = ref('')
const profileSuccess = ref('')

// --- Password State ---
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const isChangingPassword = ref(false)
const passwordError = ref('')
const passwordSuccess = ref('')

onMounted(() => {
  if (authStore.user) {
    profileForm.fullName = authStore.user.fullName || ''
    profileForm.phone = authStore.user.phone || ''
    profileForm.avatarUrl = authStore.user.avatarUrl || ''
  }
})

const updateProfile = async () => {
  isUpdatingProfile.value = true
  profileError.value = ''
  profileSuccess.value = ''
  
  try {
    const res = await AuthService.updateCurrentUser(profileForm)
    if (res.data && res.data.code === 1000) {
      profileSuccess.value = 'Cập nhật thông tin thành công!'
      // Cập nhật store để UI đồng bộ (header, etc.)
      authStore.setUser(res.data.result)
    }
  } catch (error) {
    profileError.value = getApiErrorMessage(error) || 'Đã xảy ra lỗi khi cập nhật thông tin.'
  } finally {
    isUpdatingProfile.value = false
  }
}

const changePassword = async () => {
  isChangingPassword.value = true
  passwordError.value = ''
  passwordSuccess.value = ''
  
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    passwordError.value = 'Xác nhận mật khẩu không khớp.'
    isChangingPassword.value = false
    return
  }
  
  try {
    const res = await AuthService.changePassword(passwordForm)
    if (res.data && res.data.code === 1000) {
      passwordSuccess.value = 'Đổi mật khẩu thành công!'
      // Reset form (không log/giữ password)
      passwordForm.currentPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    }
  } catch (error) {
    passwordError.value = getApiErrorMessage(error) || 'Đã xảy ra lỗi khi đổi mật khẩu.'
  } finally {
    isChangingPassword.value = false
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.page-header {
  margin-bottom: 2rem;
}

.page-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 0.5rem;
}

.page-subtitle {
  color: #64748b;
  margin: 0;
}

.profile-container {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.profile-section {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #0f172a;
  margin: 0 0 1.5rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #e2e8f0;
}

.profile-form {
  max-width: 500px;
}

.form-group {
  margin-bottom: 1.25rem;
}

.form-group label {
  display: block;
  font-size: 0.9rem;
  font-weight: 500;
  color: #334155;
  margin-bottom: 0.5rem;
}

.form-control {
  width: 100%;
  padding: 0.6rem 0.75rem;
  font-size: 0.95rem;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-control:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-control:disabled {
  background-color: #f1f5f9;
  cursor: not-allowed;
  color: #64748b;
}

.form-text {
  display: block;
  font-size: 0.8rem;
  color: #64748b;
  margin-top: 0.25rem;
}

.alert {
  padding: 0.75rem 1rem;
  border-radius: 6px;
  margin-bottom: 1.25rem;
  font-size: 0.9rem;
}

.alert-error {
  background-color: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
}

.alert-success {
  background-color: #f0fdf4;
  color: #15803d;
  border: 1px solid #bbf7d0;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.6rem 1.2rem;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-primary:hover:not(:disabled) {
  background-color: #2563eb;
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner-sm {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
