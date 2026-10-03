<template>
  <div class="min-h-screen bg-stitch-background flex font-stitch-sans text-stitch-foreground">
    <!-- Left panel — decorative -->
    <div class="hidden lg:flex lg:w-1/2 relative overflow-hidden bg-stitch-foreground">
      <div class="absolute inset-0 bg-cover bg-center opacity-30" style="background-image: url('https://images.unsplash.com/photo-1524413840807-0c3cb6fa808d?w=900&h=1200&fit=crop&auto=format')"></div>
      <div class="relative z-10 flex flex-col justify-between p-12 w-full h-full">
        <button @click="goBackOrHome" class="flex items-center gap-2 w-fit focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded-lg text-left">
          <div class="w-9 h-9 rounded-xl bg-stitch-primary flex items-center justify-center">
            <span class="text-white font-bold font-stitch-serif">日</span>
          </div>
          <span class="font-stitch-serif font-bold text-2xl text-white">BrianJP</span>
        </button>

        <div>
          <div class="text-white/20 font-stitch-serif text-[180px] leading-none select-none mb-8">語</div>
          <blockquote class="text-white/80 text-xl font-stitch-serif italic leading-relaxed mb-4">
            "Mỗi ngày học một chút, tích lũy thành thạo tiếng Nhật."
          </blockquote>
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-full bg-stitch-accent flex items-center justify-center text-white font-bold text-sm">BP</div>
            <div>
              <div class="text-white text-sm font-semibold">Brian Phạm</div>
              <div class="text-white/40 text-xs">Giảng viên — JLPT N1</div>
            </div>
          </div>
        </div>

        <div class="flex gap-6 text-sm text-white/30">
          <span>50,000+ học viên</span>
          <span>200+ bài học</span>
          <span>4.9★ đánh giá</span>
        </div>
      </div>
    </div>

    <!-- Right panel — form -->
    <div class="w-full lg:w-1/2 flex items-center justify-center px-6 py-12">
      <div class="w-full max-w-sm">
        <!-- Mobile header -->
        <div class="flex items-center justify-between mb-8 lg:hidden">
          <button @click="goBackOrHome" class="flex items-center gap-2 focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded-lg">
            <div class="w-8 h-8 rounded-lg bg-stitch-primary flex items-center justify-center">
              <span class="text-white font-bold text-sm font-stitch-serif">日</span>
            </div>
            <span class="font-stitch-serif font-bold text-xl">BrianJP</span>
          </button>
        </div>

        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const canGoBack = computed(() => typeof window !== 'undefined' && window.history.length > 2)

const goBackOrHome = () => {
  if (canGoBack.value) {
    router.back()
  } else {
    router.push('/')
  }
}
</script>
