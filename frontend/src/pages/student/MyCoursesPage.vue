<template>
  <div class="max-w-7xl mx-auto">
    <div class="mb-10">
      <h1 class="font-headline-lg text-headline-lg-mobile md:text-headline-lg text-ink-black mb-2">Khóa học của tôi</h1>
      <p class="font-body-md text-secondary">Danh sách các khóa học bạn đã ghi danh và đang theo học.</p>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="flex flex-col items-center justify-center py-20 text-secondary">
      <span class="material-symbols-outlined animate-spin text-4xl mb-4">autorenew</span>
      <p class="font-body-md">Đang tải danh sách khóa học...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="errorMsg" class="flex flex-col items-center justify-center py-20 text-error text-center">
      <span class="material-symbols-outlined text-5xl mb-4">error</span>
      <p class="font-body-md mb-6">{{ errorMsg }}</p>
      <button @click="fetchMyCourses" class="bg-primary text-on-primary px-6 py-2 rounded-xl font-button hover:opacity-90 transition-all">Thử lại</button>
    </div>

    <!-- Main Content -->
    <template v-else>
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
const courses = ref([])

const fetchMyCourses = async () => {
  isLoading.value = true
  errorMsg.value = ''

  try {
    const res = await StudentService.getMyCourses()
    if (res.data && res.data.code === 1000) {
      courses.value = res.data.result || []
    }
  } catch (error) {
    errorMsg.value = 'Không thể tải danh sách khóa học. Vui lòng thử lại sau.'
    console.error('My courses fetch error:', error)
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

onMounted(() => {
  fetchMyCourses()
})
</script>
