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
            <button v-for="s in ['FB', 'YT', 'TW', 'IG']" :key="s" class="w-8 h-8 rounded-full bg-white/10 hover:bg-stitch-primary focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring flex items-center justify-center text-xs font-bold transition-colors">
              {{ s }}
            </button>
          </div>
        </div>

        <div>
          <h4 class="text-sm font-semibold mb-4 text-white/80 uppercase tracking-wider font-stitch-serif">Học tập</h4>
          <ul class="space-y-2">
            <li v-for="item in ['Khóa học N5', 'Khóa học N4', 'Khóa học N3', 'Khóa học N2', 'Khóa học N1']" :key="item">
              <router-link
                to="/courses"
                class="text-sm text-white/60 hover:text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-1 -ml-1"
              >
                {{ item }}
              </router-link>
            </li>
          </ul>
        </div>

        <div>
          <h4 class="text-sm font-semibold mb-4 text-white/80 uppercase tracking-wider font-stitch-serif">Luyện tập</h4>
          <ul class="space-y-2">
            <li v-for="item in ['Khóa học của tôi']" :key="item">
              <router-link
                to="/student/my-courses"
                class="text-sm text-white/60 hover:text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-1 -ml-1"
              >
                {{ item }}
              </router-link>
            </li>
          </ul>
        </div>

        <div>
          <h4 class="text-sm font-semibold mb-4 text-white/80 uppercase tracking-wider font-stitch-serif">Hỗ trợ</h4>
          <ul class="space-y-2">
            <li v-for="item in ['Về chúng tôi', 'Chính sách bảo mật', 'Điều khoản sử dụng', 'Liên hệ']" :key="item">
              <a href="#" class="text-sm text-white/60 hover:text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-1 -ml-1">
                {{ item }}
              </a>
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
