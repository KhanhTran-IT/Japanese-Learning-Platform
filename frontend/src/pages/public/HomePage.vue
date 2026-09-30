<template>
  <div class="home bg-stitch-background font-stitch-sans">
    <!-- Hero -->
    <section class="relative min-h-screen flex items-center overflow-hidden">
      <div class="absolute inset-0 bg-cover bg-center" style="background-image: url('https://images.unsplash.com/photo-1598957232485-fab51e0ed7e8?w=1600&h=900&fit=crop&auto=format')">
        <div class="absolute inset-0 bg-stitch-foreground/80"></div>
      </div>

      <!-- Decorative kanji -->
      <div class="absolute right-8 top-1/2 -translate-y-1/2 hidden lg:block select-none pointer-events-none">
        <span class="text-[240px] font-stitch-serif font-bold text-white/5 leading-none">語</span>
      </div>

      <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-32 z-10 w-full">
        <div class="max-w-2xl">
          <div class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-stitch-primary/20 border border-stitch-primary/30 mb-6">
            <span class="w-2 h-2 rounded-full bg-stitch-accent animate-pulse"></span>
            <span class="text-sm text-stitch-accent font-medium">Nền tảng học tiếng Nhật #1 Việt Nam</span>
          </div>

          <h1 class="text-5xl sm:text-6xl lg:text-7xl font-stitch-serif font-bold text-white leading-tight mb-6">
            Học tiếng Nhật
            <span class="block text-stitch-accent italic">thật sự hiệu quả</span>
          </h1>

          <p class="text-lg text-white/70 mb-10 leading-relaxed max-w-xl">
            Từ N5 đến N1 — khóa học video, flashcard SRS, quiz JLPT, mini game, và cộng đồng học viên sôi động. Bắt đầu miễn phí ngay hôm nay.
          </p>

          <div class="flex flex-wrap gap-4">
            <router-link
              to="/courses"
              class="px-8 py-4 bg-stitch-primary text-white font-semibold rounded-xl hover:bg-stitch-primary/90 shadow-lg shadow-stitch-primary/30 text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring focus-visible:ring-offset-2 focus-visible:ring-offset-stitch-foreground transition-all"
            >
              Bắt đầu học miễn phí &rarr;
            </router-link>
            <router-link
              to="/courses"
              class="px-8 py-4 bg-white/10 backdrop-blur-sm text-white font-medium rounded-xl border border-white/20 hover:bg-white/20 text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-white transition-all"
            >
              Xem các khóa học
            </router-link>
          </div>

          <div class="flex flex-wrap gap-6 mt-12">
            <div v-for="s in stats" :key="s.label">
              <div class="text-2xl font-stitch-serif font-bold text-white">{{ s.value }}</div>
              <div class="text-sm text-white/50">{{ s.label }}</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features -->
    <section class="py-24 bg-stitch-background">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <p class="text-sm font-semibold text-stitch-primary uppercase tracking-widest mb-3 font-stitch-serif">Tại sao chọn BrianJP?</p>
          <h2 class="text-4xl font-stitch-serif font-bold text-stitch-foreground">
            Mọi thứ bạn cần để <span class="italic text-stitch-primary">thành thạo tiếng Nhật</span>
          </h2>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          <div v-for="f in features" :key="f.title" class="group p-6 bg-stitch-card rounded-2xl border border-stitch-border hover:border-stitch-primary/30 hover:shadow-lg hover:shadow-stitch-primary/5 transition-all card-lift">
            <div class="text-4xl mb-4">{{ f.icon }}</div>
            <h3 class="font-stitch-serif font-bold text-lg mb-2 text-stitch-card-foreground">{{ f.title }}</h3>
            <p class="text-sm text-stitch-muted-foreground leading-relaxed">{{ f.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Courses preview -->
    <section class="py-24 bg-stitch-secondary">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-end justify-between mb-12">
          <div>
            <p class="text-sm font-semibold text-stitch-primary uppercase tracking-widest mb-3 font-stitch-serif">Khóa học nổi bật</p>
            <h2 class="text-4xl font-stitch-serif font-bold text-stitch-secondary-foreground">Bắt đầu từ cấp độ của bạn</h2>
          </div>
          <router-link to="/courses" class="hidden sm:flex items-center gap-2 text-sm font-medium text-stitch-primary hover:underline focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-2 py-1">
            Xem tất cả &rarr;
          </router-link>
        </div>

        <!-- Loading / Error States -->
        <div v-if="isLoading" class="flex justify-center items-center py-20 text-stitch-muted-foreground">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-stitch-primary"></div>
        </div>
        <div v-else-if="errorMsg" class="text-center py-20 text-red-500">
          <p class="mb-4">{{ errorMsg }}</p>
          <button @click="fetchCourses" class="px-4 py-2 bg-stitch-primary text-white rounded-lg hover:bg-stitch-primary/90">Thử lại</button>
        </div>
        <div v-else-if="courses.length === 0" class="text-center py-20 text-stitch-muted-foreground">
          Chưa có khóa học nào được xuất bản.
        </div>
        
        <!-- Course Grid -->
        <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <router-link 
            v-for="c in courses" 
            :key="c.id"
            :to="`/courses/${c.slug}`"
            class="group bg-stitch-card rounded-2xl overflow-hidden border border-stitch-border hover:shadow-xl hover:shadow-black/5 transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring"
          >
            <div class="relative h-48 overflow-hidden bg-stitch-muted">
              <img v-if="c.thumbnailUrl" :src="c.thumbnailUrl" :alt="c.title" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" @error="onImgError" />
              <div v-else class="w-full h-full flex items-center justify-center bg-stitch-muted group-hover:scale-105 transition-transform duration-500">
                <span class="text-4xl font-bold text-stitch-muted-foreground">{{ c.level || 'JP' }}</span>
              </div>
              
              <div class="absolute top-3 left-3 flex gap-2">
                <span v-if="c.level" class="px-2.5 py-1 bg-stitch-foreground text-white text-xs font-bold rounded-full">JLPT {{ c.level }}</span>
                <span :class="['px-2.5 py-1 text-xs font-bold rounded-full text-white', c.courseType === 'FREE' ? 'bg-green-500' : 'bg-stitch-primary']">
                  {{ c.courseType === 'FREE' ? 'Miễn phí' : 'Trả phí' }}
                </span>
              </div>
            </div>
            <div class="p-5 flex flex-col h-[calc(100%-12rem)]">
              <h3 class="font-stitch-serif font-bold text-lg mb-2 leading-snug text-stitch-card-foreground line-clamp-2">{{ c.title }}</h3>
              <div class="flex items-center gap-3 text-sm text-stitch-muted-foreground mb-4">
                <span>⭐ {{ c.averageRating?.toFixed(1) || '0.0' }}</span>
                <span>•</span>
                <span>{{ c.totalLessons || 0 }} bài</span>
                <span>•</span>
                <span>{{ (c.totalStudents || 0).toLocaleString() }} học viên</span>
              </div>
              <div class="mt-auto flex items-center justify-between border-t border-stitch-border pt-3">
                <div>
                  <span v-if="c.courseType === 'FREE'" class="font-bold text-green-600">Miễn phí</span>
                  <div v-else class="flex flex-col">
                    <span v-if="c.salePrice > 0 && c.salePrice < c.originalPrice" class="text-xs line-through text-stitch-muted-foreground">{{ formatPrice(c.originalPrice) }}</span>
                    <span class="font-bold text-lg text-stitch-card-foreground">{{ formatPrice(c.salePrice > 0 ? c.salePrice : c.originalPrice) }}</span>
                  </div>
                </div>
                <button class="px-4 py-2 bg-stitch-primary text-white text-sm font-medium rounded-lg hover:bg-stitch-primary/90 transition-colors">
                  {{ c.courseType === 'FREE' ? 'Học ngay' : 'Mua ngay' }}
                </button>
              </div>
            </div>
          </router-link>
        </div>

        <div class="text-center mt-10">
          <router-link
            to="/courses"
            class="inline-block px-8 py-3 border-2 border-stitch-primary text-stitch-primary font-semibold rounded-xl hover:bg-stitch-primary hover:text-white transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring focus-visible:ring-offset-2 focus-visible:ring-offset-stitch-secondary"
          >
            Xem tất cả khóa học
          </router-link>
        </div>
      </div>
    </section>

    <!-- Gamification strip -->
    <section class="py-20 bg-stitch-foreground text-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
          <div>
            <p class="text-sm font-semibold text-stitch-accent uppercase tracking-widest mb-3 font-stitch-serif">Game hóa việc học</p>
            <h2 class="text-4xl font-stitch-serif font-bold mb-6 leading-tight">
              Học mỗi ngày như <span class="italic text-stitch-accent">chơi game</span>
            </h2>
            <p class="text-white/60 mb-8 leading-relaxed">
              Tích điểm XP sau mỗi bài học, duy trì streak hằng ngày, mở khóa huy hiệu thành tích, và leo lên bảng xếp hạng toàn quốc.
            </p>
            <div class="grid grid-cols-2 gap-4 mb-8">
              <div v-for="g in gamification" :key="g.label" class="flex items-center gap-3 p-3 bg-white/5 rounded-xl border border-white/10 hover:bg-white/10 transition-colors">
                <span class="text-2xl">{{ g.icon }}</span>
                <div>
                  <div class="font-semibold text-sm">{{ g.label }}</div>
                  <div class="text-xs text-white/50">{{ g.desc }}</div>
                </div>
              </div>
            </div>
          </div>

          <div class="relative">
            <div class="bg-white/5 backdrop-blur-sm rounded-2xl border border-white/10 p-6 shadow-2xl">
              <div class="flex items-center gap-3 mb-6">
                <div class="w-12 h-12 rounded-full bg-stitch-accent flex items-center justify-center font-bold text-lg text-white">YT</div>
                <div>
                  <div class="font-semibold">Yuki Tanaka</div>
                  <div class="text-sm text-white/50">Cấp độ 24 • N3 Learner</div>
                </div>
                <div class="ml-auto text-right">
                  <div class="text-2xl font-stitch-serif font-bold text-stitch-accent">8,450</div>
                  <div class="text-xs text-white/50">XP tổng</div>
                </div>
              </div>

              <div class="mb-4">
                <div class="flex justify-between text-xs mb-1">
                  <span class="text-white/60">Tiến đến cấp 25</span>
                  <span class="text-white/60">8,450 / 10,000 XP</span>
                </div>
                <div class="h-2 bg-white/10 rounded-full overflow-hidden">
                  <div class="h-full bg-gradient-to-r from-stitch-primary to-stitch-accent rounded-full w-[84.5%]"></div>
                </div>
              </div>

              <div class="grid grid-cols-3 gap-3 mb-6">
                <div class="text-center p-3 bg-orange-500/10 rounded-xl border border-orange-500/20">
                  <div class="text-2xl mb-1">🔥</div>
                  <div class="font-bold text-orange-400">47</div>
                  <div class="text-xs text-white/50">ngày streak</div>
                </div>
                <div class="text-center p-3 bg-blue-500/10 rounded-xl border border-blue-500/20">
                  <div class="text-2xl mb-1">📚</div>
                  <div class="font-bold text-blue-400">62</div>
                  <div class="text-xs text-white/50">bài học</div>
                </div>
                <div class="text-center p-3 bg-green-500/10 rounded-xl border border-green-500/20">
                  <div class="text-2xl mb-1">🏅</div>
                  <div class="font-bold text-green-400">12</div>
                  <div class="text-xs text-white/50">huy hiệu</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Testimonials -->
    <section class="py-24 bg-stitch-background">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <p class="text-sm font-semibold text-stitch-primary uppercase tracking-widest mb-3 font-stitch-serif">Học viên nói gì</p>
          <h2 class="text-4xl font-stitch-serif font-bold text-stitch-foreground">Hàng ngàn người đã thành công</h2>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div v-for="t in testimonials" :key="t.name" class="p-6 bg-stitch-card rounded-2xl border border-stitch-border shadow-sm hover:shadow-md transition-shadow">
            <div class="flex text-stitch-accent mb-4 text-lg">★★★★★</div>
            <p class="text-sm text-stitch-muted-foreground leading-relaxed mb-6 italic">"{{ t.text }}"</p>
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-full bg-gradient-to-br from-stitch-primary to-stitch-accent flex items-center justify-center text-white text-sm font-bold">
                {{ t.avatar }}
              </div>
              <div>
                <div class="font-semibold text-sm text-stitch-card-foreground">{{ t.name }}</div>
                <div class="text-xs text-stitch-muted-foreground">{{ t.role }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Banner -->
    <section class="py-24 bg-gradient-to-br from-stitch-primary to-[#8b1038] text-white">
      <div class="max-w-3xl mx-auto px-4 text-center">
        <div class="text-6xl mb-6">🌸</div>
        <h2 class="text-4xl font-stitch-serif font-bold mb-4">Bắt đầu hành trình tiếng Nhật của bạn</h2>
        <p class="text-white/70 mb-8 text-lg">Tham gia cùng 50,000+ học viên. Đăng ký miễn phí và học ngay hôm nay.</p>
        <div class="flex flex-col sm:flex-row gap-4 justify-center">
          <router-link
            to="/register"
            class="px-8 py-4 bg-white text-stitch-primary font-bold rounded-xl hover:bg-white/90 shadow-xl focus:outline-none focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-offset-2 focus-visible:ring-offset-stitch-primary transition-colors"
          >
            Đăng ký miễn phí &rarr;
          </router-link>
          <router-link
            to="/courses"
            class="px-8 py-4 bg-white/10 text-white font-medium rounded-xl border border-white/30 hover:bg-white/20 focus:outline-none focus-visible:ring-2 focus-visible:ring-white transition-colors"
          >
            Xem tất cả khóa học
          </router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { CourseService } from '@/services/course.service'
import { getApiErrorMessage } from '@/utils/api-error'

// Data state
const courses = ref([])
const isLoading = ref(true)
const errorMsg = ref('')

// Static content matching Stitch design
const stats = [
  { value: '50,000+', label: 'Học viên đang học' },
  { value: '200+', label: 'Bài học video' },
  { value: '10,000+', label: 'Flashcard từ vựng' },
  { value: '4.9★', label: 'Đánh giá trung bình' },
]

const features = [
  { icon: '📹', title: 'Video bài giảng HD', desc: 'Bài học video chất lượng cao, có phụ đề song ngữ Nhật–Việt.' },
  { icon: '🎴', title: 'Flashcard thông minh', desc: 'Hệ thống SRS giúp ghi nhớ từ vựng & Kanji hiệu quả lâu dài.' },
  { icon: '🎮', title: 'Game hóa việc học', desc: 'Tích điểm XP, streak hằng ngày, huy hiệu và bảng xếp hạng.' },
  { icon: '📝', title: 'Quiz & luyện đề JLPT', desc: 'Ngân hàng câu hỏi JLPT N5–N1 với giải thích chi tiết.' },
  { icon: '📊', title: 'Theo dõi tiến độ', desc: 'Dashboard cá nhân theo dõi tiến trình học mỗi ngày.' },
  { icon: '🤖', title: 'AI hỗ trợ học tập', desc: 'AI giải đáp câu hỏi ngữ pháp và phát âm tức thì.' },
]

const gamification = [
  { icon: '⚡', label: 'Điểm XP', desc: 'Tích lũy sau mỗi bài' },
  { icon: '🔥', label: 'Streak', desc: 'Duy trì chuỗi học' },
  { icon: '🏅', label: 'Huy hiệu', desc: '50+ thành tích' },
  { icon: '🏆', label: 'Xếp hạng', desc: 'Top học viên' },
]

const testimonials = [
  { name: 'Nguyễn Thanh Hương', role: 'Đã thi đậu JLPT N3', avatar: 'NH', text: 'BrianJP đã giúp mình đậu N3 chỉ sau 8 tháng. Flashcard và quiz rất hiệu quả, giao diện đẹp dễ dùng.' },
  { name: 'Trần Minh Khoa', role: 'Đang học N2', avatar: 'TK', text: 'Hệ thống game hóa cực kỳ thú vị, mình học mỗi ngày như chơi game mà không chán. Streak của mình đã 120 ngày rồi!' },
  { name: 'Lê Thu Thảo', role: 'Học viên N5', avatar: 'LT', text: 'Khóa N5 miễn phí mà chất lượng rất tốt, giải thích rõ ràng. Mình sẽ tiếp tục học N4 với BrianJP.' },
]

// Methods
const formatPrice = (price) => {
  if (!price || price <= 0) return '0đ'
  return new Intl.NumberFormat('vi-VN').format(price) + 'đ'
}

const onImgError = (e) => {
  e.target.style.display = 'none'
}

const fetchCourses = async () => {
  isLoading.value = true
  errorMsg.value = ''
  try {
    const res = await CourseService.getCourses({ page: 0, size: 3 })
    if (res.data.code === 1000) {
      courses.value = res.data.result.content || []
    }
  } catch (error) {
    errorMsg.value = getApiErrorMessage(error, 'Không thể tải danh sách khóa học.')
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchCourses()
})
</script>
