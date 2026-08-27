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
        <!-- Auth Links -->
        <template v-if="!isLoggedIn">
          <router-link to="/login" class="text-on-secondary-container font-button text-button hover:text-primary transition-colors hidden md:block">Đăng nhập</router-link>
          <router-link to="/register" class="bg-primary text-on-primary px-4 py-2 rounded-xl font-button text-button hover:opacity-90 active:scale-95 transition-all">Đăng ký</router-link>
        </template>
        <template v-else>
          <router-link :to="dashboardRoute" class="flex items-center gap-2 bg-primary text-on-primary px-4 py-2 rounded-xl hover:opacity-90 active:scale-95 transition-all">
            <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">account_circle</span>
            <span class="font-button text-button hidden sm:inline">Hồ sơ</span>
          </router-link>
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
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth.store'

const authStore = useAuthStore()

const isLoggedIn = computed(() => !!authStore.token)

const dashboardRoute = computed(() => {
  if (authStore.isAdmin) return '/admin/dashboard'
  return '/student/dashboard'
})
</script>
