<template>
  <div class="max-w-4xl mx-auto">
    <div class="mb-10">
      <h1 class="font-headline-lg text-headline-lg-mobile md:text-headline-lg text-ink-black mb-2">Hồ sơ cá nhân</h1>
      <p class="font-body-md text-secondary">Quản lý thông tin tài khoản và bảo mật</p>
    </div>

    <div class="space-y-8">
      <!-- Cập nhật thông tin -->
      <section class="zen-card p-8 md:p-10 rounded-[24px]">
        <h2 class="font-headline-md text-xl text-ink-black mb-6 flex items-center gap-3">
          <span class="material-symbols-outlined text-primary">person</span>
          Thông tin cơ bản
        </h2>
        
        <form @submit.prevent="updateProfile" class="max-w-xl space-y-5">
          <div>
            <label class="block font-label-sm text-secondary uppercase tracking-wider mb-2">Họ và tên</label>
            <input 
              v-model="profileForm.fullName" 
              type="text" 
              class="w-full px-4 py-3 rounded-xl border border-outline-variant bg-surface-container-lowest focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all font-body-md text-ink-black outline-none" 
              required
              maxlength="150"
            />
          </div>

          <div>
            <label class="block font-label-sm text-secondary uppercase tracking-wider mb-2">Số điện thoại</label>
            <input 
              v-model="profileForm.phone" 
              type="tel" 
              class="w-full px-4 py-3 rounded-xl border border-outline-variant bg-surface-container-lowest focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all font-body-md text-ink-black outline-none"
              maxlength="30"
            />
          </div>

          <div>
            <label class="block font-label-sm text-secondary uppercase tracking-wider mb-2">Email <span class="normal-case text-xs text-outline-variant">(Không thể thay đổi)</span></label>
            <input 
              :value="authStore.user?.email" 
              type="email" 
              class="w-full px-4 py-3 rounded-xl border border-transparent bg-surface-container-low text-secondary font-body-md cursor-not-allowed outline-none" 
              disabled
            />
          </div>

          <div v-if="profileError" class="bg-error-container/50 text-error px-4 py-3 rounded-xl font-body-md border border-error/20 flex items-center gap-3">
            <span class="material-symbols-outlined text-xl">error</span>
            {{ profileError }}
          </div>
          <div v-if="profileSuccess" class="bg-success-green/10 text-success-green px-4 py-3 rounded-xl font-body-md border border-success-green/20 flex items-center gap-3">
            <span class="material-symbols-outlined text-xl">check_circle</span>
            {{ profileSuccess }}
          </div>

          <div class="pt-4">
            <button 
              type="submit" 
              class="bg-primary hover:bg-primary-container text-white hover:text-on-primary-container px-8 py-3 rounded-xl font-button transition-all shadow-md hover:shadow-lg active:scale-95 flex items-center justify-center gap-2 min-w-[160px]" 
              :disabled="isUpdatingProfile"
              :class="{ 'opacity-70 cursor-not-allowed': isUpdatingProfile }"
            >
              <span v-if="isUpdatingProfile" class="material-symbols-outlined animate-spin text-[20px]">autorenew</span>
              {{ isUpdatingProfile ? 'Đang lưu...' : 'Lưu thay đổi' }}
            </button>
          </div>
        </form>
      </section>

      <!-- Đổi mật khẩu -->
      <section class="zen-card p-8 md:p-10 rounded-[24px]">
        <h2 class="font-headline-md text-xl text-ink-black mb-6 flex items-center gap-3">
          <span class="material-symbols-outlined text-primary">lock</span>
          Đổi mật khẩu
        </h2>
        
        <form @submit.prevent="changePassword" class="max-w-xl space-y-5">
          <div>
            <label class="block font-label-sm text-secondary uppercase tracking-wider mb-2">Mật khẩu hiện tại</label>
            <input 
              v-model="passwordForm.currentPassword" 
              type="password" 
              class="w-full px-4 py-3 rounded-xl border border-outline-variant bg-surface-container-lowest focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all font-body-md text-ink-black outline-none" 
              required
            />
          </div>

          <div>
            <label class="block font-label-sm text-secondary uppercase tracking-wider mb-2">Mật khẩu mới</label>
            <input 
              v-model="passwordForm.newPassword" 
              type="password" 
              class="w-full px-4 py-3 rounded-xl border border-outline-variant bg-surface-container-lowest focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all font-body-md text-ink-black outline-none" 
              required
              minlength="6"
              maxlength="100"
            />
            <p class="font-label-sm text-secondary mt-2">Mật khẩu phải từ 6 đến 100 ký tự.</p>
          </div>

          <div>
            <label class="block font-label-sm text-secondary uppercase tracking-wider mb-2">Xác nhận mật khẩu mới</label>
            <input 
              v-model="passwordForm.confirmPassword" 
              type="password" 
              class="w-full px-4 py-3 rounded-xl border border-outline-variant bg-surface-container-lowest focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all font-body-md text-ink-black outline-none" 
              required
              minlength="6"
              maxlength="100"
            />
          </div>

          <div v-if="passwordError" class="bg-error-container/50 text-error px-4 py-3 rounded-xl font-body-md border border-error/20 flex items-center gap-3">
            <span class="material-symbols-outlined text-xl">error</span>
            {{ passwordError }}
          </div>
          <div v-if="passwordSuccess" class="bg-success-green/10 text-success-green px-4 py-3 rounded-xl font-body-md border border-success-green/20 flex items-center gap-3">
            <span class="material-symbols-outlined text-xl">check_circle</span>
            {{ passwordSuccess }}
          </div>

          <div class="pt-4">
            <button 
              type="submit" 
              class="bg-surface-container-highest hover:bg-outline-variant text-on-surface px-8 py-3 rounded-xl font-button transition-all border border-outline-variant active:scale-95 flex items-center justify-center gap-2 min-w-[160px]" 
              :disabled="isChangingPassword"
              :class="{ 'opacity-70 cursor-not-allowed': isChangingPassword }"
            >
              <span v-if="isChangingPassword" class="material-symbols-outlined animate-spin text-[20px]">autorenew</span>
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
