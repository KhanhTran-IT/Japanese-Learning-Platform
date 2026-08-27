<template>
  <div class="max-w-7xl mx-auto">
    <div class="mb-10">
      <h1 class="font-headline-lg text-headline-lg-mobile md:text-headline-lg text-ink-black mb-2">Bảng điều khiển học tập</h1>
      <p class="font-body-md text-secondary">Theo dõi tiến độ và tiếp tục hành trình học tập của bạn.</p>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="loading-container flex flex-col items-center justify-center py-20 text-secondary">
      <span class="material-symbols-outlined animate-spin text-4xl mb-4">autorenew</span>
      <p class="font-body-md">Đang tải dữ liệu học tập...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="errorMsg" class="flex flex-col items-center justify-center py-20 text-error text-center">
      <span class="material-symbols-outlined text-5xl mb-4">error</span>
      <p class="font-body-md mb-6">{{ errorMsg }}</p>
      <button @click="fetchData" class="bg-primary text-on-primary px-6 py-2 rounded-xl font-button hover:opacity-90 transition-all">Thử lại</button>
    </div>

    <!-- Main Content -->
    <template v-else>
      <!-- Progress Overview Cards -->
      <section class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
        <div class="zen-card p-6 rounded-2xl flex items-center gap-6 group hover:border-primary/50 border-2 border-transparent transition-colors">
          <div class="w-14 h-14 rounded-full bg-secondary-container text-on-secondary-container flex items-center justify-center group-hover:scale-110 transition-transform">
            <span class="material-symbols-outlined text-2xl">school</span>
          </div>
          <div>
            <p class="font-label-sm text-secondary uppercase tracking-wider mb-1">Khóa học đang học</p>
            <p class="font-headline-md text-2xl text-ink-black">{{ progress.totalEnrolledCourses }}</p>
          </div>
        </div>

        <div class="zen-card p-6 rounded-2xl flex items-center gap-6 group hover:border-success-green/50 border-2 border-transparent transition-colors">
          <div class="w-14 h-14 rounded-full bg-success-green/20 text-success-green flex items-center justify-center group-hover:scale-110 transition-transform">
            <span class="material-symbols-outlined text-2xl">task_alt</span>
          </div>
          <div>
            <p class="font-label-sm text-secondary uppercase tracking-wider mb-1">Bài học hoàn thành</p>
            <p class="font-headline-md text-2xl text-ink-black">{{ progress.totalCompletedLessons }}</p>
          </div>
        </div>

        <div class="zen-card p-6 rounded-2xl flex items-center gap-6 group hover:border-tertiary/50 border-2 border-transparent transition-colors">
          <div class="w-14 h-14 rounded-full bg-tertiary-fixed text-on-tertiary-fixed-variant flex items-center justify-center group-hover:scale-110 transition-transform">
            <span class="material-symbols-outlined text-2xl">monitoring</span>
          </div>
          <div>
            <p class="font-label-sm text-secondary uppercase tracking-wider mb-1">Tiến độ tổng thể</p>
            <p class="font-headline-md text-2xl text-ink-black">{{ progress.overallProgressPercent }}%</p>
          </div>
        </div>
      </section>

      <!-- My Courses Section -->
      <section>
        <h2 class="font-headline-md text-xl text-ink-black mb-6">Khóa học của tôi</h2>

        <!-- Empty State -->
        <div v-if="courses.length === 0" class="zen-card p-12 text-center rounded-2xl flex flex-col items-center justify-center border-dashed border-2 border-outline-variant">
          <span class="material-symbols-outlined text-6xl text-surface-variant mb-4">menu_book</span>
          <h3 class="font-headline-md text-lg text-ink-black mb-2">Bạn chưa ghi danh khóa học nào</h3>
          <p class="font-body-md text-secondary mb-6 max-w-md">Hãy khám phá các khóa học hấp dẫn và bắt đầu hành trình học tiếng Nhật theo phong cách Zen ngay hôm nay!</p>
          <router-link to="/courses" class="bg-primary hover:bg-primary-container text-on-primary px-6 py-3 rounded-xl font-button transition-all hover:-translate-y-1 shadow-md hover:shadow-lg">
            Khám phá khóa học
          </router-link>
        </div>

        <!-- Courses Grid -->
        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          <MyCourseCard
            v-for="course in courses"
            :key="course.courseId"
            :course="course"
            @continue="handleContinue"
          />
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { StudentService } from '@/services/student.service'
import MyCourseCard from '@/components/student/MyCourseCard.vue'

const router = useRouter()

const isLoading = ref(true)
const errorMsg = ref('')
const progress = ref({
  totalEnrolledCourses: 0,
  totalCompletedLessons: 0,
  overallProgressPercent: 0
})
const courses = ref([])

const fetchData = async () => {
  isLoading.value = true
  errorMsg.value = ''

  try {
    const [progressRes, coursesRes] = await Promise.all([
      StudentService.getDashboardProgress(),
      StudentService.getMyCourses()
    ])

    if (progressRes.data.code === 1000) {
      progress.value = progressRes.data.result
    }
    if (coursesRes.data.code === 1000) {
      courses.value = coursesRes.data.result || []
    }
  } catch (error) {
    errorMsg.value = 'Không thể tải dữ liệu. Vui lòng thử lại sau.'
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

onMounted(fetchData)
</script>
