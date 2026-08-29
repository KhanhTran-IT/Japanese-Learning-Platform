<template>
  <div class="h-screen flex flex-col bg-background font-body-md text-on-surface">
    <!-- Topbar (Minimal) -->
    <header class="h-16 bg-surface-container-lowest border-b border-paper-shadow flex items-center justify-between px-6 flex-shrink-0 z-20">
      <div class="flex items-center gap-6">
        <!-- Brand/Logo -->
        <div class="flex items-center gap-3 cursor-pointer" @click="router.push('/')">
          <img src="@/assets/logo.png" alt="BrianJP Logo" class="h-8 w-8 object-contain rounded-lg" />
          <h2 class="font-headline-md text-lg text-primary hidden sm:block">BrianJP</h2>
        </div>
        
        <div class="w-px h-6 bg-paper-shadow hidden md:block"></div>
        
        <!-- Navigation Back -->
        <router-link to="/student/my-courses" class="hidden md:flex items-center gap-2 text-secondary hover:text-primary transition-colors font-button text-sm">
          <span class="material-symbols-outlined text-[18px]">arrow_back</span>
          Khóa học của tôi
        </router-link>
      </div>

      <div class="flex items-center gap-4 ml-auto">
        <router-link to="/courses" class="text-on-surface-variant hover:text-primary transition-colors font-button text-sm hidden sm:block">
          Khám phá khóa học
        </router-link>
        <div class="relative" ref="userMenuRef">
          <button @click="showUserMenu = !showUserMenu" class="flex items-center gap-2 hover:bg-surface-container-low rounded-xl px-2 py-1 transition-colors">
            <div class="w-8 h-8 rounded-full bg-secondary-container flex items-center justify-center text-on-secondary-container font-bold text-sm border border-outline-variant">
              {{ displayName.charAt(0).toUpperCase() }}
            </div>
            <span class="material-symbols-outlined text-outline-variant text-[18px]">expand_more</span>
          </button>
          
          <!-- Dropdown -->
          <div v-if="showUserMenu" class="absolute right-0 mt-2 w-56 bg-surface-container-lowest border border-paper-shadow rounded-xl shadow-xl z-50 overflow-hidden py-2">
            <div class="px-4 py-3 border-b border-paper-shadow">
              <p class="font-button text-sm text-ink-black truncate">{{ authStore.user?.fullName || 'Học viên' }}</p>
              <p class="text-xs text-secondary truncate">{{ authStore.user?.email }}</p>
            </div>
            <router-link to="/student/dashboard" @click="showUserMenu = false" class="flex items-center gap-3 px-4 py-3 text-on-surface-variant hover:bg-surface-container-high transition-colors text-sm">
              <span class="material-symbols-outlined text-[20px]">dashboard</span> Bảng điều khiển
            </router-link>
            <router-link to="/student/profile" @click="showUserMenu = false" class="flex items-center gap-3 px-4 py-3 text-on-surface-variant hover:bg-surface-container-high transition-colors text-sm">
              <span class="material-symbols-outlined text-[20px]">person</span> Hồ sơ cá nhân
            </router-link>
            <div class="border-t border-paper-shadow mt-1 pt-1">
              <button @click="handleLogout" class="w-full flex items-center gap-3 px-4 py-3 text-error hover:bg-error-container/30 transition-colors text-sm">
                <span class="material-symbols-outlined text-[20px]">logout</span> Đăng xuất
              </button>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- Main Learning Content -->
    <main class="flex-1 min-h-0 overflow-hidden">
      <router-view></router-view>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { AuthService } from '@/services/auth.service'

const authStore = useAuthStore()
const router = useRouter()

const showUserMenu = ref(false)
const userMenuRef = ref(null)

const displayName = computed(() => {
  return authStore.user?.fullName || 'Học viên'
})

const handleLogout = async () => {
  showUserMenu.value = false
  try {
    await AuthService.logout()
  } catch (error) {
    console.error('Logout error:', error)
  } finally {
    authStore.clearAuth()
    router.push('/login')
  }
}

const handleClickOutside = (e) => {
  if (userMenuRef.value && !userMenuRef.value.contains(e.target)) {
    showUserMenu.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>
