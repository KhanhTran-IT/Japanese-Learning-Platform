<template>
  <div class="min-h-screen bg-stitch-background flex items-center justify-center p-4 font-stitch-sans">
    <div class="max-w-md w-full bg-surface-container-lowest border border-paper-shadow rounded-2xl p-8 text-center shadow-lg">
      <!-- Icon -->
      <div class="w-16 h-16 mx-auto mb-5 rounded-2xl bg-error/10 text-error flex items-center justify-center">
        <span class="material-symbols-outlined text-4xl">gpp_maybe</span>
      </div>

      <!-- Badge -->
      <div class="inline-block px-3 py-1 rounded-full text-xs font-semibold bg-error/10 text-error mb-3 uppercase tracking-wider">
        Mã lỗi 403 - Forbidden
      </div>

      <!-- Title -->
      <h1 class="text-2xl font-bold font-stitch-serif text-ink-black mb-3">
        Truy cập bị từ chối
      </h1>

      <!-- Description -->
      <p class="text-secondary text-sm mb-6 leading-relaxed">
        Tài khoản của bạn không có quyền hạn cần thiết để truy cập vào tài nguyên này. 
        Vui lòng quay lại hoặc liên hệ quản trị viên nếu bạn nghĩ đây là sự nhầm lẫn.
      </p>

      <!-- Current Role Info -->
      <div v-if="userRolesText" class="p-3 rounded-xl bg-surface-container-low border border-paper-shadow text-xs text-secondary mb-6 flex items-center justify-between">
        <span>Vai trò hiện tại:</span>
        <span class="font-semibold text-ink-black">{{ userRolesText }}</span>
      </div>

      <!-- Actions -->
      <div class="flex flex-col sm:flex-row gap-3">
        <button
          @click="goToDashboard"
          class="btn-dashboard flex-1 py-2.5 px-4 rounded-xl bg-primary text-on-primary font-medium text-sm hover:opacity-90 transition-opacity flex items-center justify-center gap-2 shadow"
        >
          <span class="material-symbols-outlined text-[18px]">dashboard</span>
          Về trang của bạn
        </button>
        <button
          @click="goToHome"
          class="btn-home flex-1 py-2.5 px-4 rounded-xl border border-paper-shadow bg-surface-container-lowest text-secondary font-medium text-sm hover:bg-surface-container-low transition-colors flex items-center justify-center gap-2"
        >
          <span class="material-symbols-outlined text-[18px]">home</span>
          Trang chủ
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { getDefaultRouteForRoles } from '@/router/guards'

const router = useRouter()
const authStore = useAuthStore()

const userRoles = computed(() => authStore.user?.roles || [])

const userRolesText = computed(() => {
  const roles = userRoles.value
  if (!roles || roles.length === 0) return 'Khách (Chưa đăng nhập)'
  
  const roleNames = {
    'SUPER_ADMIN': 'Quản trị viên cấp cao (Super Admin)',
    'ADMIN': 'Quản trị viên (Admin)',
    'TEACHER': 'Giảng viên (Teacher)',
    'STUDENT': 'Học viên (Student)',
    'CONTENT_EDITOR': 'Biên tập viên (Content Editor)'
  }
  return roles.map(r => roleNames[r] || r).join(', ')
})

const goToDashboard = () => {
  const target = getDefaultRouteForRoles(userRoles.value)
  router.push(target)
}

const goToHome = () => {
  router.push('/')
}
</script>
