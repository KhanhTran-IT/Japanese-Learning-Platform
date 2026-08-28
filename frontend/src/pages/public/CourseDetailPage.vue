<template>
  <div class="bg-background min-h-screen font-body-md text-on-surface">
    <!-- Breadcrumb & Back Link -->
    <div class="max-w-[1280px] mx-auto px-margin-mobile md:px-margin-desktop py-6">
      <router-link to="/courses" class="inline-flex items-center gap-2 text-secondary hover:text-primary transition-colors font-button text-sm">
        <span class="material-symbols-outlined text-[20px]">arrow_back</span>
        Quay lại danh sách khóa học
      </router-link>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="max-w-[1280px] mx-auto px-margin-mobile md:px-margin-desktop py-20 flex flex-col items-center justify-center text-secondary">
      <span class="material-symbols-outlined animate-spin text-4xl mb-4">autorenew</span>
      <p class="font-body-md">Đang tải thông tin khóa học...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="errorMsg" class="max-w-[1280px] mx-auto px-margin-mobile md:px-margin-desktop py-20 flex flex-col items-center justify-center text-error">
      <span class="material-symbols-outlined text-5xl mb-4">error</span>
      <h2 class="font-headline-md text-2xl mb-2">Oops! Đã xảy ra lỗi</h2>
      <p class="font-body-md mb-6">{{ errorMsg }}</p>
      <div class="flex gap-4">
        <button class="bg-primary text-on-primary px-6 py-3 rounded-xl font-button hover:opacity-90 transition-all shadow-md" @click="fetchCourseDetail">Thử lại</button>
        <router-link to="/courses" class="bg-surface-container-lowest border border-paper-shadow text-on-surface px-6 py-3 rounded-xl font-button hover:bg-surface-container-low transition-all">Về danh sách</router-link>
      </div>
    </div>

    <!-- Main Content -->
    <template v-else-if="course">
      <div class="max-w-[1280px] mx-auto px-margin-mobile md:px-margin-desktop pb-24">
        <div class="flex flex-col lg:flex-row gap-8 items-start">
          
          <!-- Left Column: Details -->
          <div class="w-full lg:flex-1 space-y-8">
            <!-- Course Header -->
            <div class="zen-card p-8 md:p-10 rounded-[24px]">
              <div class="flex items-center gap-3 mb-6">
                <span v-if="course.level" class="px-3 py-1 rounded-full bg-primary-container text-on-primary-container font-label-sm text-[12px] tracking-wider uppercase">JLPT {{ course.level }}</span>
                <span v-if="course.courseType === 'FREE'" class="px-3 py-1 rounded-full bg-success-green/20 text-success-green font-label-sm text-[12px] tracking-wider uppercase">Miễn phí</span>
              </div>
              <h1 class="font-headline-lg text-[32px] md:text-[40px] text-ink-black mb-4 leading-tight">
                {{ course.title }}
              </h1>
              <p class="font-body-lg text-secondary mb-8 leading-relaxed">
                {{ course.shortDescription }}
              </p>
              
              <div class="flex flex-wrap items-center gap-6 py-6 border-t border-b border-paper-shadow mb-8">
                <div class="flex items-center gap-2">
                  <span class="material-symbols-outlined text-primary">menu_book</span>
                  <div>
                    <p class="font-label-sm text-secondary text-[11px] uppercase">Bài học</p>
                    <p class="font-button text-ink-black">{{ course.totalLessons || 0 }} bài</p>
                  </div>
                </div>
                <div class="w-px h-10 bg-paper-shadow hidden sm:block"></div>
                <div class="flex items-center gap-2">
                  <span class="material-symbols-outlined text-primary">schedule</span>
                  <div>
                    <p class="font-label-sm text-secondary text-[11px] uppercase">Thời lượng</p>
                    <p class="font-button text-ink-black">{{ formatDuration(course.totalDurationMinutes) }}</p>
                  </div>
                </div>
                <div class="w-px h-10 bg-paper-shadow hidden sm:block"></div>
                <div class="flex items-center gap-2">
                  <span class="material-symbols-outlined text-primary">group</span>
                  <div>
                    <p class="font-label-sm text-secondary text-[11px] uppercase">Học viên</p>
                    <p class="font-button text-ink-black">{{ course.totalStudents || 0 }}</p>
                  </div>
                </div>
              </div>

              <!-- Teacher Info -->
              <div class="flex items-center gap-4" v-if="course.teacherName">
                <img 
                  v-if="course.teacherAvatarUrl" 
                  :src="course.teacherAvatarUrl" 
                  alt="Teacher" 
                  class="w-14 h-14 rounded-full object-cover border-2 border-surface-container"
                  @error="onImgError"
                >
                <div v-else class="w-14 h-14 rounded-full bg-secondary-container text-on-secondary-container flex items-center justify-center font-bold text-xl">
                  {{ course.teacherName.charAt(0).toUpperCase() }}
                </div>
                <div>
                  <p class="font-label-sm text-secondary mb-1">Sensei (Giảng viên)</p>
                  <p class="font-button text-ink-black">{{ course.teacherName }}</p>
                </div>
              </div>
            </div>

            <!-- Description Section -->
            <div class="zen-card p-8 md:p-10 rounded-[24px]">
              <h2 class="font-headline-md text-ink-black mb-6 flex items-center gap-3">
                <span class="material-symbols-outlined text-primary">info</span>
                Triết lý khóa học
              </h2>
              <div class="prose max-w-none font-body-md text-on-surface-variant leading-relaxed whitespace-pre-line">
                {{ formattedDescription }}
              </div>
            </div>

            <!-- Curriculum Section -->
            <div class="zen-card p-8 md:p-10 rounded-[24px]" v-if="course.sections && course.sections.length > 0">
              <h2 class="font-headline-md text-ink-black mb-8 flex items-center gap-3">
                <span class="material-symbols-outlined text-primary">view_list</span>
                Chương trình học
              </h2>
              
              <div class="space-y-6">
                <div v-for="(section, idx) in course.sections" :key="section.id" class="border border-paper-shadow rounded-2xl overflow-hidden bg-surface-container-lowest">
                  <!-- Section Header -->
                  <div class="bg-surface-container-low px-6 py-4 flex items-center justify-between cursor-pointer border-b border-paper-shadow">
                    <div class="flex items-center gap-4">
                      <div class="w-8 h-8 rounded-full bg-surface-container-highest flex items-center justify-center font-label-sm text-secondary font-bold">
                        {{ idx + 1 }}
                      </div>
                      <h3 class="font-button text-ink-black">{{ section.title }}</h3>
                    </div>
                    <span class="font-label-sm text-secondary">{{ section.lessons?.length || 0 }} bài học</span>
                  </div>
                  
                  <!-- Lessons List -->
                  <div class="divide-y divide-paper-shadow" v-if="section.lessons && section.lessons.length > 0">
                    <div 
                      v-for="lesson in section.lessons" 
                      :key="lesson.id" 
                      class="px-6 py-4 flex items-center justify-between group transition-colors"
                      :class="{ 'hover:bg-surface cursor-pointer': lesson.isPreview || isEnrolled }"
                      @click="handleLessonClick(lesson)"
                    >
                      <div class="flex items-center gap-4">
                        <span class="material-symbols-outlined text-outline-variant group-hover:text-primary transition-colors">play_circle</span>
                        <span class="font-body-md text-on-surface-variant group-hover:text-ink-black transition-colors">{{ lesson.title }}</span>
                      </div>
                      <div class="flex items-center gap-3">
                        <span v-if="lesson.isPreview" class="px-2 py-1 bg-tertiary-fixed text-on-tertiary-fixed-variant rounded text-[10px] font-label-sm uppercase tracking-wider">Học thử</span>
                        <span class="material-symbols-outlined text-outline-variant" v-if="!(lesson.isPreview || isEnrolled)">lock</span>
                      </div>
                    </div>
                  </div>
                  <div v-else class="px-6 py-4 text-center text-secondary font-body-md italic">
                    Chưa có bài học nào trong chương này.
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Right Column: Sticky Enrollment Card -->
          <div class="w-full lg:w-[380px] lg:sticky lg:top-24">
            <div class="zen-card rounded-[24px] overflow-hidden">
              <div class="aspect-video relative bg-surface-container-high">
                <img 
                  v-if="course.thumbnailUrl" 
                  :src="course.thumbnailUrl" 
                  :alt="course.title"
                  class="w-full h-full object-cover"
                  @error="onImgError"
                >
                <div v-else class="w-full h-full flex items-center justify-center bg-secondary-container">
                  <span class="text-5xl text-on-secondary-container font-bold">{{ course.level || 'JP' }}</span>
                </div>
                <div class="absolute inset-0 bg-ink-black/20 flex items-center justify-center opacity-0 hover:opacity-100 transition-opacity cursor-pointer">
                  <div class="w-16 h-16 rounded-full bg-white/90 flex items-center justify-center">
                    <span class="material-symbols-outlined text-primary text-3xl ml-1">play_arrow</span>
                  </div>
                </div>
              </div>
              
              <div class="p-8">
                <div class="mb-6">
                  <template v-if="course.courseType === 'FREE'">
                    <span class="font-headline-lg text-[32px] text-success-green">Miễn phí</span>
                  </template>
                  <template v-else>
                    <div class="font-headline-lg text-[32px] text-ink-black">
                      {{ formatPrice(course.salePrice > 0 ? course.salePrice : course.originalPrice) }}
                    </div>
                    <div v-if="course.salePrice > 0 && course.salePrice < course.originalPrice" class="text-secondary line-through mt-1">
                      {{ formatPrice(course.originalPrice) }}
                    </div>
                  </template>
                </div>
                
                <!-- Already enrolled: Continue Learning -->
                <template v-if="isEnrolled">
                  <div class="bg-success-green/10 text-success-green font-label-sm text-center py-2 rounded-lg mb-4 flex items-center justify-center gap-2">
                    <span class="material-symbols-outlined text-[18px]">check_circle</span>
                    Đã ghi danh khóa học này
                  </div>
                  <button
                    class="w-full py-4 rounded-xl font-button text-lg mb-4 bg-primary hover:bg-primary-container text-white hover:text-on-primary-container shadow-lg hover:shadow-xl hover:-translate-y-1 active:scale-95 transition-all flex items-center justify-center gap-2"
                    @click="handleContinueLearning"
                  >
                    <span class="material-symbols-outlined text-[24px]">play_circle</span>
                    Tiếp tục học
                  </button>
                </template>
                
                <!-- Not enrolled: Enroll or Login -->
                <template v-else>
                  <!-- Guest user -->
                  <button 
                    v-if="!authStore.isAuthenticated"
                    class="w-full py-4 rounded-xl font-button text-lg mb-4 bg-primary hover:bg-primary-container text-white hover:text-on-primary-container shadow-lg hover:shadow-xl hover:-translate-y-1 active:scale-95 transition-all"
                    @click="handleEnroll"
                  >
                    Đăng nhập để học
                  </button>
                  <!-- Authenticated: Free course -->
                  <button 
                    v-else-if="course.courseType === 'FREE'"
                    class="w-full py-4 rounded-xl font-button text-lg mb-4 transition-all"
                    :class="isEnrolling ? 'bg-surface-container-high text-secondary cursor-not-allowed' : 'bg-primary hover:bg-primary-container text-white hover:text-on-primary-container shadow-lg hover:shadow-xl hover:-translate-y-1 active:scale-95'"
                    @click="handleEnroll"
                    :disabled="isEnrolling || !course.id"
                  >
                    {{ isEnrolling ? 'Đang xử lý...' : 'Bắt đầu học ngay' }}
                  </button>
                  <!-- Paid course placeholder -->
                  <button 
                    v-else 
                    class="w-full py-4 rounded-xl font-button text-lg mb-4 bg-surface-container-high text-secondary cursor-not-allowed"
                    disabled
                  >
                    Mua khóa học
                  </button>
                </template>
                
                <p v-if="enrollSuccessMsg" class="text-success-green font-label-sm text-center bg-success-green/10 py-2 rounded-lg">{{ enrollSuccessMsg }}</p>
                <p v-else-if="enrollErrorMsg" class="text-error font-label-sm text-center bg-error-container/50 py-2 rounded-lg">{{ enrollErrorMsg }}</p>
                
                <div class="mt-6 pt-6 border-t border-paper-shadow text-center">
                  <p class="font-label-sm text-secondary mb-2 uppercase tracking-wider">Khóa học bao gồm</p>
                  <ul class="space-y-3 text-sm text-on-surface-variant text-left inline-block">
                    <li class="flex items-center gap-2"><span class="material-symbols-outlined text-[18px] text-primary">ondemand_video</span> Video bài giảng chất lượng cao</li>
                    <li class="flex items-center gap-2"><span class="material-symbols-outlined text-[18px] text-primary">quiz</span> Bài tập tự luận & trắc nghiệm</li>
                    <li class="flex items-center gap-2"><span class="material-symbols-outlined text-[18px] text-primary">forum</span> Hỗ trợ giải đáp từ Sensei</li>
                    <li class="flex items-center gap-2"><span class="material-symbols-outlined text-[18px] text-primary">all_inclusive</span> Quyền truy cập trọn đời</li>
                  </ul>
                </div>
              </div>
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
import { StudentService } from '@/services/student.service'
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
const enrolledCourseData = ref(null) // holds lastLessonId etc.
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
