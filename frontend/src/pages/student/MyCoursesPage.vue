<template>
  <div class="my-courses-page">
    <div class="page-header">
      <h1 class="page-title">Khóa học của tôi</h1>
      <p class="page-subtitle">Danh sách các khóa học bạn đã ghi danh và đang theo học.</p>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="loading-container">
      <div class="spinner"></div>
      <p>Đang tải danh sách khóa học...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="errorMsg" class="error-container">
      <p>⚠️ {{ errorMsg }}</p>
      <button @click="fetchMyCourses" class="btn-retry">Thử lại</button>
    </div>

    <!-- Main Content -->
    <template v-else>
      <!-- Empty State -->
      <div v-if="courses.length === 0" class="empty-state">
        <div class="empty-icon">🎓</div>
        <h3>Bạn chưa ghi danh khóa học nào</h3>
        <p>Hãy khám phá các khóa học hấp dẫn và bắt đầu hành trình học tiếng Nhật ngay hôm nay!</p>
        <router-link to="/courses" class="btn-explore">Khám phá khóa học</router-link>
      </div>

      <!-- Courses Grid -->
      <div v-else class="courses-grid">
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

<style scoped>
.my-courses-page {
  max-width: 1200px;
}
.page-header {
  margin-bottom: 2.5rem;
}
.page-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-color, #1F2937);
  margin-bottom: 0.5rem;
}
.page-subtitle {
  font-size: 1rem;
  color: #6b7280;
}

/* Loading */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 5rem 0;
  gap: 1rem;
  color: #6b7280;
}
.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e5e7eb;
  border-top-color: var(--primary-color, #3B82F6);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Error */
.error-container {
  text-align: center;
  padding: 4rem 0;
  color: #ef4444;
}
.btn-retry {
  margin-top: 1.25rem;
  padding: 0.625rem 1.5rem;
  background-color: var(--primary-color, #3B82F6);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: background-color 0.2s;
}
.btn-retry:hover {
  background-color: #2563eb;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: var(--card-bg, #fff);
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}
.empty-state h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  color: var(--text-color, #1F2937);
}
.empty-state p {
  font-size: 0.95rem;
  color: #6b7280;
  margin-bottom: 2rem;
  max-width: 450px;
  margin-left: auto;
  margin-right: auto;
}
.btn-explore {
  display: inline-block;
  padding: 0.75rem 1.75rem;
  background: var(--primary-color, #3B82F6);
  color: white;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.95rem;
  text-decoration: none;
  transition: background-color 0.2s;
}
.btn-explore:hover {
  background-color: #2563eb;
}

/* Courses Grid */
.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.75rem;
}
</style>
