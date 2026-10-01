<template>
  <div class="min-h-screen bg-stitch-background font-stitch-sans">
    <!-- Loading State -->
    <div v-if="isLoading" class="flex justify-center items-center h-screen text-stitch-muted-foreground">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-stitch-primary"></div>
    </div>

    <!-- Error State -->
    <div v-else-if="errorMsg" class="flex flex-col items-center justify-center py-20 text-center">
      <div class="text-5xl mb-4">⚠️</div>
      <h3 class="font-stitch-serif font-bold text-2xl mb-4 text-stitch-foreground">Đã xảy ra lỗi</h3>
      <p class="text-stitch-muted-foreground mb-8">{{ errorMsg }}</p>
      <div class="flex gap-4">
        <button class="bg-stitch-primary text-white px-6 py-3 rounded-xl hover:bg-stitch-primary/90 transition-colors font-medium" @click="fetchCourseDetail">Thử lại</button>
        <router-link to="/courses" class="border border-stitch-border text-stitch-muted-foreground px-6 py-3 rounded-xl hover:bg-stitch-muted transition-colors font-medium">Về danh sách</router-link>
      </div>
    </div>

    <!-- Main Content -->
    <template v-else-if="course">
      <!-- Hero -->
      <div class="bg-stitch-foreground text-white">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
          <router-link to="/courses" class="text-sm text-white/50 hover:text-white mb-6 flex items-center gap-1 transition-colors">
            ← Quay lại danh sách
          </router-link>
          
          <div class="grid grid-cols-1 lg:grid-cols-3 gap-12">
            <div class="lg:col-span-2">
              <div class="flex flex-wrap items-center gap-2 mb-4">
                <span v-if="course.level" class="px-2.5 py-1 bg-white/10 text-white text-xs font-bold rounded-full">JLPT {{ course.level }}</span>
                <span :class="['px-2.5 py-1 text-white text-xs font-bold rounded-full', course.courseType === 'FREE' ? 'bg-green-500' : 'bg-stitch-primary']">
                  {{ course.courseType === 'FREE' ? 'Miễn phí' : 'Trả phí' }}
                </span>
              </div>
              
              <h1 class="text-4xl font-stitch-serif font-bold mb-4 leading-tight">{{ course.title }}</h1>
              
              <p class="text-white/70 mb-6 leading-relaxed">
                {{ course.shortDescription || 'Chưa có thông tin mô tả ngắn.' }}
              </p>
              
              <div class="flex flex-wrap items-center gap-4 text-sm">
                <span class="text-amber-400 font-semibold">★ {{ course.averageRating?.toFixed(1) || '0.0' }}</span>
                <span class="text-white/50">({{ course.totalStudents || 0 }} học viên)</span>
                <span class="text-white/50">•</span>
                <span class="text-white/70">{{ course.totalLessons || 0 }} bài học</span>
                <span class="text-white/50">•</span>
                <span class="text-white/70">{{ formatDuration(course.totalDurationMinutes) }}</span>
              </div>
              
              <div class="flex items-center gap-3 mt-6" v-if="course.teacherName">
                <img 
                  v-if="course.teacherAvatarUrl" 
                  :src="course.teacherAvatarUrl" 
                  :alt="course.teacherName" 
                  class="w-10 h-10 rounded-full object-cover bg-white/10"
                  @error="onImgError"
                />
                <div v-else class="w-10 h-10 rounded-full bg-stitch-accent flex items-center justify-center text-white font-bold text-sm">
                  {{ course.teacherName.charAt(0).toUpperCase() }}
                </div>
                <div>
                  <div class="text-sm font-medium">{{ course.teacherName }}</div>
                  <div class="text-xs text-white/50">Sensei (Giảng viên)</div>
                </div>
              </div>
            </div>

            <!-- Enrollment card desktop -->
            <div class="hidden lg:block">
              <div class="bg-white rounded-2xl p-6 text-stitch-foreground sticky top-24 shadow-xl border border-stitch-border">
                <div class="aspect-video bg-stitch-muted rounded-xl mb-4 overflow-hidden relative group">
                  <img 
                    v-if="course.thumbnailUrl" 
                    :src="course.thumbnailUrl" 
                    :alt="course.title" 
                    class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105" 
                    @error="onImgError" 
                  />
                  <div v-else class="w-full h-full flex items-center justify-center bg-stitch-muted">
                    <span class="text-4xl font-bold text-stitch-muted-foreground">{{ course.level || 'JP' }}</span>
                  </div>
                </div>
                
                <div class="mb-4">
                  <template v-if="course.courseType === 'FREE'">
                    <div class="text-3xl font-stitch-serif font-bold text-green-600">Miễn phí</div>
                  </template>
                  <template v-else>
                    <div v-if="course.salePrice > 0 && course.salePrice < course.originalPrice" class="text-sm line-through text-stitch-muted-foreground mb-1">
                      {{ formatPrice(course.originalPrice) }}
                    </div>
                    <div class="text-3xl font-stitch-serif font-bold text-stitch-foreground">
                      {{ formatPrice(course.salePrice > 0 ? course.salePrice : course.originalPrice) }}
                    </div>
                  </template>
                </div>
                
                <!-- Action Button -->
                <div class="mb-3">
                  <template v-if="isEnrolled">
                    <button
                      class="w-full py-3.5 rounded-xl font-semibold text-sm transition-all bg-green-500 text-white hover:bg-green-600 shadow-md hover:shadow-lg active:scale-95 flex items-center justify-center gap-2"
                      @click="handleContinueLearning"
                    >
                      ▶ Tiếp tục học
                    </button>
                    <div class="text-center mt-2 text-xs text-green-600 font-medium">✓ Đã ghi danh</div>
                  </template>
                  
                  <template v-else>
                    <button 
                      v-if="!authStore.isAuthenticated"
                      class="w-full py-3.5 rounded-xl font-semibold text-sm transition-all bg-stitch-primary text-white hover:bg-stitch-primary/90 shadow-md hover:shadow-lg active:scale-95"
                      @click="handleEnroll"
                    >
                      Đăng nhập để học
                    </button>
                    <button 
                      v-else-if="course.courseType === 'FREE'"
                      class="w-full py-3.5 rounded-xl font-semibold text-sm transition-all shadow-md hover:shadow-lg active:scale-95"
                      :class="isEnrolling ? 'bg-stitch-muted text-stitch-muted-foreground cursor-not-allowed' : 'bg-stitch-primary text-white hover:bg-stitch-primary/90'"
                      @click="handleEnroll"
                      :disabled="isEnrolling || !course.id"
                    >
                      {{ isEnrolling ? 'Đang xử lý...' : 'Đăng ký miễn phí' }}
                    </button>
                    <button 
                      v-else 
                      class="w-full py-3.5 rounded-xl font-semibold text-sm transition-all bg-stitch-muted text-stitch-muted-foreground cursor-not-allowed"
                      disabled
                    >
                      Mua khóa học
                    </button>
                  </template>
                </div>
                
                <p v-if="enrollSuccessMsg" class="text-xs text-green-600 bg-green-50 p-2 rounded-lg text-center mb-3">{{ enrollSuccessMsg }}</p>
                <p v-else-if="enrollErrorMsg" class="text-xs text-red-500 bg-red-50 p-2 rounded-lg text-center mb-3">{{ enrollErrorMsg }}</p>
                
                <div class="space-y-2 text-sm text-stitch-muted-foreground pt-4 border-t border-stitch-border">
                  <div class="flex items-center gap-2"><span class="text-green-500">✓</span><span>{{ course.totalLessons || 0 }} bài học video & quiz</span></div>
                  <div class="flex items-center gap-2"><span class="text-green-500">✓</span><span>Hỗ trợ giải đáp từ giảng viên</span></div>
                  <div class="flex items-center gap-2"><span class="text-green-500">✓</span><span>Truy cập trọn đời</span></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Content -->
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-12">
          <div class="lg:col-span-2 space-y-10">
            
            <!-- Description -->
            <div class="bg-white rounded-2xl border border-stitch-border p-6 shadow-sm">
              <h2 class="font-stitch-serif font-bold text-xl mb-4 text-stitch-foreground">Chi tiết khóa học</h2>
              <div class="prose max-w-none text-sm text-stitch-muted-foreground leading-relaxed whitespace-pre-line">
                {{ formattedDescription }}
              </div>
            </div>

            <!-- Syllabus -->
            <div v-if="course.sections && course.sections.length > 0">
              <h2 class="font-stitch-serif font-bold text-xl mb-4 text-stitch-foreground">Nội dung khóa học</h2>
              <div class="text-sm text-stitch-muted-foreground mb-4">
                {{ course.sections.length }} chương • {{ course.totalLessons || 0 }} bài học
              </div>
              
              <div class="space-y-3">
                <div v-for="(section, i) in course.sections" :key="section.id" class="bg-white rounded-xl border border-stitch-border overflow-hidden shadow-sm">
                  <button
                    class="w-full flex items-center justify-between p-4 text-left hover:bg-stitch-muted/30 transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring"
                    @click="toggleSection(i)"
                  >
                    <div class="flex items-center gap-3">
                      <span class="text-lg text-stitch-muted-foreground transition-transform duration-200" :class="{ 'rotate-90': openSection === i }">▶</span>
                      <span class="font-semibold text-sm text-stitch-card-foreground">{{ section.title }}</span>
                    </div>
                    <span class="text-xs text-stitch-muted-foreground">{{ section.lessons?.length || 0 }} bài</span>
                  </button>
                  
                  <div v-if="openSection === i" class="border-t border-stitch-border bg-stitch-background/50">
                    <template v-if="section.lessons && section.lessons.length > 0">
                      <div 
                        v-for="lesson in section.lessons" 
                        :key="lesson.id" 
                        class="flex items-center gap-3 px-4 py-3 hover:bg-stitch-muted/50 transition-colors"
                        :class="{ 'cursor-pointer': isEnrolled || lesson.isPreview }"
                        @click="handleLessonClick(lesson)"
                      >
                        <span class="text-base text-stitch-muted-foreground">▶</span>
                        <span :class="[
                          'text-sm flex-1 truncate transition-colors',
                          (isEnrolled || lesson.isPreview) ? 'text-stitch-primary hover:underline' : 'text-stitch-foreground'
                        ]">
                          {{ lesson.title }}
                        </span>
                        
                        <span v-if="lesson.isPreview" class="text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded-full whitespace-nowrap font-medium">Học thử</span>
                        <span v-if="!(isEnrolled || lesson.isPreview)" class="text-stitch-muted-foreground text-sm" title="Khóa bài học">🔒</span>
                      </div>
                    </template>
                    <div v-else class="px-4 py-3 text-sm text-stitch-muted-foreground italic">
                      Chưa có bài học trong chương này.
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="bg-white rounded-2xl border border-stitch-border p-6 shadow-sm text-center">
              <p class="text-stitch-muted-foreground text-sm">Nội dung khóa học đang được cập nhật.</p>
            </div>
          </div>

          <!-- Mobile enrollment -->
          <div class="lg:hidden bg-white rounded-2xl border border-stitch-border p-6 shadow-xl sticky bottom-4 z-10">
            <div class="mb-4">
              <template v-if="course.courseType === 'FREE'">
                <div class="text-3xl font-stitch-serif font-bold text-green-600">Miễn phí</div>
              </template>
              <template v-else>
                <div class="text-3xl font-stitch-serif font-bold text-stitch-foreground">
                  {{ formatPrice(course.salePrice > 0 ? course.salePrice : course.originalPrice) }}
                </div>
              </template>
            </div>
            
            <template v-if="isEnrolled">
              <button
                class="w-full py-3.5 rounded-xl font-semibold text-sm transition-all bg-green-500 text-white hover:bg-green-600 active:scale-95"
                @click="handleContinueLearning"
              >
                ✓ Đã đăng ký — Học ngay
              </button>
            </template>
            <template v-else>
              <button 
                v-if="!authStore.isAuthenticated"
                class="w-full py-3.5 rounded-xl font-semibold text-sm transition-all bg-stitch-primary text-white hover:bg-stitch-primary/90 active:scale-95"
                @click="handleEnroll"
              >
                Đăng nhập để học
              </button>
              <button 
                v-else-if="course.courseType === 'FREE'"
                class="w-full py-3.5 rounded-xl font-semibold text-sm transition-all active:scale-95"
                :class="isEnrolling ? 'bg-stitch-muted text-stitch-muted-foreground' : 'bg-stitch-primary text-white hover:bg-stitch-primary/90'"
                @click="handleEnroll"
                :disabled="isEnrolling || !course.id"
              >
                {{ isEnrolling ? 'Đang xử lý...' : 'Đăng ký miễn phí' }}
              </button>
              <button 
                v-else 
                class="w-full py-3.5 rounded-xl font-semibold text-sm transition-all bg-stitch-muted text-stitch-muted-foreground"
                disabled
              >
                Mua khóa học
              </button>
            </template>
            <p v-if="enrollErrorMsg" class="text-xs text-red-500 text-center mt-3">{{ enrollErrorMsg }}</p>
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
import { StudentService } from '@/services/student.service'
import { getApiErrorMessage } from '@/utils/api-error'
import { useAuthStore } from '@/stores/auth.store'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const course = ref(null)
const isLoading = ref(true)
const errorMsg = ref('')

const openSection = ref(0) // Default first section open

const isEnrolling = ref(false)
const isEnrolled = ref(false)
const enrolledCourseData = ref(null) // holds lastLessonId etc.
const enrollErrorMsg = ref('')
const enrollSuccessMsg = ref('')

const toggleSection = (idx) => {
  openSection.value = openSection.value === idx ? null : idx
}

const fetchCourseDetail = async () => {
  const slug = route.params.slug
  if (!slug) return

  isLoading.value = true
  errorMsg.value = ''
  
  try {
    const res = await CourseService.getCourseBySlug(slug)
    if (res.data && res.data.code === 1000) {
      course.value = res.data.result
      // After loading course, check enrollment
      await checkEnrollmentStatus()
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

/**
 * Check if the current user is already enrolled in this course.
 * Uses StudentService.getMyCourses() and matches by course ID.
 */
const checkEnrollmentStatus = async () => {
  if (!authStore.isAuthenticated || !course.value) return
  
  const userRoles = authStore.user?.roles || []
  if (!userRoles.includes('STUDENT')) return

  try {
    const res = await StudentService.getMyCourses()
    if (res.data && res.data.code === 1000) {
      const myCourses = res.data.result || []
      const found = myCourses.find(c => c.courseId === course.value.id)
      if (found) {
        isEnrolled.value = true
        enrolledCourseData.value = found
      }
    }
  } catch (error) {
    // Silently fail — don't block the page for enrollment check
    console.error('Enrollment check failed:', error)
  }
}

/**
 * Get the ID of the first lesson in the course curriculum.
 */
const getFirstLessonId = () => {
  if (!course.value?.sections) return null
  for (const section of course.value.sections) {
    if (section.lessons && section.lessons.length > 0) {
      return section.lessons[0].id
    }
  }
  return null
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
      enrollSuccessMsg.value = 'Ghi danh thành công! Đang chuyển tới bài học...'
      
      // Navigate to first lesson or my-courses
      const firstLessonId = getFirstLessonId()
      setTimeout(() => {
        if (firstLessonId) {
          router.push(`/student/lessons/${firstLessonId}`)
        } else {
          router.push('/student/my-courses')
        }
      }, 1000)
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

/**
 * Navigate to continue learning — last lesson if available, otherwise first lesson.
 */
const handleContinueLearning = () => {
  const lastLessonId = enrolledCourseData.value?.lastLessonId
  if (lastLessonId) {
    router.push(`/student/lessons/${lastLessonId}`)
    return
  }
  const firstLessonId = getFirstLessonId()
  if (firstLessonId) {
    router.push(`/student/lessons/${firstLessonId}`)
  } else {
    router.push('/student/my-courses')
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
