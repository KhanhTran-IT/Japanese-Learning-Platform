<template>
  <header class="fixed top-0 left-0 right-0 z-50 bg-stitch-card/90 backdrop-blur-md border-b border-stitch-border font-stitch-sans">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Logo -->
        <router-link to="/" class="flex items-center gap-2 group focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded-lg">
          <div class="w-8 h-8 rounded-lg bg-stitch-primary flex items-center justify-center">
            <span class="text-white font-bold text-sm font-stitch-serif">日</span>
          </div>
          <span class="font-stitch-serif font-bold text-xl text-stitch-foreground group-hover:text-stitch-primary transition-colors">
            BrianJP
          </span>
        </router-link>

        <!-- Desktop Nav -->
        <nav class="hidden md:flex items-center gap-6">
          <router-link 
            to="/courses" 
            class="text-sm font-medium transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-1"
            :class="[isRouteActive('/courses') ? 'text-stitch-primary' : 'text-stitch-muted-foreground hover:text-stitch-foreground']"
          >
            Khóa học
          </router-link>
          
          <router-link 
            v-if="isAdmin"
            to="/admin/dashboard" 
            class="text-sm font-medium transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-1"
            :class="[isRouteActive('/admin') ? 'text-stitch-primary' : 'text-stitch-muted-foreground hover:text-stitch-foreground']"
          >
            Admin
          </router-link>
        </nav>

        <!-- Desktop Auth -->
        <div class="hidden md:flex items-center gap-3">
          <template v-if="isLoggedIn">
            <router-link 
              :to="dashboardRoute"
              class="text-sm font-medium focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-2 py-1"
              :class="[isRouteActive(dashboardRoute) ? 'text-stitch-primary' : 'text-stitch-muted-foreground hover:text-stitch-primary transition-colors']"
            >
              Dashboard
            </router-link>
            
            <router-link 
              to="/student/flashcards"
              class="text-sm font-medium focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-2 py-1"
              :class="[isRouteActive('/student/flashcards') ? 'text-stitch-primary' : 'text-stitch-muted-foreground hover:text-stitch-primary transition-colors']"
            >
              Flashcards
            </router-link>
            
            <router-link 
              v-if="!isAdmin"
              to="/student/profile"
              class="flex items-center gap-2 text-sm font-medium text-stitch-foreground hover:text-stitch-primary focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded-full"
            >
              <div class="w-8 h-8 rounded-full bg-stitch-accent flex items-center justify-center text-white text-xs font-bold uppercase">
                {{ userInitials }}
              </div>
            </router-link>

            <button
              @click="handleLogout"
              class="text-sm text-stitch-muted-foreground hover:text-stitch-foreground transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-2 py-1"
            >
              Đăng xuất
            </button>
          </template>
          <template v-else>
            <router-link 
              to="/login"
              class="text-sm font-medium text-stitch-foreground hover:text-stitch-primary transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-2 py-1"
            >
              Đăng nhập
            </router-link>
            <router-link 
              to="/register"
              class="px-4 py-2 bg-stitch-primary text-white text-sm font-medium rounded-stitch hover:bg-stitch-primary/90 transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring focus-visible:ring-offset-2"
            >
              Đăng ký miễn phí
            </router-link>
          </template>
        </div>

        <!-- Mobile menu button -->
        <button
          @click.stop="menuOpen = !menuOpen"
          class="md:hidden p-2 rounded-lg hover:bg-stitch-muted transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring text-stitch-foreground"
          :aria-expanded="menuOpen"
          aria-label="Toggle navigation menu"
        >
          <div class="w-5 h-0.5 bg-current mb-1 transition-all" :style="{ transform: menuOpen ? 'rotate(45deg) translate(2px, 2px)' : '' }"></div>
          <div class="w-5 h-0.5 bg-current mb-1 transition-all" :style="{ opacity: menuOpen ? 0 : 1 }"></div>
          <div class="w-5 h-0.5 bg-current transition-all" :style="{ transform: menuOpen ? 'rotate(-45deg) translate(2px, -2px)' : '' }"></div>
        </button>
      </div>

      <!-- Mobile menu -->
      <transition
        enter-active-class="transition duration-200 ease-out"
        enter-from-class="opacity-0 -translate-y-4"
        enter-to-class="opacity-100 translate-y-0"
        leave-active-class="transition duration-150 ease-in"
        leave-from-class="opacity-100 translate-y-0"
        leave-to-class="opacity-0 -translate-y-4"
      >
        <div v-if="menuOpen" class="md:hidden py-4 border-t border-stitch-border space-y-2 origin-top bg-stitch-card absolute left-0 right-0 px-4 shadow-lg rounded-b-xl" ref="mobileMenuRef" @click.stop>
          <router-link
            to="/courses"
            @click="menuOpen = false"
            class="block w-full text-left px-3 py-2 rounded-lg text-sm font-medium focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring"
            :class="[isRouteActive('/courses') ? 'bg-stitch-muted text-stitch-primary' : 'hover:bg-stitch-muted text-stitch-foreground']"
          >
            Khóa học
          </router-link>
          
          <template v-if="isLoggedIn">
            <router-link 
              :to="dashboardRoute" 
              @click="menuOpen = false" 
              class="block w-full text-left px-3 py-2 rounded-lg text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring"
              :class="[isRouteActive(dashboardRoute) ? 'bg-stitch-muted text-stitch-primary' : 'hover:bg-stitch-muted text-stitch-foreground']"
            >
              Dashboard
            </router-link>
            
            <router-link
              to="/student/flashcards"
              @click="closeMobileMenu"
              class="block px-3 py-3 text-base font-medium rounded-lg"
              :class="[isRouteActive('/student/flashcards') ? 'bg-stitch-primary/10 text-stitch-primary' : 'text-stitch-foreground hover:bg-stitch-muted']"
            >
              <div class="flex items-center gap-3">
                <span class="material-symbols-outlined text-lg">style</span>
                Flashcards
              </div>
            </router-link>
            
            <router-link 
              v-if="isAdmin"
              to="/admin/dashboard" 
              @click="menuOpen = false" 
              class="block w-full text-left px-3 py-2 rounded-lg text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring"
              :class="[isRouteActive('/admin') ? 'bg-stitch-muted text-stitch-primary' : 'hover:bg-stitch-muted text-stitch-foreground']"
            >
              Admin
            </router-link>

            <router-link 
              v-if="!isAdmin"
              to="/student/profile" 
              @click="menuOpen = false" 
              class="block w-full text-left px-3 py-2 rounded-lg text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring"
              :class="[isRouteActive('/student/profile') ? 'bg-stitch-muted text-stitch-primary' : 'hover:bg-stitch-muted text-stitch-foreground']"
            >
              Profile
            </router-link>
            
            <button 
              @click="handleLogout" 
              class="block w-full text-left px-3 py-2 text-sm text-red-500 hover:bg-red-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-red-500 rounded-lg"
            >
              Đăng xuất
            </button>
          </template>
          <template v-else>
            <div class="pt-2 flex flex-col gap-2">
              <router-link 
                to="/login" 
                @click="menuOpen = false" 
                class="block w-full text-center px-4 py-2 border border-stitch-border rounded-lg text-sm hover:bg-stitch-muted text-stitch-foreground focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring"
              >
                Đăng nhập
              </router-link>
              <router-link 
                to="/register" 
                @click="menuOpen = false" 
                class="block w-full text-center px-4 py-2 bg-stitch-primary text-white rounded-lg text-sm hover:bg-stitch-primary/90 focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring focus-visible:ring-offset-2"
              >
                Đăng ký miễn phí
              </router-link>
            </div>
          </template>
        </div>
      </transition>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { AuthService } from '@/services/auth.service'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const menuOpen = ref(false)
const mobileMenuRef = ref(null)

const isLoggedIn = computed(() => authStore.isAuthenticated)
const user = computed(() => authStore.user)
const isAdmin = computed(() => {
  const roles = user.value?.roles || []
  return roles.includes('ADMIN') || roles.includes('SUPER_ADMIN')
})
const dashboardRoute = computed(() => isAdmin.value ? '/admin/dashboard' : '/student/dashboard')

const userInitials = computed(() => {
  const name = user.value?.fullName || 'U'
  return name.substring(0, 2)
})

const isRouteActive = (path) => {
  if (path === '/') return route.path === '/'
  // specific check for dashboard so it doesn't stay active for profile
  if (path === dashboardRoute.value) return route.path === dashboardRoute.value
  return route.path.startsWith(path)
}

const handleLogout = async () => {
  menuOpen.value = false
  try {
    await AuthService.logout()
  } catch (error) {
    console.error('Logout error:', error)
  } finally {
    authStore.clearAuth()
    router.push('/')
  }
}

const handleEscape = (e) => {
  if (e.key === 'Escape' && menuOpen.value) {
    menuOpen.value = false
  }
}

const handleClickOutside = () => {
  if (menuOpen.value) {
    menuOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleEscape)
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscape)
  document.removeEventListener('click', handleClickOutside)
})
</script>
