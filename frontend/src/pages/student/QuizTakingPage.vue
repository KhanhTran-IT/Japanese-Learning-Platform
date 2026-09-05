<template>
  <div class="flex flex-col min-h-screen bg-background font-body-md text-on-surface">
    <!-- Header -->
    <header class="h-16 bg-surface-container-lowest border-b border-paper-shadow flex items-center justify-between px-6 sticky top-0 z-10 shrink-0">
      <router-link to="/student/dashboard" class="inline-flex items-center gap-2 text-secondary hover:text-primary transition-colors font-button text-sm">
        <span class="material-symbols-outlined text-[20px]">arrow_back</span>
        Quay lại Dashboard
      </router-link>
      <div v-if="quiz && attemptId" class="flex items-center gap-3">
        <span class="px-3 py-1 rounded-full bg-surface-container-high text-secondary font-label-sm text-[12px] tracking-wider uppercase flex items-center gap-1">
          <span class="material-symbols-outlined text-[16px]">check_circle</span>
          {{ answeredCount }}/{{ quiz.questions.length }} câu đã trả lời
        </span>
      </div>
    </header>

    <!-- Loading State -->
    <div v-if="isLoading" class="flex-1 flex flex-col items-center justify-center py-20 text-secondary">
      <span class="material-symbols-outlined animate-spin text-4xl mb-4">autorenew</span>
      <p class="font-body-md">Đang tải bài quiz...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="errorMsg" class="flex-1 flex flex-col items-center justify-center py-20 text-center max-w-lg mx-auto px-4">
      <span class="material-symbols-outlined text-5xl mb-4 text-error">error</span>
      <h2 class="font-headline-md text-2xl text-ink-black mb-2">Không thể truy cập</h2>
      <p class="font-body-md text-secondary mb-6">{{ errorMsg }}</p>
      <router-link to="/student/dashboard" class="bg-primary text-on-primary px-6 py-3 rounded-xl font-button hover:opacity-90 transition-all shadow-md">
        Về Dashboard
      </router-link>
    </div>

    <!-- Quiz Content -->
    <main v-else-if="quiz" class="flex-1 max-w-3xl mx-auto w-full px-4 md:px-8 py-8 md:py-12 pb-24">

      <!-- Quiz Info (Before Start) -->
      <div v-if="!attemptId" class="zen-card p-8 md:p-10 rounded-[24px] text-center">
        <div class="w-16 h-16 rounded-full bg-primary/10 text-primary flex items-center justify-center mx-auto mb-6">
          <span class="material-symbols-outlined text-3xl">quiz</span>
        </div>
        <h1 class="font-headline-lg text-2xl md:text-3xl text-ink-black mb-3">{{ quiz.title }}</h1>
        <p v-if="quiz.description" class="font-body-md text-secondary mb-8 max-w-xl mx-auto">{{ quiz.description }}</p>

        <!-- Quiz Meta -->
        <div class="flex flex-wrap justify-center gap-4 mb-8">
          <div class="flex items-center gap-2 px-4 py-2 rounded-xl bg-surface-container-high text-secondary font-label-sm">
            <span class="material-symbols-outlined text-[18px]">help_outline</span>
            {{ quiz.questions.length }} câu hỏi
          </div>
          <div v-if="quiz.timeLimitMinutes" class="flex items-center gap-2 px-4 py-2 rounded-xl bg-surface-container-high text-secondary font-label-sm">
            <span class="material-symbols-outlined text-[18px]">schedule</span>
            {{ quiz.timeLimitMinutes }} phút
          </div>
          <div class="flex items-center gap-2 px-4 py-2 rounded-xl bg-surface-container-high text-secondary font-label-sm">
            <span class="material-symbols-outlined text-[18px]">emoji_events</span>
            Điểm đạt: {{ quiz.passingScore }}
          </div>
          <div v-if="quiz.maxAttempts" class="flex items-center gap-2 px-4 py-2 rounded-xl bg-surface-container-high text-secondary font-label-sm">
            <span class="material-symbols-outlined text-[18px]">repeat</span>
            Tối đa {{ quiz.maxAttempts }} lần
          </div>
        </div>

        <!-- Start Button -->
        <button
          @click="handleStartQuiz"
          :disabled="isStarting"
          class="bg-primary text-on-primary px-8 py-3 rounded-xl font-button text-lg hover:opacity-90 transition-all shadow-md hover:shadow-lg active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 mx-auto"
        >
          <span v-if="isStarting" class="material-symbols-outlined animate-spin text-[20px]">autorenew</span>
          <span v-else class="material-symbols-outlined text-[20px]">play_arrow</span>
          {{ isStarting ? 'Đang bắt đầu...' : 'Bắt đầu làm bài' }}
        </button>

        <!-- Start Error -->
        <div v-if="startError" class="mt-6 p-4 rounded-xl bg-error-container/50 text-error border border-error/20 font-body-md text-sm flex items-center justify-center gap-2">
          <span class="material-symbols-outlined text-[18px]">error</span>
          {{ startError }}
        </div>
      </div>

      <!-- Questions (After Start) -->
      <template v-if="attemptId">
        <h1 class="font-headline-lg text-2xl md:text-3xl text-ink-black mb-2">{{ quiz.title }}</h1>
        <p class="font-body-md text-secondary mb-8">Hãy chọn đáp án phù hợp nhất cho từng câu hỏi.</p>

        <!-- Progress Bar -->
        <div class="mb-8">
          <div class="flex justify-between items-center mb-2">
            <span class="font-label-sm text-secondary">Tiến độ</span>
            <span class="font-label-sm text-primary">{{ answeredCount }}/{{ quiz.questions.length }}</span>
          </div>
          <div class="h-2.5 bg-surface-container-high rounded-full overflow-hidden">
            <div
              class="h-full bg-gradient-to-r from-primary to-primary-container transition-all duration-500 ease-out rounded-full"
              :style="{ width: progressPercent + '%' }"
            ></div>
          </div>
        </div>

        <!-- Question Cards -->
        <div class="space-y-6">
          <div
            v-for="(question, qIndex) in quiz.questions"
            :key="question.id"
            class="zen-card p-6 md:p-8 rounded-[24px] transition-all"
            :class="{ 'border-2 border-primary/30': userAnswers[question.id]?.answerId || userAnswers[question.id]?.userAnswerText }"
          >
            <!-- Question Header -->
            <div class="flex items-start gap-3 mb-5">
              <span
                class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold shrink-0"
                :class="(userAnswers[question.id]?.answerId || userAnswers[question.id]?.userAnswerText)
                  ? 'bg-primary text-on-primary'
                  : 'bg-surface-container-high text-secondary'"
              >
                {{ qIndex + 1 }}
              </span>
              <div class="flex-1">
                <p class="font-headline-md text-lg text-ink-black leading-relaxed">{{ question.content }}</p>
                <span class="font-label-sm text-secondary text-xs mt-1 inline-block">
                  {{ questionTypeLabel(question.questionType) }} · {{ question.points }} điểm
                </span>
              </div>
            </div>

            <!-- Question Media -->
            <div v-if="question.imageUrl" class="mb-4 rounded-xl overflow-hidden">
              <img :src="question.imageUrl" :alt="'Hình ảnh câu ' + (qIndex + 1)" class="max-w-full h-auto rounded-xl" />
            </div>
            <div v-if="question.audioUrl" class="mb-4">
              <audio :src="question.audioUrl" controls class="w-full"></audio>
            </div>

            <!-- SINGLE_CHOICE / TRUE_FALSE: Radio buttons -->
            <div v-if="question.questionType === 'SINGLE_CHOICE' || question.questionType === 'TRUE_FALSE'" class="space-y-3">
              <label
                v-for="answer in question.answers"
                :key="answer.id"
                class="flex items-center gap-3 p-4 rounded-xl border cursor-pointer transition-all group"
                :class="userAnswers[question.id]?.answerId === answer.id
                  ? 'border-primary bg-primary/5 shadow-sm'
                  : 'border-paper-shadow bg-surface-container-lowest hover:border-outline-variant hover:bg-surface-container-low'"
              >
                <input
                  type="radio"
                  :name="'question-' + question.id"
                  :value="answer.id"
                  :checked="userAnswers[question.id]?.answerId === answer.id"
                  @change="selectAnswer(question.id, answer.id)"
                  class="w-5 h-5 accent-primary cursor-pointer shrink-0"
                />
                <span class="font-body-md text-on-surface group-hover:text-ink-black transition-colors">{{ answer.content }}</span>
              </label>
            </div>

            <!-- Unsupported types: Fallback text input -->
            <div v-else class="space-y-3">
              <div class="p-3 rounded-xl bg-tertiary-fixed/30 text-on-tertiary-fixed-variant font-label-sm text-sm flex items-center gap-2">
                <span class="material-symbols-outlined text-[18px]">info</span>
                Loại câu hỏi "{{ questionTypeLabel(question.questionType) }}" chưa hỗ trợ giao diện đầy đủ. Vui lòng nhập đáp án dạng text.
              </div>
              <input
                type="text"
                :placeholder="'Nhập đáp án cho câu ' + (qIndex + 1)"
                :value="userAnswers[question.id]?.userAnswerText || ''"
                @input="setTextAnswer(question.id, $event.target.value)"
                class="w-full px-4 py-3 rounded-xl border border-paper-shadow bg-surface-container-lowest focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all font-body-md"
              />
            </div>
          </div>
        </div>

        <!-- Submit Section -->
        <div class="mt-10 zen-card p-6 md:p-8 rounded-[24px] flex flex-col sm:flex-row items-center justify-between gap-4">
          <div class="text-center sm:text-left">
            <p class="font-headline-md text-lg text-ink-black">Sẵn sàng nộp bài?</p>
            <p class="font-body-md text-secondary text-sm">
              Bạn đã trả lời {{ answeredCount }}/{{ quiz.questions.length }} câu hỏi.
              <span v-if="answeredCount < quiz.questions.length" class="text-warning-amber">Còn {{ quiz.questions.length - answeredCount }} câu chưa trả lời.</span>
            </p>
          </div>
          <button
            @click="handleSubmitQuiz"
            :disabled="isSubmitting || answeredCount === 0"
            class="bg-primary text-on-primary px-8 py-3 rounded-xl font-button hover:opacity-90 transition-all shadow-md hover:shadow-lg active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 shrink-0"
          >
            <span v-if="isSubmitting" class="material-symbols-outlined animate-spin text-[20px]">autorenew</span>
            <span v-else class="material-symbols-outlined text-[20px]">send</span>
            {{ isSubmitting ? 'Đang nộp...' : 'Nộp bài' }}
          </button>
        </div>

        <!-- Submit Error -->
        <div v-if="submitError" class="mt-4 p-4 rounded-xl bg-error-container/50 text-error border border-error/20 font-body-md text-sm flex items-center gap-2">
          <span class="material-symbols-outlined text-[18px]">error</span>
          {{ submitError }}
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { QuizService } from '@/services/quiz.service'
import { getApiErrorMessage } from '@/utils/api-error'

const route = useRoute()
const router = useRouter()

// States
const isLoading = ref(true)
const errorMsg = ref('')
const quiz = ref(null)

const isStarting = ref(false)
const startError = ref('')
const attemptId = ref(null)

const isSubmitting = ref(false)
const submitError = ref('')

// User answers: { [questionId]: { answerId?: number, userAnswerText?: string } }
const userAnswers = reactive({})

// Question type labels
const QUESTION_TYPE_LABELS = {
  SINGLE_CHOICE: 'Trắc nghiệm',
  TRUE_FALSE: 'Đúng/Sai',
  MULTIPLE_CHOICE: 'Chọn nhiều',
  FILL_BLANK: 'Điền từ',
  MATCHING: 'Nối đáp án',
  LISTENING: 'Nghe',
  REORDER: 'Sắp xếp'
}

const questionTypeLabel = (type) => QUESTION_TYPE_LABELS[type] || type

// Computed
const answeredCount = computed(() => {
  if (!quiz.value) return 0
  return quiz.value.questions.filter(q => {
    const answer = userAnswers[q.id]
    return answer && (answer.answerId || answer.userAnswerText)
  }).length
})

const progressPercent = computed(() => {
  if (!quiz.value || quiz.value.questions.length === 0) return 0
  return Math.round((answeredCount.value / quiz.value.questions.length) * 100)
})

// Actions
const selectAnswer = (questionId, answerId) => {
  userAnswers[questionId] = { answerId }
}

const setTextAnswer = (questionId, text) => {
  userAnswers[questionId] = { userAnswerText: text }
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
    console.error('Quiz fetch error:', error)
    if (error.response?.status === 404) {
      errorMsg.value = 'Quiz không tồn tại hoặc chưa được xuất bản.'
    } else if (error.response?.status === 403) {
      errorMsg.value = 'Bạn chưa ghi danh khóa học chứa quiz này.'
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
    } else {
      throw new Error(res.data?.message || 'Không thể bắt đầu làm bài')
    }
  } catch (error) {
    console.error('Start quiz error:', error)
    startError.value = getApiErrorMessage(error, 'Không thể bắt đầu làm bài. Có thể bạn đã hết số lần làm quiz.')
  } finally {
    isStarting.value = false
  }
}

const handleSubmitQuiz = async () => {
  if (!attemptId.value || isSubmitting.value) return

  const quizId = route.params.quizId
  isSubmitting.value = true
  submitError.value = ''

  // Build answers payload
  const answers = quiz.value.questions
    .filter(q => userAnswers[q.id])
    .map(q => {
      const answer = userAnswers[q.id]
      const payload = { questionId: q.id }

      if (answer.answerId) {
        payload.answerId = answer.answerId
      }
      if (answer.userAnswerText) {
        payload.userAnswerText = answer.userAnswerText
      }

      return payload
    })

  try {
    const res = await QuizService.submitQuiz(quizId, {
      attemptId: attemptId.value,
      answers
    })

    if (res.data && res.data.code === 1000) {
      // Navigate to result page
      router.push(`/student/quizzes/${quizId}/result/${attemptId.value}`)
    } else {
      throw new Error(res.data?.message || 'Nộp bài thất bại')
    }
  } catch (error) {
    console.error('Submit quiz error:', error)
    submitError.value = getApiErrorMessage(error, 'Không thể nộp bài. Vui lòng thử lại.')
  } finally {
    isSubmitting.value = false
  }
}

onMounted(fetchQuiz)
</script>
