<template>
  <div class="dashboard">
    <h1 class="page-title">Bảng điều khiển học tập</h1>
    <p class="page-subtitle">Theo dõi tiến độ và tiếp tục hành trình học tập của bạn.</p>

    <!-- Loading State -->
    <div v-if="isLoading" class="loading-container">
      <div class="spinner"></div>
      <p>Đang tải dữ liệu học tập...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="errorMsg" class="error-container">
      <p>{{ errorMsg }}</p>
      <button @click="fetchData" class="btn-retry">Thử lại</button>
    </div>

    <!-- Main Content -->
    <template v-else>
      <!-- Progress Overview Cards -->
      <section class="stats-section">
        <ProgressOverviewCard
          label="Khóa học đang học"
          :value="progress.totalEnrolledCourses"
          icon="JP"
          color="#3B82F6"
        />
        <ProgressOverviewCard
          label="Bài học đã hoàn thành"
          :value="progress.totalCompletedLessons"
          icon="OK"
          color="#10B981"
        />
        <ProgressOverviewCard
          label="Tiến độ tổng thể"
          :value="progress.overallProgressPercent"
          icon="%"
          color="#8B5CF6"
          :isPercent="true"
        />
      </section>

      <!-- My Courses Section -->
      <section class="courses-section">
        <h2 class="section-title">Khóa học của tôi</h2>

        <!-- Empty State -->
        <div v-if="courses.length === 0" class="empty-state">
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
      </section>
    </template>
  </div>
</template>

<script setup>
  import { ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { StudentService } from '@/services/student.service'
  import ProgressOverviewCard from '@/components/student/ProgressOverviewCard.vue'
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
    // Navigate to the course's last lesson or course detail page
    if (course.lastLessonId) {
      router.push(`/student/lessons/${course.lastLessonId}`)
    } else {
      router.push(course.slug ? `/courses/${course.slug}` : '/courses')
    }
  }

  onMounted(fetchData)
</script>

<style scoped>
.dashboard {
  max-width: 1100px;
}
.page-title {
  font-size: 1.625rem;
  font-weight: 700;
  color: var(--text-color);
  margin-bottom: 0.25rem;
}
.page-subtitle {
  font-size: 0.9rem;
  color: #6b7280;
  margin-bottom: 2rem;
}

/* Loading */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
  gap: 1rem;
  color: #6b7280;
}
.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e5e7eb;
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Error */
.error-container {
  text-align: center;
  padding: 3rem 0;
  color: #ef4444;
}
.btn-retry {
  margin-top: 1rem;
  padding: 0.5rem 1.5rem;
  background-color: var(--primary-color);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.875rem;
}
.btn-retry:hover {
  background-color: #2563eb;
}

/* Stats Section */
.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.25rem;
  margin-bottom: 2.5rem;
}

/* Courses Section */
.courses-section {
  margin-top: 0.5rem;
}
.section-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 1.25rem;
  color: var(--text-color);
}
.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 3rem 1rem;
  background: var(--card-bg, #fff);
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}
.empty-state h3 {
  font-size: 1.125rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: var(--text-color);
}
.empty-state p {
  font-size: 0.875rem;
  color: #6b7280;
  margin-bottom: 1.5rem;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}
.btn-explore {
  display: inline-block;
  padding: 0.625rem 1.5rem;
  background: var(--primary-color);
  color: white;
  border-radius: 8px;
  font-weight: 500;
  font-size: 0.875rem;
  transition: background-color 0.2s;
}
.btn-explore:hover {
  background-color: #2563eb;
}
</style>
