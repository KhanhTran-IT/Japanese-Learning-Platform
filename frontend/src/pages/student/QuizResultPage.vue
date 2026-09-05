<template>
  <div class="flex flex-col min-h-screen bg-background font-body-md text-on-surface">
    <!-- Header -->
    <header class="h-16 bg-surface-container-lowest border-b border-paper-shadow flex items-center justify-between px-6 sticky top-0 z-10 shrink-0">
      <router-link to="/student/dashboard" class="inline-flex items-center gap-2 text-secondary hover:text-primary transition-colors font-button text-sm">
        <span class="material-symbols-outlined text-[20px]">arrow_back</span>
        Quay lại Dashboard
      </router-link>
    </header>

    <!-- Loading State -->
    <div v-if="isLoading" class="flex-1 flex flex-col items-center justify-center py-20 text-secondary">
      <span class="material-symbols-outlined animate-spin text-4xl mb-4">autorenew</span>
      <p class="font-body-md">Đang tải kết quả...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="errorMsg" class="flex-1 flex flex-col items-center justify-center py-20 text-center max-w-lg mx-auto px-4">
      <span class="material-symbols-outlined text-5xl mb-4 text-error">error</span>
      <h2 class="font-headline-md text-2xl text-ink-black mb-2">Không thể tải kết quả</h2>
      <p class="font-body-md text-secondary mb-6">{{ errorMsg }}</p>
      <router-link to="/student/dashboard" class="bg-primary text-on-primary px-6 py-3 rounded-xl font-button hover:opacity-90 transition-all shadow-md">
        Về Dashboard
      </router-link>
    </div>

    <!-- Result Content -->
    <main v-else-if="result" class="flex-1 max-w-3xl mx-auto w-full px-4 md:px-8 py-8 md:py-12 pb-24">

      <!-- Result Summary Card -->
      <div class="zen-card p-8 md:p-10 rounded-[24px] text-center mb-8">
        <!-- Pass/Fail Icon -->
        <div
          class="w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6"
          :class="result.passed ? 'bg-success-green/15' : 'bg-error/10'"
        >
          <span
            class="material-symbols-outlined text-4xl"
            :class="result.passed ? 'text-success-green' : 'text-error'"
          >
            {{ result.passed ? 'emoji_events' : 'sentiment_dissatisfied' }}
          </span>
        </div>

        <!-- Pass/Fail Badge -->
        <div
          class="inline-flex items-center gap-2 px-5 py-2 rounded-full font-button text-sm mb-4"
          :class="result.passed
            ? 'bg-success-green/15 text-success-green border border-success-green/30'
            : 'bg-error/10 text-error border border-error/30'"
        >
          <span class="material-symbols-outlined text-[18px]">
            {{ result.passed ? 'check_circle' : 'cancel' }}
          </span>
          {{ result.passed ? 'ĐẠT' : 'CHƯA ĐẠT' }}
        </div>

        <h1 class="font-headline-lg text-2xl md:text-3xl text-ink-black mb-2">{{ result.quizTitle }}</h1>

        <!-- Score Display -->
        <div class="flex items-baseline justify-center gap-1 mb-6">
          <span class="font-headline-lg text-5xl md:text-6xl" :class="result.passed ? 'text-success-green' : 'text-error'">
            {{ result.score }}
          </span>
          <span class="font-body-md text-secondary text-lg">/ {{ result.passingScore }} điểm đạt</span>
        </div>

        <!-- Stats Grid -->
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
          <div class="p-4 rounded-xl bg-surface-container-high">
            <p class="font-label-sm text-secondary text-xs uppercase tracking-wider mb-1">Tổng câu</p>
            <p class="font-headline-md text-xl text-ink-black">{{ result.totalQuestions }}</p>
          </div>
          <div class="p-4 rounded-xl bg-success-green/10">
            <p class="font-label-sm text-success-green text-xs uppercase tracking-wider mb-1">Đúng</p>
            <p class="font-headline-md text-xl text-success-green">{{ result.correctCount }}</p>
          </div>
          <div class="p-4 rounded-xl bg-error/10">
            <p class="font-label-sm text-error text-xs uppercase tracking-wider mb-1">Sai</p>
            <p class="font-headline-md text-xl text-error">{{ result.wrongCount }}</p>
          </div>
          <div class="p-4 rounded-xl bg-surface-container-high">
            <p class="font-label-sm text-secondary text-xs uppercase tracking-wider mb-1">Trạng thái</p>
            <p class="font-headline-md text-xl text-ink-black">{{ result.status }}</p>
          </div>
        </div>

        <!-- Time Info -->
        <div v-if="result.startedAt || result.submittedAt" class="flex flex-wrap justify-center gap-4 text-sm text-secondary font-body-md">
          <span v-if="result.startedAt" class="flex items-center gap-1">
            <span class="material-symbols-outlined text-[16px]">schedule</span>
            Bắt đầu: {{ formatDateTime(result.startedAt) }}
          </span>
          <span v-if="result.submittedAt" class="flex items-center gap-1">
            <span class="material-symbols-outlined text-[16px]">send</span>
            Nộp: {{ formatDateTime(result.submittedAt) }}
          </span>
        </div>
      </div>

      <!-- Detailed Answers -->
      <h2 class="font-headline-md text-xl text-ink-black mb-6 flex items-center gap-2">
        <span class="material-symbols-outlined text-primary">fact_check</span>
        Chi tiết đáp án
      </h2>

      <div class="space-y-4">
        <div
          v-for="(answer, index) in result.answers"
          :key="answer.questionId"
          class="zen-card p-6 rounded-[24px] transition-all border-2"
          :class="answer.isCorrect ? 'border-success-green/20' : 'border-error/20'"
        >
          <!-- Question -->
          <div class="flex items-start gap-3 mb-4">
            <span
              class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold shrink-0"
              :class="answer.isCorrect ? 'bg-success-green/15 text-success-green' : 'bg-error/10 text-error'"
            >
              {{ index + 1 }}
            </span>
            <div class="flex-1">
              <p class="font-headline-md text-lg text-ink-black leading-relaxed">{{ answer.questionContent }}</p>
              <span class="font-label-sm text-secondary text-xs mt-1 inline-flex items-center gap-1">
                {{ questionTypeLabel(answer.questionType) }}
                · {{ answer.pointsEarned }} điểm
                <span
                  class="material-symbols-outlined text-[16px] ml-1"
                  :class="answer.isCorrect ? 'text-success-green' : 'text-error'"
                >
                  {{ answer.isCorrect ? 'check_circle' : 'cancel' }}
                </span>
              </span>
            </div>
          </div>

          <!-- Answer Details -->
          <div class="space-y-2 ml-11">
            <!-- User's answer -->
            <div class="flex items-start gap-2 p-3 rounded-xl" :class="answer.isCorrect ? 'bg-success-green/5' : 'bg-error/5'">
              <span class="material-symbols-outlined text-[18px] mt-0.5 shrink-0" :class="answer.isCorrect ? 'text-success-green' : 'text-error'">
                {{ answer.isCorrect ? 'check' : 'close' }}
              </span>
              <div>
                <span class="font-label-sm text-secondary text-xs uppercase tracking-wider">Đáp án của bạn:</span>
                <p class="font-body-md text-on-surface">
                  {{ answer.selectedAnswerContent || answer.userAnswerText || '(Không trả lời)' }}
                </p>
              </div>
            </div>

            <!-- Correct answer (only show if wrong) -->
            <div v-if="!answer.isCorrect && answer.correctAnswerContent" class="flex items-start gap-2 p-3 rounded-xl bg-success-green/5">
              <span class="material-symbols-outlined text-[18px] mt-0.5 text-success-green shrink-0">check</span>
              <div>
                <span class="font-label-sm text-secondary text-xs uppercase tracking-wider">Đáp án đúng:</span>
                <p class="font-body-md text-success-green font-medium">{{ answer.correctAnswerContent }}</p>
              </div>
            </div>

            <!-- Explanation -->
            <div v-if="answer.explanation" class="flex items-start gap-2 p-3 rounded-xl bg-tertiary-fixed/30">
              <span class="material-symbols-outlined text-[18px] mt-0.5 text-on-tertiary-fixed-variant shrink-0">lightbulb</span>
              <div>
                <span class="font-label-sm text-secondary text-xs uppercase tracking-wider">Giải thích:</span>
                <p class="font-body-md text-on-surface">{{ answer.explanation }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="mt-10 flex flex-col sm:flex-row justify-center gap-4">
        <router-link
          :to="`/student/quizzes/${result.quizId}`"
          class="px-6 py-3 rounded-xl font-button transition-all bg-primary text-on-primary hover:opacity-90 shadow-md hover:shadow-lg text-center flex items-center justify-center gap-2"
        >
          <span class="material-symbols-outlined text-[20px]">replay</span>
          Làm lại quiz
        </router-link>
        <router-link
          to="/student/dashboard"
          class="px-6 py-3 rounded-xl font-button transition-all bg-surface-container-high text-secondary hover:bg-surface-container-highest hover:text-ink-black border border-transparent text-center flex items-center justify-center gap-2"
        >
          <span class="material-symbols-outlined text-[20px]">dashboard</span>
          Về Dashboard
        </router-link>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { QuizService } from '@/services/quiz.service'
import { getApiErrorMessage } from '@/utils/api-error'

const route = useRoute()

// States
const isLoading = ref(true)
const errorMsg = ref('')
const result = ref(null)

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

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  try {
    const date = new Date(dateTimeStr)
    return date.toLocaleString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return dateTimeStr
  }
}

const fetchResult = async () => {
  const { quizId, attemptId } = route.params
  isLoading.value = true
  errorMsg.value = ''

  try {
    const res = await QuizService.getQuizResult(quizId, attemptId)
    if (res.data && res.data.code === 1000) {
      result.value = res.data.result
    } else {
      throw new Error(res.data?.message || 'Lỗi lấy kết quả')
    }
  } catch (error) {
    console.error('Result fetch error:', error)
    if (error.response?.status === 404) {
      errorMsg.value = 'Kết quả quiz không tồn tại.'
    } else if (error.response?.status === 403) {
      errorMsg.value = 'Bạn không có quyền xem kết quả này.'
    } else {
      errorMsg.value = getApiErrorMessage(error)
    }
  } finally {
    isLoading.value = false
  }
}

onMounted(fetchResult)
</script>
