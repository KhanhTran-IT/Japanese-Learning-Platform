<template>
  <div class="min-h-screen bg-stitch-background flex flex-col font-stitch-sans">
    
    <!-- Intro Phase (Before Start) -->
    <div v-if="!attemptId" class="flex-1 flex flex-col">
      <header class="h-16 border-b border-stitch-border flex items-center px-6 bg-white shrink-0">
        <router-link to="/student/dashboard" class="text-stitch-muted-foreground hover:text-stitch-foreground text-sm font-medium flex items-center gap-2">
          ← Quay lại
        </router-link>
      </header>

      <div class="flex-1 flex items-center justify-center px-4 py-8">
        <div v-if="isLoading" class="text-stitch-muted-foreground flex flex-col items-center">
          <span class="material-symbols-outlined animate-spin text-4xl mb-4">autorenew</span>
          <p>Đang tải bài quiz...</p>
        </div>
        <div v-else-if="errorMsg" class="max-w-md w-full text-center p-8 bg-white rounded-[24px] border border-red-200">
          <span class="material-symbols-outlined text-5xl text-red-500 mb-4">error</span>
          <h2 class="text-2xl font-bold mb-2">Không thể truy cập</h2>
          <p class="text-stitch-muted-foreground mb-6">{{ errorMsg }}</p>
          <router-link to="/student/dashboard" class="px-6 py-3 bg-stitch-primary text-white rounded-xl font-semibold">
            Về Dashboard
          </router-link>
        </div>
        <div v-else-if="quiz" class="max-w-md w-full text-center">
          <div class="w-20 h-20 rounded-[24px] bg-stitch-primary/10 text-stitch-primary flex items-center justify-center text-4xl mx-auto mb-6">
            📝
          </div>
          <h1 class="text-3xl md:text-4xl font-stitch-serif font-bold mb-3">{{ quiz.title }}</h1>
          <p class="text-stitch-muted-foreground mb-8">{{ quiz.description || 'Hoàn thành bài kiểm tra để đánh giá kiến thức của bạn.' }}</p>

          <div class="grid grid-cols-2 gap-4 mb-8">
            <div class="p-4 bg-white rounded-2xl border border-stitch-border">
              <div class="text-2xl mb-1">❓</div>
              <div class="text-sm font-medium text-stitch-foreground">{{ quiz.questions.length }} câu hỏi</div>
            </div>
            <div class="p-4 bg-white rounded-2xl border border-stitch-border">
              <div class="text-2xl mb-1">⏱</div>
              <div class="text-sm font-medium text-stitch-foreground">{{ quiz.timeLimitMinutes > 0 ? `${quiz.timeLimitMinutes} phút` : 'Không giới hạn' }}</div>
            </div>
            <div class="p-4 bg-white rounded-2xl border border-stitch-border">
              <div class="text-2xl mb-1">⭐</div>
              <div class="text-sm font-medium text-stitch-foreground">Đạt: {{ quiz.passingScore }} điểm</div>
            </div>
            <div class="p-4 bg-white rounded-2xl border border-stitch-border">
              <div class="text-2xl mb-1">🔁</div>
              <div class="text-sm font-medium text-stitch-foreground">
                Tối đa: {{ quiz.maxAttempts ? `${quiz.maxAttempts} lần` : 'Vô hạn' }}
              </div>
            </div>
          </div>

          <div v-if="startError" class="mb-6 p-4 rounded-xl bg-red-50 text-red-600 text-sm border border-red-100 flex items-center justify-center gap-2">
            <span class="material-symbols-outlined text-[18px]">error</span>
            {{ startError }}
          </div>

          <button
            @click="handleStartQuiz"
            :disabled="isStarting"
            class="w-full py-4 bg-stitch-primary text-white rounded-2xl font-bold text-lg hover:bg-stitch-primary/90 transition-all active:scale-95 disabled:opacity-50 flex items-center justify-center gap-2"
          >
            <span v-if="isStarting" class="material-symbols-outlined animate-spin">autorenew</span>
            {{ isStarting ? 'Đang chuẩn bị...' : 'Bắt đầu làm bài →' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Quiz Phase (After Start) -->
    <template v-else>
      <!-- Progress Header -->
      <div class="bg-white border-b border-stitch-border px-4 py-3 flex items-center gap-4 sticky top-0 z-10 shrink-0">
        <button @click="confirmExit" class="text-stitch-muted-foreground hover:text-stitch-foreground font-medium text-xl w-8 h-8 flex items-center justify-center rounded-full hover:bg-stitch-muted transition-colors">
          ✕
        </button>
        <div class="flex-1 h-2 bg-stitch-muted rounded-full overflow-hidden">
          <div 
            class="h-full bg-gradient-to-r from-stitch-primary to-stitch-accent rounded-full transition-all duration-500" 
            :style="{ width: progressPct + '%' }" 
          />
        </div>
        <span class="text-sm text-stitch-muted-foreground whitespace-nowrap font-medium min-w-[3rem] text-right">
          {{ currentQ + 1 }} / {{ quiz.questions.length }}
        </span>
      </div>

      <div class="flex-1 flex flex-col items-center justify-start md:justify-center px-4 py-8 overflow-y-auto">
        <div class="w-full max-w-2xl">
          
          <!-- Top indicators -->
          <div class="flex items-center justify-between mb-6">
            <span class="px-3 py-1 bg-stitch-primary/10 text-stitch-primary text-xs font-bold rounded-full uppercase tracking-wider">
              {{ questionTypeLabel(q.questionType) }}
            </span>
            <div v-if="quiz.timeLimitMinutes > 0" class="flex items-center gap-1.5 text-sm font-medium bg-white px-3 py-1.5 rounded-full border border-stitch-border shadow-sm">
              <span class="text-amber-500 material-symbols-outlined text-[16px]">timer</span>
              <span class="font-mono font-bold" :class="remainingSeconds <= 60 ? 'text-red-500 animate-pulse' : 'text-stitch-foreground'">
                {{ formattedTime }}
              </span>
            </div>
          </div>

          <!-- Question Card -->
          <div class="bg-white rounded-[32px] border border-stitch-border p-8 mb-6 shadow-sm">
            <div class="text-center">
              <div class="text-xs text-stitch-muted-foreground uppercase tracking-widest mb-4 font-semibold">
                Câu {{ currentQ + 1 }} ({{ q.points }} điểm)
              </div>
              <h2 class="text-2xl md:text-3xl font-stitch-serif font-bold leading-snug text-stitch-foreground">{{ q.content }}</h2>
            </div>
            
            <!-- Media -->
            <div v-if="q.imageUrl" class="mt-6 rounded-2xl overflow-hidden border border-stitch-border">
              <img :src="q.imageUrl" alt="Question Image" class="w-full max-h-64 object-contain bg-stitch-muted" />
            </div>
            <div v-if="q.audioUrl" class="mt-6">
              <audio :src="q.audioUrl" controls class="w-full rounded-full"></audio>
            </div>
          </div>

          <!-- Answer Options -->
          <div v-if="q.questionType === 'SINGLE_CHOICE' || q.questionType === 'TRUE_FALSE'" class="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-8">
            <button
              v-for="(ans, i) in q.answers"
              :key="ans.id"
              @click="handleSelect(ans.id)"
              class="p-5 rounded-2xl text-left transition-all font-medium border-2 flex items-start gap-3"
              :class="userAnswers[q.id]?.answerId === ans.id 
                ? 'bg-stitch-primary/10 border-stitch-primary text-stitch-foreground shadow-sm' 
                : 'bg-white border-stitch-border text-stitch-foreground hover:border-stitch-primary/50 hover:bg-stitch-primary/5'"
            >
              <div class="w-6 h-6 shrink-0 rounded-full border-2 flex items-center justify-center transition-colors"
                   :class="userAnswers[q.id]?.answerId === ans.id ? 'border-stitch-primary bg-stitch-primary text-white' : 'border-stitch-muted-foreground/30'">
                <div v-if="userAnswers[q.id]?.answerId === ans.id" class="w-2 h-2 rounded-full bg-white"></div>
              </div>
              <div class="flex-1">
                <span class="block text-xs text-stitch-muted-foreground mb-1 font-semibold uppercase tracking-wider">
                  Tùy chọn {{ String.fromCharCode(65 + i) }}
                </span>
                <span class="text-lg md:text-xl font-stitch-sans leading-tight">{{ ans.content }}</span>
              </div>
            </button>
          </div>
          
          <div v-else class="mb-8">
            <textarea
              v-model="textAnswerTemp"
              @input="handleTextSelect"
              placeholder="Nhập câu trả lời của bạn..."
              class="w-full bg-white border-2 border-stitch-border rounded-2xl p-5 text-lg min-h-[120px] focus:outline-none focus:border-stitch-primary resize-none transition-colors"
            ></textarea>
          </div>
          
          <!-- Submit Error -->
          <div v-if="submitError" class="mb-6 p-4 rounded-xl bg-red-50 text-red-600 text-sm border border-red-100 flex items-center justify-center gap-2">
            <span class="material-symbols-outlined text-[18px]">error</span>
            {{ submitError }}
          </div>

          <!-- Navigation / Submit Button -->
          <div class="flex items-center gap-4">
            <button 
              v-if="currentQ > 0"
              @click="handlePrev"
              class="px-6 py-4 rounded-2xl border-2 border-stitch-border bg-white text-stitch-foreground font-semibold hover:bg-stitch-muted transition-colors"
              :disabled="isSubmitting"
            >
              Quay lại
            </button>
            <button
              @click="handleNext"
              :disabled="isSubmitting"
              class="flex-1 py-4 bg-stitch-foreground text-white font-bold rounded-2xl hover:bg-stitch-foreground/90 transition-all text-lg flex items-center justify-center gap-2 shadow-lg disabled:opacity-50"
            >
              <span v-if="isSubmitting" class="material-symbols-outlined animate-spin">autorenew</span>
              <template v-else>
                {{ currentQ < quiz.questions.length - 1 ? 'Câu tiếp theo →' : 'Nộp bài 🚀' }}
              </template>
            </button>
          </div>
          
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { QuizService } from '@/services/quiz.service'
import { getApiErrorMessage } from '@/utils/api-error'

const route = useRoute()
const router = useRouter()

// Intro States
const isLoading = ref(true)
const errorMsg = ref('')
const quiz = ref(null)

const isStarting = ref(false)
const startError = ref('')
const attemptId = ref(null)

// Quiz States
const currentQ = ref(0)
const textAnswerTemp = ref('')

const isSubmitting = ref(false)
const submitError = ref('')
const isFinished = ref(false)

// Timer states
const remainingSeconds = ref(0)
let timerInterval = null

// User answers: { [questionId]: { answerId?: number, userAnswerText?: string } }
const userAnswers = reactive({})

const QUESTION_TYPE_LABELS = {
  SINGLE_CHOICE: 'Trắc nghiệm',
  TRUE_FALSE: 'Đúng/Sai',
  MULTIPLE_CHOICE: 'Chọn nhiều',
  FILL_BLANK: 'Điền từ'
}

const questionTypeLabel = (type) => QUESTION_TYPE_LABELS[type] || type

// Computed
const q = computed(() => {
  if (!quiz.value || !quiz.value.questions || quiz.value.questions.length === 0) return null
  return quiz.value.questions[currentQ.value]
})

const answeredCount = computed(() => {
  if (!quiz.value) return 0
  return quiz.value.questions.filter(qItem => {
    const answer = userAnswers[qItem.id]
    return answer && (answer.answerId || answer.userAnswerText?.trim())
  }).length
})

const progressPct = computed(() => {
  if (!quiz.value || quiz.value.questions.length === 0) return 0
  return Math.round((answeredCount.value / quiz.value.questions.length) * 100)
})

const formattedTime = computed(() => {
  const m = Math.floor(remainingSeconds.value / 60)
  const s = remainingSeconds.value % 60
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
})

// Watchers
watch(currentQ, () => {
  if (q.value && q.value.questionType !== 'SINGLE_CHOICE' && q.value.questionType !== 'TRUE_FALSE') {
    textAnswerTemp.value = userAnswers[q.value.id]?.userAnswerText || ''
  }
})

// Route Leave Guard
onBeforeRouteLeave((to, from, next) => {
  if (attemptId.value && !isFinished.value && !isSubmitting.value) {
    const answer = window.confirm('Bạn đang làm bài kiểm tra. Nếu thoát bây giờ, kết quả sẽ bị mất và được tính là 1 lần làm bài. Bạn có chắc chắn muốn thoát?')
    if (answer) {
      clearInterval(timerInterval)
      next()
    } else {
      next(false)
    }
  } else {
    next()
  }
})

// Actions
const handleSelect = (answerId) => {
  if (q.value) {
    userAnswers[q.value.id] = { answerId }
  }
}

const handleTextSelect = () => {
  if (q.value) {
    userAnswers[q.value.id] = { userAnswerText: textAnswerTemp.value }
  }
}

const handlePrev = () => {
  if (currentQ.value > 0) {
    currentQ.value--
  }
}

const handleNext = () => {
  if (currentQ.value < quiz.value.questions.length - 1) {
    currentQ.value++
  } else {
    handleSubmitQuiz()
  }
}

const confirmExit = () => {
  router.push('/student/dashboard') // This will trigger the route leave guard
}

const fetchQuiz = async () => {
  const quizId = route.params.quizId
  isLoading.value = true
  errorMsg.value = ''

  try {
    const res = await QuizService.getQuiz(quizId)
    if (res.data && res.data.code === 1000) {
      quiz.value = res.data.result
    } else {
      throw new Error(res.data?.message || 'Lỗi lấy dữ liệu quiz')
    }
  } catch (error) {
    if (error.response?.status === 404) {
      errorMsg.value = 'Bài kiểm tra không tồn tại hoặc chưa được phát hành.'
    } else if (error.response?.status === 403) {
      errorMsg.value = 'Bạn chưa được phép làm bài kiểm tra này.'
    } else {
      errorMsg.value = getApiErrorMessage(error)
    }
  } finally {
    isLoading.value = false
  }
}

const handleStartQuiz = async () => {
  const quizId = route.params.quizId
  isStarting.value = true
  startError.value = ''

  try {
    const res = await QuizService.startQuiz(quizId)
    if (res.data && res.data.code === 1000) {
      attemptId.value = res.data.result.attemptId
      if (quiz.value.timeLimitMinutes > 0 && res.data.result.startedAt) {
        startTimer(res.data.result.startedAt)
      }
      
      // Init temp text
      if (q.value && q.value.questionType !== 'SINGLE_CHOICE' && q.value.questionType !== 'TRUE_FALSE') {
        textAnswerTemp.value = ''
      }
    } else {
      throw new Error(res.data?.message || 'Không thể bắt đầu làm bài')
    }
  } catch (error) {
    startError.value = getApiErrorMessage(error, 'Không thể bắt đầu làm bài. Có thể bạn đã hết số lần làm.')
  } finally {
    isStarting.value = false
  }
}

const startTimer = (startedAtIso) => {
  // Use the reliable backend contract for start time
  const startedAt = new Date(startedAtIso).getTime()
  const timeLimitMs = quiz.value.timeLimitMinutes * 60 * 1000
  const endTime = startedAt + timeLimitMs

  updateTimer(endTime)
  timerInterval = setInterval(() => updateTimer(endTime), 1000)
}

const updateTimer = (endTime) => {
  const now = Date.now()
  const diff = Math.floor((endTime - now) / 1000)
  
  if (diff <= 0) {
    remainingSeconds.value = 0
    clearInterval(timerInterval)
    if (!isSubmitting.value && !isFinished.value) {
      submitError.value = 'Hết thời gian làm bài. Hệ thống đang tự động nộp bài...'
      handleSubmitQuiz()
    }
  } else {
    remainingSeconds.value = diff
  }
}

const handleSubmitQuiz = async () => {
  if (!attemptId.value || isSubmitting.value) return

  // Verify answering progress
  if (answeredCount.value < quiz.value.questions.length && remainingSeconds.value > 0) {
    const confirmSubmit = window.confirm(`Bạn mới trả lời ${answeredCount.value}/${quiz.value.questions.length} câu. Bạn có chắc chắn muốn nộp bài không?`)
    if (!confirmSubmit) return
  }

  const quizId = route.params.quizId
  isSubmitting.value = true
  submitError.value = ''

  // Build payload
  const answers = quiz.value.questions
    .filter(qItem => userAnswers[qItem.id])
    .map(qItem => {
      const answer = userAnswers[qItem.id]
      const payload = { questionId: qItem.id }
      if (answer.answerId) payload.answerId = answer.answerId
      if (answer.userAnswerText) payload.userAnswerText = answer.userAnswerText
      return payload
    })

  try {
    const res = await QuizService.submitQuiz(quizId, {
      attemptId: attemptId.value,
      answers
    })

    if (res.data && res.data.code === 1000) {
      isFinished.value = true // Bypass route guard
      clearInterval(timerInterval)
      router.push(`/student/quizzes/${quizId}/result/${attemptId.value}`)
    } else {
      throw new Error(res.data?.message || 'Nộp bài thất bại')
    }
  } catch (error) {
    submitError.value = getApiErrorMessage(error, 'Không thể nộp bài. Vui lòng thử lại.')
  } finally {
    isSubmitting.value = false
  }
}

onMounted(fetchQuiz)

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
})
</script>
