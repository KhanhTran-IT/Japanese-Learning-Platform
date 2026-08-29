<template>
  <div class="min-h-screen flex flex-col bg-background">
    <!-- Minimal top bar -->
    <header class="w-full px-margin-mobile md:px-margin-desktop py-4 flex items-center justify-between">
      <router-link to="/" class="flex items-center gap-2 font-headline-md text-headline-md text-primary no-underline hover:opacity-80 transition-opacity">
        <img src="@/assets/logo.png" alt="BrianJP Logo" class="h-8 w-8 object-contain rounded-lg" />
        BrianJP
      </router-link>
      <button
        v-if="canGoBack"
        @click="goBack"
        class="inline-flex items-center gap-1 text-secondary hover:text-on-surface transition-colors font-body-md text-sm"
      >
        <span class="material-symbols-outlined text-[18px]">arrow_back</span>
        Quay lại
      </button>
      <router-link
        v-else
        to="/courses"
        class="inline-flex items-center gap-1 text-secondary hover:text-on-surface transition-colors font-body-md text-sm no-underline"
      >
        <span class="material-symbols-outlined text-[18px]">school</span>
        Khóa học
      </router-link>
    </header>

    <!-- Centered form area -->
    <main class="flex-1 flex items-center justify-center px-margin-mobile py-8">
      <div class="w-full max-w-[440px]">
        <div class="zen-card rounded-2xl p-8 md:p-10">
          <router-view></router-view>
        </div>

        <!-- Footer links -->
        <p class="text-center text-sm text-secondary mt-6 font-body-md">
          <router-link to="/" class="hover:text-on-surface transition-colors no-underline text-secondary">
            Trang chủ
          </router-link>
          <span class="mx-2 text-outline-variant">·</span>
          <router-link to="/courses" class="hover:text-on-surface transition-colors no-underline text-secondary">
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
