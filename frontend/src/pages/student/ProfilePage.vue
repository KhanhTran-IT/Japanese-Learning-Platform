<template>
  <div class="max-w-5xl mx-auto space-y-8">
    <!-- Header Banner -->
    <div class="bg-stitch-foreground text-white rounded-[24px] overflow-hidden shadow-lg">
      <div class="p-6 md:p-10">
        <div class="flex items-center gap-6">
          <div class="relative group">
            <div class="w-20 h-20 md:w-24 md:h-24 rounded-full bg-gradient-to-br from-stitch-primary to-stitch-accent flex items-center justify-center text-3xl font-bold border-4 border-white/10 shrink-0 overflow-hidden">
              <img v-if="authStore.user?.avatarUrl" :src="authStore.user.avatarUrl" alt="Avatar" loading="lazy" decoding="async" class="w-full h-full object-cover" />
              <span v-else>{{ userInitials }}</span>
            </div>
            <!-- Coming soon feature (Change Avatar) -->
            <button class="absolute bottom-0 right-0 w-8 h-8 bg-white rounded-full flex items-center justify-center text-sm shadow-lg hover:bg-stitch-muted transition-colors opacity-0 group-hover:opacity-100" title="Chưa hỗ trợ thay đổi ảnh đại diện">
              ✏️
            </button>
          </div>
          <div class="min-w-0">
            <h1 class="text-2xl md:text-3xl font-stitch-serif font-bold mb-1 truncate">{{ authStore.user?.fullName }}</h1>
            <p class="text-white/60 text-sm md:text-base truncate">{{ authStore.user?.email }}</p>
            <div class="flex items-center gap-3 mt-3">
              <span class="text-xs bg-white/10 px-3 py-1 rounded-full font-medium">Học viên</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content Grid -->
    <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
      <!-- Sidebar Navigation -->
      <div class="lg:col-span-1">
        <nav class="bg-stitch-card rounded-[24px] border border-stitch-border overflow-hidden sticky top-24">
          <button
            @click="activeTab = 'info'"
            class="w-full flex items-center gap-3 px-5 py-4 text-sm text-left border-b border-stitch-border transition-colors outline-none"
            :class="activeTab === 'info' ? 'bg-stitch-primary/5 text-stitch-primary font-bold border-l-4 border-l-stitch-primary' : 'hover:bg-stitch-muted text-stitch-foreground border-l-4 border-l-transparent'"
          >
            <span class="text-lg">👤</span>
            <span>Thông tin cá nhân</span>
          </button>
          <button
            @click="activeTab = 'password'"
            class="w-full flex items-center gap-3 px-5 py-4 text-sm text-left border-b border-stitch-border transition-colors outline-none"
            :class="activeTab === 'password' ? 'bg-stitch-primary/5 text-stitch-primary font-bold border-l-4 border-l-stitch-primary' : 'hover:bg-stitch-muted text-stitch-foreground border-l-4 border-l-transparent'"
          >
            <span class="text-lg">🔒</span>
            <span>Bảo mật</span>
          </button>
          <button
            @click="handleLogout"
            class="w-full flex items-center gap-3 px-5 py-4 text-sm text-red-500 hover:bg-red-50 transition-colors outline-none border-l-4 border-l-transparent"
          >
            <span class="text-lg">🚪</span>
            <span class="font-medium">Đăng xuất</span>
          </button>
        </nav>
      </div>

      <!-- Tab Content -->
      <div class="lg:col-span-3">
        <!-- Tab: Info -->
        <div v-if="activeTab === 'info'" class="bg-stitch-card rounded-[24px] border border-stitch-border p-6 md:p-8">
          <h2 class="font-stitch-serif font-bold text-xl text-stitch-foreground mb-6">Thông tin cá nhân</h2>
          
          <form @submit.prevent="updateProfile" class="space-y-6">
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-5">
              <div>
                <label class="block text-sm font-medium text-stitch-foreground mb-2">Họ và tên <span class="text-red-500">*</span></label>
                <input 
                  v-model="profileForm.fullName" 
                  type="text" 
                  class="w-full px-4 py-2.5 border border-stitch-border rounded-xl text-sm bg-stitch-background focus:outline-none focus:border-stitch-primary focus:ring-1 focus:ring-stitch-primary transition-all text-stitch-foreground" 
                  required
                  maxlength="150"
                  placeholder="Nhập họ và tên"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-stitch-foreground mb-2">Số điện thoại</label>
                <input 
                  v-model="profileForm.phone" 
                  type="tel" 
                  class="w-full px-4 py-2.5 border border-stitch-border rounded-xl text-sm bg-stitch-background focus:outline-none focus:border-stitch-primary focus:ring-1 focus:ring-stitch-primary transition-all text-stitch-foreground"
                  maxlength="30"
                  placeholder="Nhập số điện thoại"
                />
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-stitch-foreground mb-2">Email</label>
              <input 
                :value="authStore.user?.email" 
                type="email" 
                class="w-full px-4 py-2.5 border border-transparent rounded-xl text-sm bg-stitch-muted text-stitch-muted-foreground cursor-not-allowed outline-none" 
                disabled
              />
              <p class="text-xs text-stitch-muted-foreground mt-2">Email dùng để đăng nhập và không thể thay đổi.</p>
            </div>

            <!-- Alerts -->
            <div v-if="profileError" class="bg-red-50 text-red-600 px-4 py-3 rounded-xl text-sm font-medium border border-red-100 flex items-center gap-2">
              <span class="material-symbols-outlined text-xl">error</span>
              {{ profileError }}
            </div>
            <div v-if="profileSuccess" class="bg-green-50 text-green-700 px-4 py-3 rounded-xl text-sm font-medium border border-green-100 flex items-center gap-2">
              <span class="material-symbols-outlined text-xl">check_circle</span>
              {{ profileSuccess }}
            </div>

            <div class="flex items-center justify-end pt-6 border-t border-stitch-border">
              <button 
                type="submit" 
                class="px-6 py-2.5 bg-stitch-primary text-white text-sm font-semibold rounded-xl hover:bg-stitch-primary/90 transition-all active:scale-95 flex items-center gap-2 min-w-[140px] justify-center" 
                :disabled="isUpdatingProfile"
                :class="{ 'opacity-70 cursor-not-allowed': isUpdatingProfile }"
              >
                <span v-if="isUpdatingProfile" class="material-symbols-outlined animate-spin text-[18px]">autorenew</span>
                {{ isUpdatingProfile ? 'Đang lưu...' : 'Lưu thay đổi' }}
              </button>
            </div>
          </form>
        </div>

        <!-- Tab: Password -->
        <div v-if="activeTab === 'password'" class="bg-stitch-card rounded-[24px] border border-stitch-border p-6 md:p-8">
          <h2 class="font-stitch-serif font-bold text-xl text-stitch-foreground mb-6">Bảo mật tài khoản</h2>
          
          <form @submit.prevent="changePassword" class="space-y-5 max-w-lg">
            <div>
              <label class="block text-sm font-medium text-stitch-foreground mb-2">Mật khẩu hiện tại <span class="text-red-500">*</span></label>
              <input 
                v-model="passwordForm.currentPassword" 
                type="password" 
                class="w-full px-4 py-2.5 border border-stitch-border rounded-xl text-sm bg-stitch-background focus:outline-none focus:border-stitch-primary focus:ring-1 focus:ring-stitch-primary transition-all text-stitch-foreground" 
                required
                placeholder="••••••••"
                autocomplete="current-password"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-stitch-foreground mb-2">Mật khẩu mới <span class="text-red-500">*</span></label>
              <input 
                v-model="passwordForm.newPassword" 
                type="password" 
                class="w-full px-4 py-2.5 border border-stitch-border rounded-xl text-sm bg-stitch-background focus:outline-none focus:border-stitch-primary focus:ring-1 focus:ring-stitch-primary transition-all text-stitch-foreground" 
                required
                minlength="6"
                maxlength="100"
                placeholder="Tối thiểu 6 ký tự"
                autocomplete="new-password"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-stitch-foreground mb-2">Xác nhận mật khẩu mới <span class="text-red-500">*</span></label>
              <input 
                v-model="passwordForm.confirmPassword" 
                type="password" 
                class="w-full px-4 py-2.5 border border-stitch-border rounded-xl text-sm bg-stitch-background focus:outline-none focus:border-stitch-primary focus:ring-1 focus:ring-stitch-primary transition-all text-stitch-foreground" 
                required
                minlength="6"
                maxlength="100"
                placeholder="Nhập lại mật khẩu mới"
                autocomplete="new-password"
              />
            </div>

            <!-- Alerts -->
            <div v-if="passwordError" class="bg-red-50 text-red-600 px-4 py-3 rounded-xl text-sm font-medium border border-red-100 flex items-center gap-2">
              <span class="material-symbols-outlined text-xl">error</span>
              {{ passwordError }}
            </div>
            <div v-if="passwordSuccess" class="bg-green-50 text-green-700 px-4 py-3 rounded-xl text-sm font-medium border border-green-100 flex items-center gap-2">
              <span class="material-symbols-outlined text-xl">check_circle</span>
              {{ passwordSuccess }}
            </div>

            <div class="pt-4">
              <button 
                type="submit" 
                class="px-6 py-2.5 bg-stitch-foreground text-white text-sm font-semibold rounded-xl hover:bg-stitch-foreground/90 transition-all active:scale-95 flex items-center gap-2" 
                :disabled="isChangingPassword"
                :class="{ 'opacity-70 cursor-not-allowed': isChangingPassword }"
              >
                <span v-if="isChangingPassword" class="material-symbols-outlined animate-spin text-[18px]">autorenew</span>
                {{ isChangingPassword ? 'Đang cập nhật...' : 'Đổi mật khẩu' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { AuthService } from '@/services/auth.service'
import { getApiErrorMessage } from '@/utils/api-error'

const router = useRouter()
const authStore = useAuthStore()
const activeTab = ref('info')

const userInitials = computed(() => {
  const name = authStore.user?.fullName || 'U'
  return name.substring(0, 2).toUpperCase()
})

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
      authStore.setUser(res.data.result)
      
      // Auto clear success message after 3s
      setTimeout(() => {
        profileSuccess.value = ''
      }, 3000)
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
    const res = await AuthService.changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    if (res.data && res.data.code === 1000) {
      passwordSuccess.value = 'Đổi mật khẩu thành công!'
      // Reset form
      passwordForm.currentPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      
      setTimeout(() => {
        passwordSuccess.value = ''
      }, 3000)
    }
  } catch (error) {
    profileError.value = '' // Clear if previous profile error
    passwordError.value = getApiErrorMessage(error) || 'Đã xảy ra lỗi khi đổi mật khẩu.'
  } finally {
    isChangingPassword.value = false
  }
}

const handleLogout = async () => {
  try {
    await AuthService.logout()
  } catch (error) {
    console.error('Logout error:', error)
  } finally {
    authStore.clearAuth()
    router.push('/login')
  }
}
</script>
