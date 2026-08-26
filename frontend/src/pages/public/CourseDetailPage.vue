<template>
  <div class="course-detail-page">
    <!-- Breadcrumb & Back Link -->
    <div class="page-navigation">
      <router-link to="/courses" class="back-link">
        &larr; Quay lại danh sách khóa học
      </router-link>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="state-container loading-state">
      <div class="spinner"></div>
      <p>Đang tải thông tin khóa học...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="errorMsg" class="state-container error-state">
      <h2 class="error-title">Oops! Đã xảy ra lỗi</h2>
      <p class="error-text">{{ errorMsg }}</p>
      <div class="error-actions">
        <button class="btn-retry" @click="fetchCourseDetail">Thử lại</button>
        <router-link to="/courses" class="btn-secondary">Về danh sách</router-link>
      </div>
    </div>

    <!-- Main Content -->
    <template v-else-if="course">
      <div class="course-content-grid">
        <!-- Main Column (Left) -->
        <div class="main-column">
          <div class="course-header">
            <div class="badges">
              <span v-if="course.level" class="badge badge-level">{{ course.level }}</span>
              <span v-if="course.courseType === 'FREE'" class="badge badge-free">Miễn phí</span>
            </div>
            <h1 class="course-title">{{ course.title }}</h1>
            <p class="course-short-desc">{{ course.shortDescription }}</p>

            <div class="course-meta">
              <span class="meta-item">
                <strong>{{ course.totalLessons || 0 }}</strong> bài học
              </span>
              <span class="meta-item">
                <strong>{{ formatDuration(course.totalDurationMinutes) }}</strong>
              </span>
              <span class="meta-item">
                <strong>{{ course.totalStudents || 0 }}</strong> học viên
              </span>
              <span v-if="course.averageRating" class="meta-item">
                Đánh giá: <strong>{{ course.averageRating.toFixed(1) }}/5</strong>
              </span>
            </div>

            <div v-if="course.teacherName" class="teacher-info">
              <img 
                v-if="course.teacherAvatarUrl" 
                :src="course.teacherAvatarUrl" 
                alt="Teacher Avatar" 
                class="teacher-avatar"
                @error="onImgError"
              />
              <div v-else class="teacher-avatar-placeholder">
                {{ course.teacherName.charAt(0).toUpperCase() }}
              </div>
              <div class="teacher-details">
                <span class="teacher-label">Giảng viên</span>
                <span class="teacher-name">{{ course.teacherName }}</span>
              </div>
            </div>
          </div>

          <div class="content-section">
            <h2 class="section-title">Giới thiệu khóa học</h2>
            <div class="course-description">{{ formattedDescription }}</div>
          </div>

          <div class="content-section" v-if="course.sections && course.sections.length > 0">
            <h2 class="section-title">Nội dung chương trình</h2>
            <div class="curriculum">
              <div v-for="(section, idx) in course.sections" :key="section.id" class="section-item">
                <div class="section-header">
                  <span class="section-number">Chương {{ idx + 1 }}:</span>
                  <span class="section-name">{{ section.title }}</span>
                  <span class="section-lessons-count">{{ section.lessons?.length || 0 }} bài</span>
                </div>
                
                <div class="lessons-list" v-if="section.lessons && section.lessons.length > 0">
                  <div v-for="lesson in section.lessons" :key="lesson.id" class="lesson-item" @click="handleLessonClick(lesson)" :class="{ 'clickable': lesson.isPreview || isEnrolled }">
                    <div class="lesson-info">
                      <span class="lesson-icon">Bài</span>
                      <span class="lesson-title">{{ lesson.title }}</span>
                    </div>
                    <div class="lesson-meta">
                      <span v-if="lesson.isPreview" class="badge-preview">Học thử</span>
                    </div>
                  </div>
                </div>
                <div v-else class="empty-lessons">Chưa có bài học nào.</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Sidebar Column (Right) -->
        <div class="sidebar-column">
          <div class="enrollment-card">
            <div class="course-thumbnail">
              <img 
                v-if="course.thumbnailUrl" 
                :src="course.thumbnailUrl" 
                :alt="course.title" 
                @error="onImgError"
              />
              <div v-else class="thumb-placeholder">
                <span>{{ course.level || 'JP' }}</span>
              </div>
            </div>
            
            <div class="enrollment-body">
              <div class="price-box">
                <template v-if="course.courseType === 'FREE'">
                  <div class="price-free-large">Miễn phí</div>
                </template>
                <template v-else>
                  <div class="price-current">
                    {{ formatPrice(course.salePrice > 0 ? course.salePrice : course.originalPrice) }}
                  </div>
                  <div v-if="course.salePrice > 0 && course.salePrice < course.originalPrice" class="price-original">
                    {{ formatPrice(course.originalPrice) }}
                  </div>
                </template>
              </div>

              <button
                v-if="course.courseType === 'FREE'"
                class="btn-enroll btn-free"
                @click="handleEnroll"
                :disabled="isEnrolling || isEnrolled || !course.id"
              >
                {{ isEnrolling ? 'Đang xử lý...' : (isEnrolled ? 'Đã ghi danh' : 'Đăng ký học miễn phí') }}
              </button>
              <button v-else class="btn-enroll btn-paid" disabled>
                Mua khóa học
              </button>
              <p v-if="enrollSuccessMsg" class="enroll-success">{{ enrollSuccessMsg }}</p>
              <p v-else-if="enrollErrorMsg" class="enroll-error">{{ enrollErrorMsg }}</p>
              <p v-else-if="course.courseType === 'FREE'" class="enroll-note">* Đăng nhập bằng tài khoản học viên để ghi danh.</p>
              <p v-else class="enroll-note">* Thanh toán khóa học trả phí đang được phát triển.</p>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CourseService } from '@/services/course.service'
import { getApiErrorMessage } from '@/utils/api-error'
import { useAuthStore } from '@/stores/auth.store'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const course = ref(null)
const isLoading = ref(true)
const errorMsg = ref('')

const isEnrolling = ref(false)
const isEnrolled = ref(false)
const enrollErrorMsg = ref('')
const enrollSuccessMsg = ref('')

const fetchCourseDetail = async () => {
  const slug = route.params.slug
  if (!slug) return

  isLoading.value = true
  errorMsg.value = ''
  
  try {
    const res = await CourseService.getCourseBySlug(slug)
    if (res.data && res.data.code === 1000) {
      course.value = res.data.result
    }
  } catch (error) {
    if (error.response && error.response.status === 404) {
      errorMsg.value = 'Không tìm thấy khóa học này. Có thể đường dẫn không đúng hoặc khóa học đã bị xóa.'
    } else {
      errorMsg.value = getApiErrorMessage(error, 'Không thể tải thông tin khóa học.')
    }
  } finally {
    isLoading.value = false
  }
}

const handleEnroll = async () => {
  enrollErrorMsg.value = ''
  enrollSuccessMsg.value = ''

  if (!authStore.isAuthenticated) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  const userRoles = authStore.user?.roles || []
  if (userRoles.length > 0 && !userRoles.includes('STUDENT')) {
    enrollErrorMsg.value = 'Chỉ tài khoản học viên mới có thể ghi danh khóa học.'
    return
  }

  isEnrolling.value = true
  try {
    const res = await CourseService.enrollFreeCourse(course.value.id)
    if (res.data && res.data.code === 1000) {
      isEnrolled.value = true
      enrollSuccessMsg.value = 'Ghi danh thành công! Đang chuyển hướng...'
      setTimeout(() => {
        router.push('/student/dashboard')
      }, 1500)
    }
  } catch (error) {
    const backendMsg = getApiErrorMessage(error, 'Lỗi ghi danh khóa học.')
    enrollErrorMsg.value = backendMsg
    if (backendMsg.includes('đã ghi danh')) {
      isEnrolled.value = true
    }
  } finally {
    isEnrolling.value = false
  }
}

const handleLessonClick = (lesson) => {
  if (isEnrolled.value || lesson.isPreview) {
    if (!authStore.isAuthenticated) {
      // Must login even for preview
      router.push({ path: '/login', query: { redirect: `/student/lessons/${lesson.id}` } })
    } else {
      router.push(`/student/lessons/${lesson.id}`)
    }
  }
}

onMounted(() => {
  fetchCourseDetail()
})

// Re-fetch if URL slug changes while on the same component
watch(() => route.params.slug, (newSlug) => {
  if (newSlug) fetchCourseDetail()
})

// Computed
const formattedDescription = computed(() => {
  if (!course.value?.description) return 'Chưa có thông tin mô tả chi tiết.'
  return course.value.description
})

// Helpers
const formatDuration = (minutes) => {
  if (!minutes || minutes <= 0) return '0 phút'
  if (minutes < 60) return `${minutes} phút`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m > 0 ? `${h}h ${m}p` : `${h} giờ`
}

const formatPrice = (price) => {
  if (!price || price <= 0) return '0đ'
  return new Intl.NumberFormat('vi-VN').format(price) + 'đ'
}

const onImgError = (e) => {
  e.target.style.display = 'none'
}
</script>

<style scoped>
.course-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem;
}

.page-navigation {
  margin-bottom: 1.5rem;
}

.back-link {
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
}
.back-link:hover {
  text-decoration: underline;
}

/* States */
.state-container {
  padding: 5rem 1rem;
  text-align: center;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f1f5f9;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 1rem;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.error-title {
  font-size: 1.5rem;
  color: #0f172a;
  margin-bottom: 0.5rem;
}
.error-text {
  color: #ef4444;
  margin-bottom: 1.5rem;
}
.error-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}
.btn-retry {
  padding: 0.625rem 1.25rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
}
.btn-secondary {
  padding: 0.625rem 1.25rem;
  background: #f1f5f9;
  color: #334155;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  text-decoration: none;
}
.btn-retry:hover { background: #2563eb; }
.btn-secondary:hover { background: #e2e8f0; }

/* Grid Layout */
.course-content-grid {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 2rem;
  align-items: start;
}

/* Main Column */
.main-column {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.course-header {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
.badges {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}
.badge {
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.8rem;
  font-weight: 600;
}
.badge-level {
  background: #e0e7ff;
  color: #4338ca;
}
.badge-free {
  background: #dcfce7;
  color: #15803d;
}

.course-title {
  font-size: 2.2rem;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.3;
  margin-bottom: 1rem;
}
.course-short-desc {
  font-size: 1.1rem;
  color: #475569;
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.course-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}
.meta-item {
  color: #64748b;
  font-size: 0.95rem;
}
.meta-item strong {
  color: #0f172a;
}

.teacher-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}
.teacher-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}
.teacher-avatar-placeholder {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: #3b82f6;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  font-weight: bold;
}
.teacher-details {
  display: flex;
  flex-direction: column;
}
.teacher-label {
  font-size: 0.8rem;
  color: #64748b;
}
.teacher-name {
  font-weight: 600;
  color: #0f172a;
}

.content-section {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 1.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #e2e8f0;
}
.course-description {
  color: #334155;
  line-height: 1.7;
  font-size: 1.05rem;
  white-space: pre-line;
}

/* Curriculum */
.curriculum {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.section-item {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}
.section-header {
  background: #f8fafc;
  padding: 1rem 1.25rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  border-bottom: 1px solid #e2e8f0;
}
.section-number {
  font-weight: 600;
  color: #475569;
}
.section-name {
  font-weight: 600;
  color: #0f172a;
  flex: 1;
}
.section-lessons-count {
  font-size: 0.85rem;
  color: #64748b;
}

.lessons-list {
  display: flex;
  flex-direction: column;
}
.lesson-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.875rem 1.25rem;
  border-bottom: 1px solid #f1f5f9;
  background: white;
}
.lesson-item.clickable {
  cursor: pointer;
}
.lesson-item.clickable:hover {
  background: #f8fafc;
}
.lesson-item:last-child {
  border-bottom: none;
}
.lesson-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
.lesson-icon {
  color: #64748b;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
}
.lesson-title {
  color: #334155;
}
.badge-preview {
  font-size: 0.75rem;
  background: #fef08a;
  color: #854d0e;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-weight: 600;
}
.empty-lessons {
  padding: 1rem;
  color: #94a3b8;
  font-style: italic;
  text-align: center;
}


/* Sidebar */
.enrollment-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  position: sticky;
  top: 2rem;
}
.course-thumbnail {
  aspect-ratio: 16 / 9;
  background: #f1f5f9;
  position: relative;
}
.course-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 3rem;
  font-weight: 800;
}

.enrollment-body {
  padding: 1.5rem;
}
.price-box {
  margin-bottom: 1.5rem;
}
.price-free-large {
  font-size: 2rem;
  font-weight: 800;
  color: #22c55e;
}
.price-current {
  font-size: 2rem;
  font-weight: 800;
  color: #0f172a;
}
.price-original {
  font-size: 1.1rem;
  color: #94a3b8;
  text-decoration: line-through;
  margin-top: 0.25rem;
}

.btn-enroll {
  width: 100%;
  padding: 1rem;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  color: white;
  margin-bottom: 0.75rem;
}
.btn-enroll:disabled {
  cursor: not-allowed;
  opacity: 0.75;
}
.btn-free {
  background: #10b981;
}
.btn-free:hover {
  background: #059669;
}
.btn-paid {
  background: #3b82f6;
}
.btn-paid:hover {
  background: #2563eb;
}
.enroll-note {
  text-align: center;
  font-size: 0.8rem;
  color: #94a3b8;
}
.enroll-success {
  text-align: center;
  font-size: 0.9rem;
  color: #10b981;
  margin-top: 0.5rem;
  font-weight: 500;
}
.enroll-error {
  text-align: center;
  font-size: 0.9rem;
  color: #ef4444;
  margin-top: 0.5rem;
  font-weight: 500;
}

/* Responsive */
@media (max-width: 992px) {
  .course-content-grid {
    grid-template-columns: 1fr;
  }
  .enrollment-card {
    position: static;
    margin-bottom: 2rem;
  }
}
@media (max-width: 640px) {
  .course-title {
    font-size: 1.8rem;
  }
  .course-meta {
    flex-direction: column;
    gap: 0.5rem;
  }
  .course-header, .content-section, .enrollment-body {
    padding: 1.25rem;
  }
}
</style>
