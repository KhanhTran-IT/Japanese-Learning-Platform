<template>
  <div class="flex flex-col lg:flex-row min-h-screen bg-background font-body-md text-on-surface">
    <!-- Main Content Area -->
    <div class="flex-1 flex flex-col h-screen overflow-y-auto">
      <!-- Header/Navigation -->
      <header class="h-16 bg-surface-container-lowest border-b border-paper-shadow flex items-center justify-between px-6 sticky top-0 z-10 shrink-0">
        <router-link to="/student/my-courses" class="inline-flex items-center gap-2 text-secondary hover:text-primary transition-colors font-button text-sm">
          <span class="material-symbols-outlined text-[20px]">arrow_back</span>
          Quay lại Khóa học
        </router-link>
        <div v-if="lesson" class="flex items-center gap-3">
          <span class="px-3 py-1 rounded-full bg-surface-container-high text-secondary font-label-sm text-[12px] tracking-wider uppercase flex items-center gap-1">
            <span class="material-symbols-outlined text-[16px]">schedule</span>
            {{ lesson.durationMinutes }} phút
          </span>
        </div>
      </header>

      <!-- Loading State -->
      <div v-if="isLoading" class="flex-1 flex flex-col items-center justify-center py-20 text-secondary">
        <span class="material-symbols-outlined animate-spin text-4xl mb-4">autorenew</span>
        <p class="font-body-md">Đang tải bài học...</p>
      </div>

      <!-- Error/Forbidden State -->
      <div v-else-if="errorMsg" class="error-state flex-1 flex flex-col items-center justify-center py-20 text-center max-w-lg mx-auto">
        <span class="material-symbols-outlined text-5xl mb-4 text-error">lock</span>
        <h2 class="font-headline-md text-2xl text-ink-black mb-2">Không thể truy cập</h2>
        <p class="font-body-md text-secondary mb-6">{{ errorMsg }}</p>
        <router-link to="/student/my-courses" class="bg-primary text-on-primary px-6 py-3 rounded-xl font-button hover:opacity-90 transition-all shadow-md">
          Về danh sách khóa học
        </router-link>
      </div>

      <!-- Main Content -->
      <main v-else-if="lesson" class="flex-1 max-w-4xl mx-auto w-full px-4 md:px-8 py-8 md:py-12 pb-24 space-y-8">
        
        <!-- Title & Nav -->
        <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-2">
          <h1 class="lesson-title font-headline-lg text-2xl md:text-3xl text-ink-black leading-tight flex-1">
            {{ lesson.title }}
          </h1>
          <div class="flex items-center gap-2 shrink-0">
            <button 
              class="px-4 py-2 rounded-lg border border-outline-variant bg-surface-container-lowest font-button text-sm text-secondary hover:bg-surface-container-low hover:text-ink-black transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1"
              :disabled="!curriculum || !curriculum.previousLessonId"
              @click="goToLesson(curriculum.previousLessonId)"
            >
              <span class="material-symbols-outlined text-[18px]">chevron_left</span>
              Bài trước
            </button>
            <button 
              class="px-4 py-2 rounded-lg border border-outline-variant bg-surface-container-lowest font-button text-sm text-secondary hover:bg-surface-container-low hover:text-ink-black transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1"
              :disabled="!curriculum || !curriculum.nextLessonId"
              @click="goToLesson(curriculum.nextLessonId)"
            >
              Tiếp theo
              <span class="material-symbols-outlined text-[18px]">chevron_right</span>
            </button>
          </div>
        </div>
        
        <!-- Video Section -->
        <div v-if="lesson.videoUrl" class="zen-card rounded-[24px] overflow-hidden aspect-video bg-black flex items-center justify-center">
          <video :src="lesson.videoUrl" controls class="w-full h-full object-contain">
            Trình duyệt của bạn không hỗ trợ video.
          </video>
        </div>

        <!-- Text Content -->
        <div v-if="lesson.content" class="zen-card p-8 md:p-10 rounded-[24px]">
          <p class="safe-content whitespace-pre-wrap text-on-surface-variant leading-relaxed font-body-md text-[17px]">
            {{ lesson.content }}
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
          <!-- Progress Tracking Panel -->
          <aside class="zen-card p-6 md:p-8 rounded-[24px]">
            <h3 class="font-headline-md text-xl text-ink-black mb-6 flex items-center gap-2">
              <span class="material-symbols-outlined text-primary">monitoring</span>
              Tiến độ của bạn
            </h3>
            
            <div class="flex items-center gap-4 mb-8">
              <div class="flex-1 h-2.5 bg-surface-container-high rounded-full overflow-hidden">
                <div class="h-full bg-gradient-to-r from-primary to-primary-container transition-all duration-500 ease-out rounded-full" :style="{ width: progressForm.watchedPercent + '%' }"></div>
              </div>
              <span class="progress-text font-headline-md text-lg text-primary min-w-[3rem] text-right">{{ Math.round(progressForm.watchedPercent) }}%</span>
            </div>

            <div class="space-y-6">
              <div>
                <label class="block font-label-sm text-secondary uppercase tracking-wider mb-3">Cập nhật phần trăm đã học:</label>
                <input 
                  type="range" 
                  min="0" 
                  max="100" 
                  v-model.number="progressForm.watchedPercent" 
                  class="w-full h-2 bg-surface-container-high rounded-lg appearance-none cursor-pointer accent-primary"
                  :disabled="isSaving"
                />
              </div>

              <div class="flex flex-col sm:flex-row gap-3">
                <button 
                  @click="saveProgress" 
                  class="flex-1 px-6 py-3 rounded-xl font-button transition-all bg-surface-container-high text-secondary hover:bg-surface-container-highest hover:text-ink-black border border-transparent disabled:opacity-50 disabled:cursor-not-allowed"
                  :disabled="isSaving"
                >
                  {{ isSaving ? 'Đang lưu...' : 'Lưu tiến độ' }}
                </button>
                <button 
                  @click="markCompleted" 
                  class="btn-complete flex-1 px-6 py-3 rounded-xl font-button transition-all flex items-center justify-center gap-2"
                  :class="lesson.isCompleted ? 'bg-success-green text-white cursor-default shadow-md' : 'bg-primary text-white hover:bg-primary-container shadow-md hover:shadow-lg active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed'"
                  :disabled="isSaving || lesson.isCompleted"
                >
                  <span v-if="lesson.isCompleted" class="material-symbols-outlined text-[20px]">check_circle</span>
                  {{ lesson.isCompleted ? 'Đã hoàn thành' : 'Đánh dấu xong' }}
                </button>
              </div>
            </div>
            
            <div v-if="saveMessage" class="mt-4 p-3 rounded-xl font-body-md text-center text-sm flex items-center justify-center gap-2" :class="saveStatus === 'success' ? 'bg-success-green/10 text-success-green border border-success-green/20' : 'bg-error-container/50 text-error border border-error/20'">
              <span class="material-symbols-outlined text-[18px]">{{ saveStatus === 'success' ? 'check_circle' : 'error' }}</span>
              {{ saveMessage }}
            </div>
          </aside>

          <!-- Resources Panel -->
          <aside class="zen-card p-6 md:p-8 rounded-[24px]">
            <h3 class="font-headline-md text-xl text-ink-black mb-6 flex items-center gap-2">
              <span class="material-symbols-outlined text-primary">attach_file</span>
              Tài liệu đính kèm
            </h3>
            
            <div v-if="isLoadingResources" class="text-secondary font-body-md italic flex items-center gap-2">
              <span class="material-symbols-outlined animate-spin text-[18px]">autorenew</span> Đang tải...
            </div>
            <div v-else-if="resourceError" class="bg-error-container/50 text-error p-3 rounded-xl text-sm border border-error/20 flex items-start gap-2">
              <span class="material-symbols-outlined text-[18px] shrink-0 mt-0.5">error</span>
              {{ resourceError }}
            </div>
            <div v-else-if="resources.length === 0" class="text-secondary font-body-md italic bg-surface-container-lowest p-6 rounded-xl border border-dashed border-outline-variant text-center">
              Chưa có tài liệu đính kèm.
            </div>
            <ul v-else class="space-y-3">
              <li v-for="res in resources" :key="res.id" class="flex items-center justify-between p-4 rounded-xl border border-paper-shadow bg-surface-container-lowest hover:border-outline-variant transition-colors group">
                <div class="flex items-center gap-3 overflow-hidden">
                  <span class="px-2 py-1 rounded bg-secondary-container text-on-secondary-container font-label-sm text-[10px] uppercase tracking-wider shrink-0">
                    {{ res.resourceType }}
                  </span>
                  <a :href="res.fileUrl" target="_blank" rel="noopener noreferrer" class="font-button text-sm text-ink-black group-hover:text-primary truncate transition-colors">
                    {{ res.title }}
                  </a>
                </div>
                <span v-if="res.fileSize" class="text-xs text-secondary shrink-0 font-mono ml-4">
                  {{ formatFileSize(res.fileSize) }}
                </span>
              </li>
            </ul>
          </aside>
        </div>

        <!-- Quizzes Panel -->
        <div v-if="quizzes && quizzes.length > 0" class="zen-card p-6 md:p-8 rounded-[24px]">
          <h3 class="font-headline-md text-xl text-ink-black mb-6 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">quiz</span>
            Bài tập (Quiz)
          </h3>
          <div class="space-y-4">
            <div v-for="quiz in quizzes" :key="quiz.id" class="p-5 rounded-xl border border-paper-shadow bg-surface-container-lowest flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 group hover:border-primary/30 transition-colors">
              <div class="flex-1">
                <h4 class="font-button text-ink-black text-lg mb-1">{{ quiz.title }}</h4>
                <div class="flex flex-wrap items-center gap-3 text-sm text-secondary font-body-md">
                  <span v-if="quiz.questionCount" class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-[16px]">format_list_numbered</span>
                    {{ quiz.questionCount }} câu
                  </span>
                  <span v-if="quiz.timeLimitMinutes" class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-[16px]">timer</span>
                    {{ quiz.timeLimitMinutes }} phút
                  </span>
                  <span v-if="quiz.maxAttempts" class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-[16px]">replay</span>
                    {{ quiz.remainingAttempts !== null ? `Còn ${quiz.remainingAttempts}/${quiz.maxAttempts} lượt` : `${quiz.maxAttempts} lượt` }}
                  </span>
                </div>
                <div v-if="quiz.latestAttemptId" class="mt-2 text-sm flex items-center gap-2" :class="quiz.latestPassed ? 'text-success-green' : 'text-error'">
                  <span class="material-symbols-outlined text-[16px]">{{ quiz.latestPassed ? 'check_circle' : 'cancel' }}</span>
                  Lần gần nhất: {{ quiz.latestScore }}/{{ quiz.passingScore }} điểm ({{ quiz.latestPassed ? 'Đạt' : 'Chưa đạt' }})
                </div>
              </div>
              <div class="shrink-0 flex gap-2">
                <router-link
                  v-if="quiz.latestAttemptId"
                  :to="`/student/quizzes/${quiz.id}/result/${quiz.latestAttemptId}`"
                  class="px-4 py-2 rounded-lg border border-outline-variant text-secondary font-button text-sm hover:bg-surface-container-low transition-colors"
                >
                  Xem kết quả
                </router-link>
                <router-link
                  v-if="quiz.remainingAttempts === null || quiz.remainingAttempts > 0"
                  :to="`/student/quizzes/${quiz.id}`"
                  class="px-4 py-2 rounded-lg bg-primary text-on-primary font-button text-sm hover:opacity-90 shadow-sm hover:shadow transition-all"
                >
                  {{ quiz.latestAttemptId ? 'Làm lại' : 'Làm bài' }}
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- Curriculum Sidebar -->
    <aside class="w-full lg:w-[350px] shrink-0 border-t lg:border-t-0 lg:border-l border-paper-shadow bg-surface-container-lowest lg:h-screen lg:sticky lg:top-0 overflow-y-auto">
      <LearningCurriculumSidebar
        :curriculum="curriculum"
        :currentLessonId="lesson?.id"
        :isLoading="isLoadingCurriculum"
        :errorMsg="curriculumError"
      />
    </aside>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LearningService } from '@/services/learning.service'
import { QuizService } from '@/services/quiz.service'
import { getApiErrorMessage } from '@/utils/api-error'
import LearningCurriculumSidebar from '@/components/lesson/LearningCurriculumSidebar.vue'

const route = useRoute()
const router = useRouter()

// States
const isLoading = ref(true)
const isSaving = ref(false)
const errorMsg = ref('')
const saveMessage = ref('')
const saveStatus = ref('') // 'success' or 'error'

const lesson = ref(null)
const resources = ref([])
const isLoadingResources = ref(false)
const resourceError = ref('')

const quizzes = ref([])
const isLoadingQuizzes = ref(false)
const quizzesError = ref('')

const curriculum = ref(null)
const isLoadingCurriculum = ref(false)
const curriculumError = ref('')

const progressForm = reactive({
  watchedPercent: 0,
  isCompleted: false
})

const fetchLesson = async () => {
  const lessonId = parseInt(route.params.id)
  
  if (isNaN(lessonId)) {
    errorMsg.value = 'Đường dẫn bài học không hợp lệ.'
    isLoading.value = false
    return
  }

  isLoading.value = true
  errorMsg.value = ''

  try {
    const res = await LearningService.getLessonDetail(lessonId)
    if (res.data && res.data.code === 1000) {
      lesson.value = res.data.result
      progressForm.watchedPercent = lesson.value.watchedPercent || 0
      progressForm.isCompleted = lesson.value.isCompleted || false
    } else {
      throw new Error(res.data?.message || 'Lỗi lấy dữ liệu')
    }
  } catch (error) {
    console.error('Learning error:', error)
    // Đặc tả xử lý riêng cho 403 (FORBIDDEN_ACCESS)
    if (error.response && error.response.status === 403) {
      errorMsg.value = 'Bạn chưa ghi danh khóa học này nên không thể xem bài học (không phải bài học thử).'
    } else {
      errorMsg.value = getApiErrorMessage(error)
    }
  } finally {
    isLoading.value = false
  }
}

const fetchResources = async (lessonId) => {
  isLoadingResources.value = true
  resourceError.value = ''
  try {
    const res = await LearningService.getLessonResources(lessonId)
    if (res.data && res.data.code === 1000) {
      resources.value = res.data.result || []
    }
  } catch (error) {
    // Silently handle — don't break lesson content
    resourceError.value = 'Không thể tải tài liệu đính kèm.'
    console.error('Resource fetch error:', error)
  } finally {
    isLoadingResources.value = false
  }
}

const fetchCurriculum = async (lessonId) => {
  isLoadingCurriculum.value = true
  curriculumError.value = ''
  try {
    const res = await LearningService.getLessonCurriculum(lessonId)
    if (res.data && res.data.code === 1000) {
      curriculum.value = res.data.result
    }
  } catch (error) {
    curriculumError.value = 'Không thể tải chương trình học.'
    console.error('Curriculum fetch error:', error)
  } finally {
    isLoadingCurriculum.value = false
  }
}

const fetchQuizzes = async (lessonId) => {
  isLoadingQuizzes.value = true
  quizzesError.value = ''
  try {
    const res = await QuizService.getLessonQuizzes(lessonId)
    if (res.data && res.data.code === 1000) {
      quizzes.value = res.data.result || []
    }
  } catch (error) {
    quizzesError.value = 'Không thể tải danh sách bài tập.'
    console.error('Quizzes fetch error:', error)
  } finally {
    isLoadingQuizzes.value = false
  }
}

const submitProgress = async (payload) => {
  if (!lesson.value) return
  
  isSaving.value = true
  saveMessage.value = ''
  
  try {
    const res = await LearningService.updateProgress(lesson.value.id, payload)
    if (res.data && res.data.code === 1000) {
      saveStatus.value = 'success'
      saveMessage.value = 'Đã lưu tiến độ thành công!'
      
      // Update local state to reflect successful save
      const currentPercent = lesson.value.watchedPercent || 0
      lesson.value.watchedPercent = Math.max(currentPercent, payload.watchedPercent)
      lesson.value.isCompleted = payload.isCompleted || lesson.value.isCompleted
      
      progressForm.watchedPercent = lesson.value.watchedPercent
      progressForm.isCompleted = lesson.value.isCompleted
      
      // Ẩn thông báo sau 3s
      setTimeout(() => { saveMessage.value = '' }, 3000)
    }
  } catch (error) {
    saveStatus.value = 'error'
    saveMessage.value = getApiErrorMessage(error)
  } finally {
    isSaving.value = false
  }
}

const saveProgress = () => {
  // Validate input client-side
  let percent = progressForm.watchedPercent
  if (percent < 0) percent = 0
  if (percent > 100) percent = 100
  progressForm.watchedPercent = percent
  
  submitProgress({
    watchedPercent: percent,
    isCompleted: progressForm.isCompleted
  })
}

const markCompleted = async () => {
  if (!lesson.value) return

  isSaving.value = true
  saveMessage.value = ''

  try {
    const res = await LearningService.completeLesson(lesson.value.id)
    if (res.data && res.data.code === 1000) {
      saveStatus.value = 'success'
      saveMessage.value = 'Đã hoàn thành bài học!'

      // Update local state
      lesson.value.watchedPercent = 100
      lesson.value.isCompleted = true
      progressForm.watchedPercent = 100
      progressForm.isCompleted = true

      setTimeout(() => { saveMessage.value = '' }, 3000)
    }
  } catch (error) {
    saveStatus.value = 'error'
    saveMessage.value = getApiErrorMessage(error)
  } finally {
    isSaving.value = false
  }
}

const goToLesson = (lessonId) => {
  if (lessonId) {
    router.push(`/student/lessons/${lessonId}`)
  }
}

onMounted(() => {
  fetchLesson()
})

watch(() => route.params.id, () => {
  fetchLesson()
})

// Watch lesson loaded to fetch resources and curriculum
watch(lesson, (newLesson) => {
  if (newLesson && newLesson.id) {
    fetchResources(newLesson.id)
    fetchCurriculum(newLesson.id)
    fetchQuizzes(newLesson.id)
  }
})

const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(i > 0 ? 1 : 0) + ' ' + units[i]
}
</script>
