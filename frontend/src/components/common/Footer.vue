<template>
  <footer class="bg-stitch-foreground text-white mt-auto font-stitch-sans">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-10">
        <div class="md:col-span-1">
          <div class="flex items-center gap-2 mb-4">
            <div class="w-8 h-8 rounded-lg bg-stitch-primary flex items-center justify-center">
              <span class="text-white font-bold text-sm font-stitch-serif">日</span>
            </div>
            <span class="font-stitch-serif font-bold text-xl">BrianJP</span>
          </div>
          <p class="text-sm text-white/60 leading-relaxed">
            Nền tảng học tiếng Nhật toàn diện — từ N5 đến N1, với flashcard, quiz, game và cộng đồng học viên sôi động.
          </p>
          <div class="flex gap-3 mt-6">
            <button 
              v-for="s in ['FB', 'YT', 'TW', 'IG']" 
              :key="s" 
              type="button"
              disabled
              aria-disabled="true"
              title="Kênh mạng xã hội (Sắp ra mắt)"
              class="w-8 h-8 rounded-full bg-white/10 opacity-50 cursor-not-allowed flex items-center justify-center text-xs font-bold transition-opacity"
            >
              {{ s }}
            </button>
          </div>
        </div>

        <div>
          <h4 class="text-sm font-semibold mb-4 text-white/80 uppercase tracking-wider font-stitch-serif">Học tập</h4>
          <ul class="space-y-2">
            <li v-for="level in ['N5', 'N4', 'N3', 'N2', 'N1']" :key="level">
              <router-link
                :to="{ path: '/courses', query: { level } }"
                class="text-sm text-white/60 hover:text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-1 -ml-1"
              >
                Khóa học {{ level }}
              </router-link>
            </li>
          </ul>
        </div>

        <div>
          <h4 class="text-sm font-semibold mb-4 text-white/80 uppercase tracking-wider font-stitch-serif">Luyện tập</h4>
          <ul class="space-y-2">
            <li>
              <router-link
                to="/student/my-courses"
                class="text-sm text-white/60 hover:text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-1 -ml-1"
              >
                Khóa học của tôi
              </router-link>
            </li>
            <li>
              <router-link
                to="/student/flashcards"
                class="text-sm text-white/60 hover:text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-1 -ml-1"
              >
                Flashcards ôn tập
              </router-link>
            </li>
            <li>
              <router-link
                to="/student/leaderboard"
                class="text-sm text-white/60 hover:text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-1 -ml-1"
              >
                Bảng xếp hạng
              </router-link>
            </li>
          </ul>
        </div>

        <div>
          <h4 class="text-sm font-semibold mb-4 text-white/80 uppercase tracking-wider font-stitch-serif">Hỗ trợ</h4>
          <ul class="space-y-2">
            <li v-for="item in ['Về chúng tôi', 'Chính sách bảo mật', 'Điều khoản sử dụng', 'Liên hệ']" :key="item">
              <span 
                class="inline-flex items-center gap-1.5 text-sm text-white/40 cursor-not-allowed select-none px-1 -ml-1"
                :title="`${item} (Đang hoàn thiện)`"
              >
                <span>{{ item }}</span>
              </span>
            </li>
          </ul>
        </div>
      </div>

      <div class="mt-12 pt-8 border-t border-white/10 flex flex-col sm:flex-row items-center justify-between gap-4">
        <p class="text-sm text-white/40">© 2026 BrianJP. All rights reserved.</p>
        <div class="flex items-center gap-1 text-sm text-white/40">
          <span>Yêu thích tiếng Nhật</span>
          <span class="text-stitch-accent">♡</span>
          <span>từ Việt Nam</span>
        </div>
      </div>
    </div>
  </footer>
</template>
