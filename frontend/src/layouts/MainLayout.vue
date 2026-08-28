<template>
  <div class="min-h-screen flex flex-col bg-background text-on-surface">
    <!-- Top Navigation Bar -->
    <nav class="fixed top-0 w-full z-50 flex justify-between items-center px-margin-mobile md:px-margin-desktop h-16 glass-nav border-b border-paper-shadow">
      <div class="flex items-center gap-4">
        <router-link to="/" class="flex items-center gap-3">
          <!-- Placeholder logo for now, as we don't have the actual SVG -->
          <div class="w-8 h-8 bg-primary text-white rounded flex items-center justify-center font-bold text-lg">B</div>
          <span class="font-headline-md text-headline-md font-bold text-primary hidden md:inline">BrianJP</span>
        </router-link>
      </div>
      
      <!-- Desktop Navigation Links -->
      <div class="hidden md:flex items-center gap-8">
        <router-link to="/courses" class="text-on-secondary-container font-medium hover:text-primary transition-colors font-body-md text-body-md" active-class="text-primary font-bold border-b-2 border-primary pb-1">Khóa học</router-link>
        <a href="#" class="text-on-secondary-container font-medium hover:text-primary transition-colors font-body-md text-body-md">Flashcards</a>
        <a href="#" class="text-on-secondary-container font-medium hover:text-primary transition-colors font-body-md text-body-md">Games</a>
      </div>
      
      <div class="flex items-center gap-4">
        <!-- Guest: Login/Register -->
        <template v-if="!isLoggedIn">
          <router-link to="/login" class="text-on-secondary-container font-button text-button hover:text-primary transition-colors hidden md:block">Đăng nhập</router-link>
          <router-link to="/register" class="bg-primary text-on-primary px-4 py-2 rounded-xl font-button text-button hover:opacity-90 active:scale-95 transition-all">Đăng ký</router-link>
        </template>
        <!-- Authenticated user menu -->
        <template v-else>
          <router-link to="/student/my-courses" class="text-on-secondary-container font-button text-button hover:text-primary transition-colors hidden md:block">Khóa học của tôi</router-link>
          <div class="relative" ref="userMenuRef">
            <button @click="showUserMenu = !showUserMenu" class="flex items-center gap-2 bg-primary text-on-primary px-4 py-2 rounded-xl hover:opacity-90 active:scale-95 transition-all">
              <span class="material-symbols-outlined text-[20px]" style="font-variation-settings: 'FILL' 1;">account_circle</span>
              <span class="font-button text-button hidden sm:inline">{{ displayName }}</span>
              <span class="material-symbols-outlined text-[18px]">expand_more</span>
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
              <router-link to="/student/my-courses" @click="showUserMenu = false" class="flex items-center gap-3 px-4 py-3 text-on-surface-variant hover:bg-surface-container-high transition-colors text-sm">
                <span class="material-symbols-outlined text-[20px]">school</span> Khóa học của tôi
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
        </template>
      </div>
    </nav>

    <main class="flex-1 mt-16 flex flex-col">
      <router-view></router-view>
    </main>

    <!-- Footer -->
    <footer class="w-full py-12 px-margin-mobile md:px-margin-desktop grid grid-cols-1 md:grid-cols-4 gap-gutter bg-surface-container-highest border-t border-paper-shadow mt-auto">
      <div class="col-span-1 md:col-span-1">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-8 h-8 bg-primary text-white rounded flex items-center justify-center font-bold text-lg">B</div>
          <span class="font-headline-md text-headline-md text-primary">BrianJP</span>
        </div>
        <p class="text-body-md text-on-secondary-container mb-6 italic">Cultivating language growth through focus and discipline.</p>
        <p class="text-label-sm text-on-secondary-container">© 2026 BrianJP. All rights reserved.</p>
      </div>
      <div class="space-y-4">
        <h4 class="font-button text-button text-primary">Tài nguyên</h4>
        <ul class="space-y-2">
          <li><router-link to="/courses" class="text-on-secondary-container hover:text-primary transition-colors">Tất cả khóa học</router-link></li>
          <li><a class="text-on-secondary-container hover:text-primary transition-colors" href="#">Tài liệu JLPT</a></li>
        </ul>
      </div>
      <div class="space-y-4">
        <h4 class="font-button text-button text-primary">Cộng đồng</h4>
        <ul class="space-y-2">
          <li><a class="text-on-secondary-container hover:text-primary transition-colors" href="#">Facebook</a></li>
          <li><a class="text-on-secondary-container hover:text-primary transition-colors" href="#">YouTube</a></li>
        </ul>
      </div>
      <div class="space-y-4">
        <h4 class="font-button text-button text-primary">Hỗ trợ</h4>
        <ul class="space-y-2">
          <li><a class="text-on-secondary-container hover:text-primary transition-colors" href="#">Liên hệ</a></li>
          <li><a class="text-on-secondary-container hover:text-primary transition-colors" href="#">Chính sách bảo mật</a></li>
        </ul>
      </div>
    </footer>
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

const isLoggedIn = computed(() => authStore.isAuthenticated)

const displayName = computed(() => {
  const name = authStore.user?.fullName || 'Học viên'
  // Truncate for button display
  return name.length > 12 ? name.substring(0, 12) + '…' : name
})

const handleLogout = async () => {
  showUserMenu.value = false
  try {
    await AuthService.logout()
  } catch (error) {
    console.error('Logout error:', error)
  } finally {
    authStore.clearAuth()
    router.push('/')
  }
}

// Close dropdown when clicking outside
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
