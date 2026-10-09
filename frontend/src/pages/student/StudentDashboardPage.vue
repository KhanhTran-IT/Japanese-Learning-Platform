<template>
  <div class="max-w-7xl mx-auto space-y-8">
    <!-- Loading State -->
    <div v-if="isLoading" class="flex flex-col items-center justify-center py-32 text-stitch-muted-foreground">
      <span class="material-symbols-outlined animate-spin text-4xl mb-4">autorenew</span>
      <p class="text-sm font-medium">Đang tải dữ liệu học tập...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="errorMsg" class="flex flex-col items-center justify-center py-20 text-red-500 text-center bg-red-50 rounded-[24px] border border-red-100">
      <span class="material-symbols-outlined text-5xl mb-4">error</span>
      <p class="font-medium mb-6">{{ errorMsg }}</p>
      <button @click="fetchData" class="bg-red-500 text-white px-6 py-2.5 rounded-xl font-medium hover:bg-red-600 transition-colors">Thử lại</button>
    </div>

    <!-- Main Content -->
    <template v-else>
      <!-- Header Banner (Stitch Dashboard Style) -->
      <div class="bg-stitch-foreground text-white rounded-[24px] overflow-hidden shadow-lg relative">
        <div class="p-6 md:p-10 relative z-10">
          <div class="flex flex-col sm:flex-row sm:items-center gap-6 mb-10">
            <div class="w-16 h-16 sm:w-20 sm:h-20 shrink-0 rounded-full bg-gradient-to-br from-stitch-primary to-stitch-accent flex items-center justify-center text-2xl sm:text-3xl font-bold border-4 border-white/10 shadow-inner">
              {{ userInitials }}
            </div>
            <div>
              <h1 class="text-2xl sm:text-3xl font-stitch-serif font-bold mb-2">Chào buổi sáng, {{ userName }}! 👋</h1>
              <p class="text-white/70 text-sm sm:text-base flex items-center gap-2">
                <span class="inline-flex w-2 h-2 rounded-full bg-green-400 animate-pulse"></span>
                Chúc bạn một ngày học tập hiệu quả
              </p>
            </div>
          </div>
          
          <div class="grid grid-cols-2 lg:grid-cols-3 gap-4">
            <div class="p-4 sm:p-5 rounded-2xl bg-gradient-to-br from-blue-500/20 to-indigo-500/10 border border-white/10 backdrop-blur-sm">
              <div class="text-2xl mb-1">📚</div>
              <div class="text-2xl sm:text-3xl font-stitch-serif font-bold">{{ progress.totalEnrolledCourses }}</div>
              <div class="text-xs sm:text-sm text-white/60 mt-1">Khóa học đang tham gia</div>
            </div>
            
            <div class="p-4 sm:p-5 rounded-2xl bg-gradient-to-br from-green-500/20 to-emerald-500/10 border border-white/10 backdrop-blur-sm">
              <div class="text-2xl mb-1">✅</div>
              <div class="text-2xl sm:text-3xl font-stitch-serif font-bold">{{ progress.totalCompletedLessons }}</div>
              <div class="text-xs sm:text-sm text-white/60 mt-1">Bài học đã hoàn thành</div>
            </div>
            
            <div class="p-4 sm:p-5 rounded-2xl bg-gradient-to-br from-yellow-500/20 to-amber-500/10 border border-white/10 backdrop-blur-sm col-span-2 lg:col-span-1">
              <div class="text-2xl mb-1">📈</div>
              <div class="text-2xl sm:text-3xl font-stitch-serif font-bold">{{ progress.overallProgressPercent }}%</div>
              <div class="text-xs sm:text-sm text-white/60 mt-1">Tiến độ tổng thể</div>
            </div>
          </div>
        </div>
        
        <!-- Decorative bg pattern -->
        <div class="absolute right-0 top-0 bottom-0 w-1/2 bg-gradient-to-l from-stitch-primary/30 to-transparent pointer-events-none mix-blend-overlay"></div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Main Column -->
        <div class="lg:col-span-2 space-y-8">
          
          <!-- Tiếp tục học -->
          <div class="bg-stitch-card rounded-[24px] border border-stitch-border p-6 sm:p-8">
            <div class="flex items-center justify-between mb-6">
              <h3 class="font-stitch-serif font-bold text-xl text-stitch-foreground">Tiếp tục học</h3>
              <router-link to="/student/my-courses" class="text-sm font-medium text-stitch-primary hover:underline">
                Xem tất cả
              </router-link>
            </div>
            
            <!-- Empty state -->
            <div v-if="courses.length === 0" class="text-center py-10 bg-stitch-muted rounded-2xl border border-dashed border-stitch-border">
              <span class="material-symbols-outlined text-4xl text-stitch-muted-foreground mb-3">menu_book</span>
              <p class="text-stitch-foreground font-medium mb-1">Chưa có khóa học nào</p>
              <p class="text-sm text-stitch-muted-foreground mb-4">Hãy ghi danh một khóa học để bắt đầu.</p>
              <router-link to="/courses" class="inline-block px-5 py-2 bg-stitch-primary text-white text-sm font-medium rounded-xl hover:bg-stitch-primary/90 transition-colors">
                Khám phá khóa học
              </router-link>
            </div>

            <div v-else class="space-y-4">
              <div 
                v-for="c in courses.slice(0, 3)" 
                :key="c.courseId"
                class="flex gap-4 sm:gap-5 cursor-pointer group p-3 sm:p-4 rounded-2xl hover:bg-stitch-muted transition-colors border border-transparent hover:border-stitch-border"
                @click="handleContinue(c)"
              >
                <div class="w-20 h-16 sm:w-28 sm:h-20 rounded-xl overflow-hidden bg-stitch-muted shrink-0 shadow-sm">
                  <img v-if="c.thumbnailUrl" :src="c.thumbnailUrl" :alt="c.courseName" loading="lazy" decoding="async" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                  <div v-else class="w-full h-full flex items-center justify-center bg-gradient-to-br from-stitch-primary/20 to-stitch-accent/20">
                    <span class="font-stitch-serif text-xl text-stitch-primary/50 font-bold">日</span>
                  </div>
                </div>
                <div class="flex-1 min-w-0 flex flex-col justify-center">
                  <div class="text-sm sm:text-base font-bold leading-tight group-hover:text-stitch-primary truncate mb-1 transition-colors">{{ c.courseName }}</div>
                  <div class="text-xs text-stitch-muted-foreground mb-2 truncate">Bài tiếp theo: {{ c.lastLessonName || 'Bắt đầu' }}</div>
                  
                  <div class="flex items-center gap-3">
                    <div class="flex-1 h-1.5 bg-stitch-border rounded-full overflow-hidden">
                      <div class="h-full bg-stitch-primary rounded-full transition-all" :style="{ width: `${getCourseProgress(c)}%` }" />
                    </div>
                    <span class="text-xs font-medium text-stitch-muted-foreground shrink-0">{{ Math.round(getCourseProgress(c)) }}%</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Sidebar Column -->
        <div class="space-y-8">
          <!-- Quiz gần đây -->
          <div class="bg-stitch-card rounded-[24px] border border-stitch-border p-6 sm:p-8">
            <h3 class="font-stitch-serif font-bold text-xl text-stitch-foreground mb-6">Quiz gần đây</h3>
            
            <div v-if="quizAttempts.length === 0" class="text-center py-8">
              <p class="text-sm text-stitch-muted-foreground">Bạn chưa làm bài quiz nào.</p>
            </div>
            
            <div v-else class="space-y-5">
              <router-link
                v-for="a in quizAttempts.slice(0, 5)"
                :key="a.attemptId"
                :to="`/student/quizzes/${a.quizId}/result/${a.attemptId}`"
                class="flex items-start gap-4 group cursor-pointer"
              >
                <div 
                  class="w-10 h-10 rounded-full flex items-center justify-center shrink-0 shadow-sm"
                  :class="a.passed ? 'bg-green-100 text-green-600' : 'bg-red-100 text-red-600'"
                >
                  <span class="material-symbols-outlined text-[20px]">{{ a.passed ? 'check_circle' : 'cancel' }}</span>
                </div>
                <div class="flex-1 min-w-0 pt-0.5">
                  <div class="text-sm font-medium text-stitch-foreground group-hover:text-stitch-primary truncate transition-colors">{{ a.quizTitle }}</div>
                  <div class="text-xs text-stitch-muted-foreground mt-1">{{ formatDateTime(a.submittedAt || a.startedAt) }}</div>
                </div>
                <div class="text-right shrink-0 pt-0.5">
                  <div class="text-sm font-bold" :class="a.passed ? 'text-green-600' : 'text-red-600'">{{ a.score }}đ</div>
                </div>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { StudentService } from '@/services/student.service'
import { QuizService } from '@/services/quiz.service'

const router = useRouter()
const authStore = useAuthStore()

const isLoading = ref(true)
const errorMsg = ref('')
const progress = ref({
  totalEnrolledCourses: 0,
  totalCompletedLessons: 0,
  overallProgressPercent: 0
})
const courses = ref([])
const quizAttempts = ref([])

const userName = computed(() => {
  const name = authStore.user?.fullName || 'Học viên'
  return name.split(' ').pop() // Get first name or last word
})

const userInitials = computed(() => {
  const name = authStore.user?.fullName || 'U'
  return name.substring(0, 2).toUpperCase()
})

const fetchData = async () => {
  isLoading.value = true
  errorMsg.value = ''

  try {
    const [progressRes, coursesRes, attemptsRes] = await Promise.all([
      StudentService.getDashboardProgress(),
      StudentService.getMyCourses(),
      QuizService.getMyQuizAttempts().catch(() => null)
    ])

    if (progressRes.data.code === 1000) {
      progress.value = progressRes.data.result
    }
    if (coursesRes.data.code === 1000) {
      courses.value = coursesRes.data.result || []
    }
    if (attemptsRes?.data?.code === 1000) {
      quizAttempts.value = attemptsRes.data.result || []
    }
  } catch (error) {
    errorMsg.value = 'Không thể kết nối đến máy chủ để lấy dữ liệu học tập.'
    console.error('Dashboard fetch error:', error)
  } finally {
    isLoading.value = false
  }
}

const handleContinue = (course) => {
  if (course.lastLessonId) {
    router.push(`/student/lessons/${course.lastLessonId}`)
  } else {
    router.push(course.slug ? `/courses/${course.slug}` : '/courses')
  }
}

const getCourseProgress = (course) => {
  if (typeof course.progressPercent === 'number') {
    return Math.min(Math.max(course.progressPercent, 0), 100)
  }
  const total = course.totalLessons || 0
  const completed = course.completedLessons || 0
  if (total === 0) return 0
  return Math.min((completed / total) * 100, 100)
}

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  try {
    const date = new Date(dateTimeStr)
    return date.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    })
  } catch {
    return dateTimeStr
  }
}

onMounted(fetchData)
</script>
