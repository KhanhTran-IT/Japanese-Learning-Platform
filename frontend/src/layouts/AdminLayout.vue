<template>
  <div class="min-h-screen bg-[#0f1117] text-white flex font-stitch-sans">
    <!-- Sidebar -->
    <aside class="w-56 bg-[#161b27] border-r border-white/5 flex flex-col flex-shrink-0">
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

    <!-- Main Content -->
    <main class="flex-1 flex flex-col min-w-0">
      <!-- Top bar -->
      <header class="flex items-center justify-between px-6 py-4 border-b border-white/5 bg-[#161b27]/50 backdrop-blur-sm sticky top-0 z-10 shrink-0">
        <div>
          <h1 class="font-stitch-serif font-bold text-xl text-white">{{ currentPageTitle }}</h1>
          <p class="text-sm text-white/40">{{ currentDate }}</p>
        </div>
        <div class="flex items-center gap-3">
          <button class="w-9 h-9 rounded-lg bg-white/5 hover:bg-white/10 flex items-center justify-center text-lg transition-colors">🔔</button>
          <div class="w-9 h-9 rounded-full bg-stitch-accent flex items-center justify-center text-white font-bold text-sm shadow-lg">
            {{ userInitials }}
          </div>
        </div>
      </header>

      <!-- Page Content -->
      <div class="flex-1 overflow-y-auto p-6">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { AuthService } from '@/services/auth.service'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

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
