<template>
  <div class="min-h-screen bg-[#0f1117] text-white flex font-stitch-sans">
    <!-- Desktop Sidebar -->
    <aside class="hidden md:flex flex-col w-56 bg-[#161b27] border-r border-white/5 flex-shrink-0">
      <div class="p-5 border-b border-white/5">
        <div class="flex items-center gap-2">
          <div class="w-7 h-7 rounded-lg bg-stitch-primary flex items-center justify-center">
            <span class="text-white font-bold text-xs">日</span>
          </div>
          <span class="font-stitch-serif font-bold text-base">BrianJP</span>
          <span class="ml-auto text-[10px] bg-white/10 px-1.5 py-0.5 rounded text-white/50 uppercase tracking-wider">Admin</span>
        </div>
      </div>

      <nav class="flex-1 py-4 overflow-y-auto">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="w-full flex items-center gap-3 px-4 py-3 text-sm transition-colors"
          active-class="bg-stitch-primary/10 text-stitch-primary border-r-2 border-stitch-primary"
          :class="[$route.path === item.path ? '' : 'text-white/50 hover:text-white/80 hover:bg-white/5']"
        >
          <span class="text-lg">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>

        <div class="my-2 border-t border-white/5"></div>

        <router-link
          to="/"
          class="w-full flex items-center gap-3 px-4 py-3 text-sm text-white/50 hover:text-white/80 hover:bg-white/5 transition-colors"
        >
          <span class="text-lg">🌐</span>
          <span>Về trang chủ</span>
        </router-link>
      </nav>

      <div class="p-4 border-t border-white/5">
        <button
          @click="handleLogout"
          class="w-full flex items-center justify-center gap-2 py-2.5 rounded-lg bg-red-500/10 text-red-400 hover:bg-red-500/20 hover:text-red-300 transition-colors text-sm font-medium"
        >
          <span>🚪</span>
          <span>Đăng xuất</span>
        </button>
      </div>
    </aside>

    <!-- Mobile Drawer Modal -->
    <div 
      v-if="mobileDrawerOpen" 
      class="fixed inset-0 z-50 md:hidden flex"
      role="dialog"
      aria-modal="true"
      aria-label="Admin Navigation Drawer"
    >
      <!-- Backdrop -->
      <div 
        class="fixed inset-0 bg-black/70 backdrop-blur-sm transition-opacity" 
        @click="mobileDrawerOpen = false"
      ></div>
      
      <!-- Drawer Content -->
      <aside class="relative w-64 max-w-[80vw] bg-[#161b27] border-r border-white/10 flex flex-col h-full z-10 shadow-2xl">
        <div class="p-5 border-b border-white/5 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <div class="w-7 h-7 rounded-lg bg-stitch-primary flex items-center justify-center">
              <span class="text-white font-bold text-xs">日</span>
            </div>
            <span class="font-stitch-serif font-bold text-base">BrianJP</span>
            <span class="text-[10px] bg-white/10 px-1.5 py-0.5 rounded text-white/50 uppercase tracking-wider">Admin</span>
          </div>
          <button 
            @click="mobileDrawerOpen = false" 
            class="p-1.5 rounded-lg text-white/60 hover:text-white hover:bg-white/10 text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring"
            aria-label="Đóng menu"
          >
            ✕
          </button>
        </div>

        <nav class="flex-1 py-4 overflow-y-auto">
          <router-link
            v-for="item in menuItems"
            :key="item.path"
            :to="item.path"
            @click="mobileDrawerOpen = false"
            class="w-full flex items-center gap-3 px-4 py-3 text-sm transition-colors"
            active-class="bg-stitch-primary/10 text-stitch-primary border-r-2 border-stitch-primary"
            :class="[$route.path === item.path ? '' : 'text-white/50 hover:text-white/80 hover:bg-white/5']"
          >
            <span class="text-lg">{{ item.icon }}</span>
            <span>{{ item.label }}</span>
          </router-link>

          <div class="my-2 border-t border-white/5"></div>

          <router-link
            to="/"
            @click="mobileDrawerOpen = false"
            class="w-full flex items-center gap-3 px-4 py-3 text-sm text-white/50 hover:text-white/80 hover:bg-white/5 transition-colors"
          >
            <span class="text-lg">🌐</span>
            <span>Về trang chủ</span>
          </router-link>
        </nav>

        <div class="p-4 border-t border-white/5">
          <button
            @click="handleLogout"
            class="w-full flex items-center justify-center gap-2 py-2.5 rounded-lg bg-red-500/10 text-red-400 hover:bg-red-500/20 hover:text-red-300 transition-colors text-sm font-medium"
          >
            <span>🚪</span>
            <span>Đăng xuất</span>
          </button>
        </div>
      </aside>
    </div>

    <!-- Main Content -->
    <main class="flex-1 flex flex-col min-w-0">
      <!-- Top bar -->
      <header class="flex items-center justify-between px-4 sm:px-6 py-4 border-b border-white/5 bg-[#161b27]/80 backdrop-blur-sm sticky top-0 z-10 shrink-0">
        <div class="flex items-center gap-3">
          <!-- Mobile Hamburger Toggle -->
          <button
            @click="mobileDrawerOpen = true"
            class="md:hidden p-2 rounded-lg bg-white/5 hover:bg-white/10 text-white focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring"
            aria-label="Mở menu quản trị"
            :aria-expanded="mobileDrawerOpen"
          >
            <span class="text-lg leading-none">☰</span>
          </button>
          
          <div>
            <h1 class="font-stitch-serif font-bold text-lg sm:text-xl text-white">{{ currentPageTitle }}</h1>
            <p class="text-xs sm:text-sm text-white/40 hidden sm:block">{{ currentDate }}</p>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <router-link 
            to="/" 
            class="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white/5 hover:bg-white/10 text-xs text-white/70 hover:text-white transition-colors"
          >
            <span>🌐</span>
            <span>Trang chủ</span>
          </router-link>
          <div class="w-9 h-9 rounded-full bg-stitch-accent flex items-center justify-center text-white font-bold text-sm shadow-lg">
            {{ userInitials }}
          </div>
        </div>
      </header>

      <!-- Page Content -->
      <div class="flex-1 overflow-y-auto p-4 sm:p-6">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { AuthService } from '@/services/auth.service'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const mobileDrawerOpen = ref(false)

const handleEscape = (e) => {
  if (e.key === 'Escape' && mobileDrawerOpen.value) {
    mobileDrawerOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleEscape)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscape)
})

const menuItems = [
  { path: '/admin/dashboard', icon: '📊', label: 'Dashboard' },
  { path: '/admin/users', icon: '👥', label: 'Học viên' },
  { path: '/admin/courses', icon: '📚', label: 'Khóa học' },
  { path: '/admin/quizzes', icon: '📝', label: 'Bài tập (Quiz)' }
]

const currentPageTitle = computed(() => {
  const match = menuItems.find(item => route.path.startsWith(item.path))
  if (route.path.includes('/structure')) return 'Cấu trúc Khóa học'
  if (route.path.includes('/builder')) return 'Quản lý Câu hỏi'
  return match ? match.label : 'Quản trị hệ thống'
})

const currentDate = computed(() => {
  return new Intl.DateTimeFormat('vi-VN', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  }).format(new Date())
})

const userInitials = computed(() => {
  const name = authStore.user?.fullName || 'Admin'
  const parts = name.split(' ')
  if (parts.length >= 2) {
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
  }
  return name.substring(0, 2).toUpperCase()
})

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
