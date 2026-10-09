<template>
  <div class="h-screen flex flex-col lg:flex-row bg-[#0f1117] text-white overflow-hidden font-stitch-sans">
    
    <!-- Main Content Area -->
    <div class="flex-1 flex flex-col min-w-0">
      <!-- Top bar -->
      <header class="flex items-center gap-4 px-4 py-3 bg-[#161b27] border-b border-white/5 flex-shrink-0">
        <router-link to="/student/my-courses" class="text-white/50 hover:text-white transition-colors text-sm flex items-center gap-1.5 shrink-0">
          ← Khóa học
        </router-link>
        <div class="flex-1 min-w-0">
          <h1 class="text-sm font-semibold truncate">{{ lesson?.title || 'Đang tải...' }}</h1>
          <div class="text-xs text-white/30 truncate">{{ curriculum?.courseTitle || '...' }}</div>
        </div>
        <div class="flex items-center gap-3 shrink-0">
          <div v-if="lesson" class="hidden sm:flex items-center gap-1.5 text-xs text-white/40 bg-white/5 px-2 py-1 rounded">
            <span class="material-symbols-outlined text-[14px]">schedule</span>
            {{ lesson.durationMinutes }} phút
          </div>
          <button @click="sidebarOpen = !sidebarOpen" class="lg:hidden text-white/40 hover:text-white text-lg p-1">
            ☰
          </button>
        </div>
      </header>

      <!-- Loading State -->
      <div v-if="isLoading" class="flex-1 flex flex-col items-center justify-center text-white/40">
        <span class="material-symbols-outlined animate-spin text-4xl mb-4">autorenew</span>
        <p class="text-sm">Đang tải bài học...</p>
      </div>

      <!-- Error/Forbidden State -->
      <div v-else-if="errorMsg" class="flex-1 flex flex-col items-center justify-center text-center max-w-lg mx-auto p-6">
        <span class="material-symbols-outlined text-5xl mb-4 text-red-400">lock</span>
        <h2 class="text-2xl font-bold mb-2">Không thể truy cập</h2>
        <p class="text-white/60 mb-6">{{ errorMsg }}</p>
        <router-link to="/student/my-courses" class="bg-stitch-primary text-white px-6 py-3 rounded-xl font-semibold hover:bg-stitch-primary/90 transition-all">
          Về danh sách khóa học
        </router-link>
      </div>

      <!-- Main Content Container -->
      <div v-else-if="lesson" class="flex-1 flex flex-col overflow-hidden">
        <!-- Video -->
        <div v-if="lesson.videoUrl" class="relative bg-black flex-shrink-0 flex items-center justify-center" style="aspect-ratio: 16/9; max-height: 60vh;">
          <video :src="lesson.videoUrl" controls class="w-full h-full object-contain">
            Trình duyệt của bạn không hỗ trợ video.
          </video>
        </div>

        <!-- Tabs below video/header -->
        <div class="flex-1 overflow-auto bg-[#0f1117] flex flex-col">
          <div class="border-b border-white/5 shrink-0 sticky top-0 bg-[#0f1117] z-10">
            <div class="flex gap-0 px-4 overflow-x-auto custom-scrollbar-horizontal">
              <button
                v-if="lesson.content"
                @click="activeTab = 'content'"
                class="px-5 py-3 text-sm font-medium border-b-2 transition-colors whitespace-nowrap"
                :class="activeTab === 'content' ? 'border-stitch-accent text-white' : 'border-transparent text-white/40 hover:text-white/70'"
              >
                📝 Nội dung
              </button>
              <button
                @click="activeTab = 'progress'"
                class="px-5 py-3 text-sm font-medium border-b-2 transition-colors whitespace-nowrap"
                :class="activeTab === 'progress' ? 'border-stitch-accent text-white' : 'border-transparent text-white/40 hover:text-white/70'"
              >
                📈 Tiến độ
              </button>
              <button
                @click="activeTab = 'quizzes'"
                class="px-5 py-3 text-sm font-medium border-b-2 transition-colors whitespace-nowrap"
                :class="activeTab === 'quizzes' ? 'border-stitch-accent text-white' : 'border-transparent text-white/40 hover:text-white/70'"
              >
                📝 Bài tập
                <span v-if="quizzes.length" class="ml-1 px-1.5 py-0.5 rounded-full bg-white/10 text-[10px]">{{ quizzes.length }}</span>
              </button>
              <button
                @click="activeTab = 'resources'"
                class="px-5 py-3 text-sm font-medium border-b-2 transition-colors whitespace-nowrap"
                :class="activeTab === 'resources' ? 'border-stitch-accent text-white' : 'border-transparent text-white/40 hover:text-white/70'"
              >
                📎 Tài liệu
                <span v-if="resources.length" class="ml-1 px-1.5 py-0.5 rounded-full bg-white/10 text-[10px]">{{ resources.length }}</span>
              </button>
            </div>
          </div>

          <div class="p-6 md:p-8 flex-1 max-w-4xl mx-auto w-full">
            <!-- Content Tab -->
            <div v-show="activeTab === 'content'" class="animate-fade-in">
              <div v-if="lesson.content" class="bg-white/5 rounded-2xl p-6 md:p-8 border border-white/5">
                <p class="whitespace-pre-wrap text-white/80 leading-relaxed text-sm md:text-base">
                  {{ lesson.content }}
                </p>
              </div>
            </div>

            <!-- Progress Tab -->
            <div v-show="activeTab === 'progress'" class="animate-fade-in">
              <div class="max-w-2xl">
                <div class="bg-white/5 rounded-2xl p-6 md:p-8 border border-white/5">
                  <h3 class="text-lg font-bold mb-6 flex items-center gap-2">
                    <span class="material-symbols-outlined text-stitch-primary">monitoring</span>
                    Đánh dấu tiến độ
                  </h3>
                  
                  <div class="flex items-center gap-4 mb-8">
                    <div class="flex-1 h-2 bg-white/10 rounded-full overflow-hidden">
                      <div class="h-full bg-gradient-to-r from-stitch-primary to-stitch-accent transition-all duration-500 rounded-full" :style="{ width: progressForm.watchedPercent + '%' }"></div>
                    </div>
                    <span class="text-stitch-primary font-bold min-w-[3rem] text-right">{{ Math.round(progressForm.watchedPercent) }}%</span>
                  </div>

                  <div class="space-y-6">
                    <div>
                      <label class="block text-xs text-white/40 uppercase tracking-wider mb-3">Kéo để cập nhật % (nếu không xem video):</label>
                      <input 
                        type="range" 
                        min="0" 
                        max="100" 
                        v-model.number="progressForm.watchedPercent" 
                        class="w-full h-1.5 bg-white/20 rounded-lg appearance-none cursor-pointer accent-stitch-primary"
                        :disabled="isSaving"
                      />
                    </div>

                    <div class="flex flex-col sm:flex-row gap-3">
                      <button 
                        @click="saveProgress" 
                        class="flex-1 px-4 py-2.5 rounded-xl text-sm font-semibold transition-all bg-white/10 text-white hover:bg-white/20 border border-transparent disabled:opacity-50 disabled:cursor-not-allowed"
                        :disabled="isSaving"
                      >
                        {{ isSaving ? 'Đang lưu...' : 'Lưu % tiến độ' }}
                      </button>
                      <button 
                        @click="markCompleted" 
                        class="flex-1 px-4 py-2.5 rounded-xl text-sm font-semibold transition-all flex items-center justify-center gap-2"
                        :class="lesson.isCompleted ? 'bg-green-500/20 text-green-400 border border-green-500/30 cursor-default' : 'bg-stitch-primary text-white hover:bg-stitch-primary/90 disabled:opacity-50 disabled:cursor-not-allowed'"
                        :disabled="isSaving || lesson.isCompleted"
                      >
                        <span v-if="lesson.isCompleted" class="material-symbols-outlined text-[18px]">check_circle</span>
                        {{ lesson.isCompleted ? '✓ Đã hoàn thành' : 'Đánh dấu xong bài học' }}
                      </button>
                    </div>
                  </div>
                  
                  <div v-if="saveMessage" class="mt-4 p-3 rounded-xl text-sm flex items-center justify-center gap-2 border" :class="saveStatus === 'success' ? 'bg-green-500/10 text-green-400 border-green-500/20' : 'bg-red-500/10 text-red-400 border-red-500/20'">
                    <span class="material-symbols-outlined text-[18px]">{{ saveStatus === 'success' ? 'check_circle' : 'error' }}</span>
                    {{ saveMessage }}
                  </div>
                </div>
                
                <!-- Next/Prev Buttons -->
                <div class="mt-8 flex justify-between gap-4">
                  <button 
                    class="px-4 py-2.5 rounded-xl border border-white/10 bg-white/5 text-sm font-semibold text-white/70 hover:bg-white/10 hover:text-white transition-colors disabled:opacity-30 disabled:cursor-not-allowed flex items-center gap-2"
                    :disabled="!curriculum || !curriculum.previousLessonId"
                    @click="goToLesson(curriculum.previousLessonId)"
                  >
                    <span class="material-symbols-outlined text-[18px]">chevron_left</span>
                    Bài trước
                  </button>
                  <button 
                    class="px-4 py-2.5 rounded-xl border border-white/10 bg-white/5 text-sm font-semibold text-white/70 hover:bg-white/10 hover:text-white transition-colors disabled:opacity-30 disabled:cursor-not-allowed flex items-center gap-2"
                    :disabled="!curriculum || !curriculum.nextLessonId"
                    @click="goToLesson(curriculum.nextLessonId)"
                  >
                    Bài tiếp theo
                    <span class="material-symbols-outlined text-[18px]">chevron_right</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- Quizzes Tab -->
            <div v-show="activeTab === 'quizzes'" class="animate-fade-in">
              <div class="max-w-3xl">
                <div v-if="isLoadingQuizzes" class="text-white/40 text-sm flex items-center gap-2">
                  <span class="material-symbols-outlined animate-spin text-[18px]">autorenew</span> Đang tải bài tập...
                </div>
                <div v-else-if="quizzesError" class="bg-red-500/10 text-red-400 p-4 rounded-xl text-sm border border-red-500/20">
                  {{ quizzesError }}
                </div>
                <div v-else-if="quizzes.length === 0" class="bg-white/5 border border-white/10 rounded-xl p-8 text-center text-white/40 text-sm">
                  Chưa có bài tập nào cho bài học này.
                </div>
                <div v-else class="space-y-4">
                  <div v-for="quiz in quizzes" :key="quiz.id" class="bg-white/5 border border-white/10 hover:border-stitch-primary/30 rounded-2xl p-5 sm:p-6 transition-colors flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center">
                    <div>
                      <h4 class="font-bold text-lg mb-2 text-white">{{ quiz.title }}</h4>
                      <div class="flex flex-wrap items-center gap-3 text-xs text-white/50">
                        <span v-if="quiz.questionCount" class="flex items-center gap-1 bg-white/10 px-2 py-1 rounded">
                          <span class="material-symbols-outlined text-[14px]">format_list_numbered</span>
                          {{ quiz.questionCount }} câu
                        </span>
                        <span v-if="quiz.timeLimitMinutes" class="flex items-center gap-1 bg-white/10 px-2 py-1 rounded">
                          <span class="material-symbols-outlined text-[14px]">timer</span>
                          {{ quiz.timeLimitMinutes }} phút
                        </span>
                        <span v-if="quiz.maxAttempts" class="flex items-center gap-1 bg-white/10 px-2 py-1 rounded">
                          <span class="material-symbols-outlined text-[14px]">replay</span>
                          {{ quiz.remainingAttempts !== null ? `Còn ${quiz.remainingAttempts}/${quiz.maxAttempts} lượt` : `${quiz.maxAttempts} lượt` }}
                        </span>
                      </div>
                      
                      <div v-if="quiz.latestAttemptId" class="mt-3 text-xs flex items-center gap-1.5" :class="quiz.latestPassed ? 'text-green-400' : 'text-red-400'">
                        <span class="material-symbols-outlined text-[14px]">{{ quiz.latestPassed ? 'check_circle' : 'cancel' }}</span>
                        Lần gần nhất: {{ quiz.latestScore }}/{{ quiz.passingScore }} điểm ({{ quiz.latestPassed ? 'Đạt' : 'Chưa đạt' }})
                      </div>
                    </div>
                    
                    <div class="flex gap-2 w-full sm:w-auto shrink-0 mt-2 sm:mt-0">
                      <router-link
                        v-if="quiz.latestAttemptId"
                        :to="`/student/quizzes/${quiz.id}/result/${quiz.latestAttemptId}`"
                        class="flex-1 sm:flex-none px-4 py-2 rounded-xl bg-white/10 text-white text-sm font-semibold text-center hover:bg-white/20 transition-colors"
                      >
                        Kết quả
                      </router-link>
                      <router-link
                        v-if="quiz.remainingAttempts === null || quiz.remainingAttempts > 0"
                        :to="`/student/quizzes/${quiz.id}`"
                        class="flex-1 sm:flex-none px-5 py-2 rounded-xl bg-stitch-primary text-white text-sm font-semibold text-center hover:bg-stitch-primary/90 transition-colors"
                      >
                        {{ quiz.latestAttemptId ? 'Làm lại' : 'Làm bài' }}
                      </router-link>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Resources Tab -->
            <div v-show="activeTab === 'resources'" class="animate-fade-in">
              <div class="max-w-3xl">
                <div v-if="isLoadingResources" class="text-white/40 text-sm flex items-center gap-2">
                  <span class="material-symbols-outlined animate-spin text-[18px]">autorenew</span> Đang tải...
                </div>
                <div v-else-if="resourceError" class="bg-red-500/10 text-red-400 p-4 rounded-xl text-sm border border-red-500/20">
                  {{ resourceError }}
                </div>
                <div v-else-if="resources.length === 0" class="bg-white/5 border border-white/10 rounded-xl p-8 text-center text-white/40 text-sm">
                  Chưa có tài liệu đính kèm.
                </div>
                <div v-else class="space-y-3">
                  <a 
                    v-for="res in resources" 
                    :key="res.id" 
                    :href="res.fileUrl" 
                    target="_blank" 
                    rel="noopener noreferrer" 
                    class="flex items-center justify-between p-4 rounded-xl border border-white/10 bg-white/5 hover:border-white/30 hover:bg-white/10 transition-colors group"
                  >
                    <div class="flex items-center gap-3 overflow-hidden">
                      <span class="px-2 py-1 rounded bg-white/10 text-white/70 text-[10px] uppercase tracking-wider shrink-0">
                        {{ res.resourceType }}
                      </span>
                      <span class="text-sm text-white group-hover:text-stitch-primary truncate transition-colors">
                        {{ res.title }}
                      </span>
                    </div>
                    <span v-if="res.fileSize" class="text-xs text-white/40 shrink-0 font-mono ml-4">
                      {{ formatFileSize(res.fileSize) }}
                    </span>
                  </a>
                </div>
              </div>
            </div>
            
          </div>
        </div>
      </div>
    </div>

    <!-- Sidebar -->
    <aside 
      class="w-72 bg-[#161b27] flex flex-col flex-shrink-0 z-20 transition-all duration-300 absolute lg:relative right-0 h-full border-l border-white/5"
      :class="sidebarOpen ? 'translate-x-0' : 'translate-x-full lg:translate-x-0 lg:hidden'"
    >
      <button @click="sidebarOpen = false" class="lg:hidden absolute top-3 right-3 text-white/50 p-2 z-30">
        ✕
      </button>
      <LearningCurriculumSidebar
        :curriculum="curriculum"
        :currentLessonId="lesson?.id"
        :isLoading="isLoadingCurriculum"
        :errorMsg="curriculumError"
      />
    </aside>
    
    <!-- Mobile overlay -->
    <div 
      v-if="sidebarOpen" 
      @click="sidebarOpen = false"
      class="lg:hidden fixed inset-0 bg-black/50 z-10"
    ></div>

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

const sidebarOpen = ref(false)
const activeTab = ref('progress') // Default tab

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
      
      // Auto switch to content if text lesson, else progress
      if (lesson.value.content && !lesson.value.videoUrl) {
        activeTab.value = 'content'
      } else {
        activeTab.value = 'progress'
      }
    } else {
      throw new Error(res.data?.message || 'Lỗi lấy dữ liệu')
    }
  } catch (error) {
    console.error('Learning error:', error)
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
    resourceError.value = 'Không thể tải tài liệu đính kèm.'
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
      
      const currentPercent = lesson.value.watchedPercent || 0
      lesson.value.watchedPercent = Math.max(currentPercent, payload.watchedPercent)
      lesson.value.isCompleted = payload.isCompleted || lesson.value.isCompleted
      
      progressForm.watchedPercent = lesson.value.watchedPercent
      progressForm.isCompleted = lesson.value.isCompleted
      
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
    sidebarOpen.value = false
  }
}

onMounted(() => {
  fetchLesson()
})

watch(() => route.params.id, () => {
  fetchLesson()
})

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

<style scoped>
.custom-scrollbar-horizontal::-webkit-scrollbar {
  height: 4px;
}
.custom-scrollbar-horizontal::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar-horizontal::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}
.custom-scrollbar-horizontal::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}
.animate-fade-in {
  animation: fadeIn 0.3s ease-out forwards;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
