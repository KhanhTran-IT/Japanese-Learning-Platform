<template>
  <div class="flex h-screen bg-background font-body-md text-on-surface">
    <!-- Sidebar Navigation -->
    <aside class="hidden lg:flex flex-col w-[280px] bg-surface-container-low border-r border-paper-shadow overflow-y-auto custom-scroll p-6 flex-shrink-0">
      <div class="mb-8 flex items-center gap-3 cursor-pointer" @click="router.push('/')">
        <img src="@/assets/logo.png" alt="BrianJP Logo" class="h-10 w-10 object-contain rounded-lg" />
        <div>
          <h2 class="font-headline-md text-[18px] text-primary">BrianJP</h2>
          <p class="text-on-surface-variant font-label-sm text-[10px] uppercase tracking-wider">Học viên</p>
        </div>
      </div>
      
      <nav class="space-y-2 mb-8 flex-1">
        <router-link to="/" class="w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all text-on-surface-variant hover:bg-surface-container-high">
          <span class="material-symbols-outlined">home</span>
          <span class="font-button text-button">Trang chủ</span>
        </router-link>
        
        <router-link to="/courses" class="w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all text-on-surface-variant hover:bg-surface-container-high">
          <span class="material-symbols-outlined">explore</span>
          <span class="font-button text-button">Khám phá khóa học</span>
        </router-link>

        <div class="border-t border-paper-shadow my-3"></div>
        
        <router-link to="/student/dashboard" class="w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all" active-class="bg-primary-container text-on-primary-container font-bold" :class="[isRouteActive('/student/dashboard') ? '' : 'text-on-surface-variant hover:bg-surface-container-high']">
          <span class="material-symbols-outlined" :style="isRouteActive('/student/dashboard') ? 'font-variation-settings: \'FILL\' 1;' : ''">dashboard</span>
          <span class="font-button text-button">Bảng điều khiển</span>
        </router-link>
        
        <router-link to="/student/my-courses" class="w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all" active-class="bg-primary-container text-on-primary-container font-bold" :class="[isRouteActive('/student/my-courses') ? '' : 'text-on-surface-variant hover:bg-surface-container-high']">
          <span class="material-symbols-outlined" :style="isRouteActive('/student/my-courses') ? 'font-variation-settings: \'FILL\' 1;' : ''">school</span>
          <span class="font-button text-button">Khóa học của tôi</span>
        </router-link>
        
        <router-link to="/student/profile" class="w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all" active-class="bg-primary-container text-on-primary-container font-bold" :class="[isRouteActive('/student/profile') ? '' : 'text-on-surface-variant hover:bg-surface-container-high']">
          <span class="material-symbols-outlined" :style="isRouteActive('/student/profile') ? 'font-variation-settings: \'FILL\' 1;' : ''">person</span>
          <span class="font-button text-button">Hồ sơ cá nhân</span>
        </router-link>
      </nav>

      <div class="mt-auto">
        <button @click="handleLogout" class="w-full flex items-center justify-center gap-2 py-3 border border-outline text-on-surface-variant hover:bg-error-container hover:text-on-error-container hover:border-error-container rounded-xl transition-all font-button">
          <span class="material-symbols-outlined">logout</span>
          Đăng xuất
        </button>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="flex-1 flex flex-col min-w-0">
      <!-- Topbar -->
      <header class="h-16 bg-surface/80 glass-nav border-b border-paper-shadow flex items-center justify-between px-6 flex-shrink-0">
        <div class="lg:hidden flex items-center gap-3">
          <div class="w-8 h-8 bg-primary text-white rounded flex items-center justify-center font-bold">B</div>
        </div>
        <div class="hidden lg:block">
          <!-- Breadcrumbs or page title could go here -->
        </div>
        <div class="flex items-center gap-4 ml-auto">
          <div class="flex items-center gap-3">
            <div class="text-right hidden sm:block">
              <p class="font-label-sm text-label-sm text-secondary">Xin chào,</p>
              <p class="font-button text-button text-ink-black">{{ displayName }}</p>
            </div>
            <div class="w-10 h-10 rounded-full bg-secondary-fixed flex items-center justify-center text-on-secondary font-bold text-lg border-2 border-surface-variant">
              {{ displayName.charAt(0).toUpperCase() }}
            </div>
          </div>
        </div>
      </header>
      
      <!-- Page Content -->
      <div class="flex-1 overflow-y-auto custom-scroll p-4 md:p-8">
        <div class="max-w-7xl mx-auto h-full">
          <router-view></router-view>
        </div>
      </div>
    </main>
    
    <!-- Mobile Bottom Nav -->
    <nav class="lg:hidden fixed bottom-0 w-full bg-surface-container-lowest border-t border-paper-shadow flex justify-around p-3 z-50">
      <router-link to="/student/dashboard" class="flex flex-col items-center gap-1 p-2 rounded-lg" active-class="text-primary" :class="[isRouteActive('/student/dashboard') ? '' : 'text-on-surface-variant']">
        <span class="material-symbols-outlined" :style="isRouteActive('/student/dashboard') ? 'font-variation-settings: \'FILL\' 1;' : ''">dashboard</span>
      </router-link>
      <router-link to="/student/my-courses" class="flex flex-col items-center gap-1 p-2 rounded-lg" active-class="text-primary" :class="[isRouteActive('/student/my-courses') ? '' : 'text-on-surface-variant']">
        <span class="material-symbols-outlined" :style="isRouteActive('/student/my-courses') ? 'font-variation-settings: \'FILL\' 1;' : ''">school</span>
      </router-link>
      <router-link to="/student/profile" class="flex flex-col items-center gap-1 p-2 rounded-lg" active-class="text-primary" :class="[isRouteActive('/student/profile') ? '' : 'text-on-surface-variant']">
        <span class="material-symbols-outlined" :style="isRouteActive('/student/profile') ? 'font-variation-settings: \'FILL\' 1;' : ''">person</span>
      </router-link>
    </nav>
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

const displayName = computed(() => {
  return authStore.user?.fullName || 'Học viên'
})

const isRouteActive = (path) => {
  return route.path.startsWith(path)
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
