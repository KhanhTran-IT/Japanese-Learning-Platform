<template>
  <div class="min-h-screen flex flex-col bg-stitch-background text-stitch-foreground font-stitch-sans">
    <!-- Minimal top bar -->
    <header class="w-full px-4 sm:px-6 lg:px-8 py-4 flex items-center justify-between">
      <router-link to="/" class="flex items-center gap-2 group focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded-lg">
        <div class="w-8 h-8 rounded-lg bg-stitch-primary flex items-center justify-center">
          <span class="text-white font-bold text-sm font-stitch-serif">日</span>
        </div>
        <span class="font-stitch-serif font-bold text-xl text-stitch-foreground group-hover:text-stitch-primary transition-colors">
          BrianJP
        </span>
      </router-link>
      <button
        v-if="canGoBack"
        @click="goBack"
        class="inline-flex items-center gap-1 text-stitch-muted-foreground hover:text-stitch-foreground transition-colors text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-2 py-1"
      >
        <span class="material-symbols-outlined text-[18px]">arrow_back</span>
        Quay lại
      </button>
      <router-link
        v-else
        to="/courses"
        class="inline-flex items-center gap-1 text-stitch-muted-foreground hover:text-stitch-foreground transition-colors text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-2 py-1"
      >
        <span class="material-symbols-outlined text-[18px]">school</span>
        Khóa học
      </router-link>
    </header>

    <!-- Centered form area -->
    <main class="flex-1 flex items-center justify-center px-4 py-8">
      <div class="w-full max-w-[440px]">
        <div class="bg-stitch-card border border-stitch-border shadow-sm rounded-2xl p-8 md:p-10 text-stitch-card-foreground">
          <router-view></router-view>
        </div>

        <!-- Footer links -->
        <p class="text-center text-sm text-stitch-muted-foreground mt-6">
          <router-link to="/" class="hover:text-stitch-foreground transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-1">
            Trang chủ
          </router-link>
          <span class="mx-2">·</span>
          <router-link to="/courses" class="hover:text-stitch-foreground transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-1">
            Khóa học
          </router-link>
        </p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// window.history.length > 2 means user navigated here from within the app
const canGoBack = computed(() => typeof window !== 'undefined' && window.history.length > 2)

const goBack = () => {
  router.back()
}
</script>
