<template>
  <div class="min-h-screen bg-stitch-background flex flex-col font-stitch-sans text-stitch-foreground">
    <!-- Header -->
    <header class="h-16 bg-white border-b border-stitch-border flex items-center justify-between px-6 sticky top-0 z-10 shrink-0">
      <router-link to="/student/dashboard" class="inline-flex items-center gap-2 text-stitch-muted-foreground hover:text-stitch-foreground transition-colors text-sm font-medium">
        ← Quay lại Dashboard
      </router-link>
    </header>

    <!-- Loading State -->
    <div v-if="isLoading" class="flex-1 flex flex-col items-center justify-center py-20 text-stitch-muted-foreground">
      <span class="material-symbols-outlined animate-spin text-4xl mb-4">autorenew</span>
      <p>Đang tải kết quả...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="errorMsg" class="flex-1 flex flex-col items-center justify-center py-20 text-center max-w-lg mx-auto px-4">
      <span class="material-symbols-outlined text-5xl mb-4 text-red-500">error</span>
      <h2 class="text-2xl font-bold mb-2">Không thể tải kết quả</h2>
      <p class="text-stitch-muted-foreground mb-6">{{ errorMsg }}</p>
      <router-link to="/student/dashboard" class="bg-stitch-primary text-white px-6 py-3 rounded-xl font-semibold hover:bg-stitch-primary/90 transition-all shadow-md">
        Về Dashboard
      </router-link>
    </div>

    <!-- Result Content -->
    <main v-else-if="result" class="flex-1 max-w-3xl mx-auto w-full px-4 py-8 md:py-12 pb-24">
      <!-- Score header -->
      <div class="text-center mb-8">
        <div class="text-6xl mb-4 animate-bounce">{{ result.passed ? '🎉' : '😅' }}</div>
        <h1 class="text-3xl md:text-4xl font-stitch-serif font-bold mb-2">{{ result.quizTitle }}</h1>
        <div 
          class="text-2xl font-bold mb-2"
          :class="result.passed ? 'text-green-500' : 'text-red-500'"
        >
          {{ result.passed ? 'Tuyệt vời!' : 'Cần cố gắng thêm' }}
        </div>
        <div class="text-stitch-muted-foreground font-medium">Bạn trả lời đúng {{ result.correctCount }}/{{ result.totalQuestions }} câu</div>
      </div>

      <!-- Score display -->
      <div class="flex justify-center mb-10">
        <div class="relative w-48 h-48">
          <svg class="w-full h-full -rotate-90 drop-shadow-sm" viewBox="0 0 100 100">
            <circle cx="50" cy="50" r="42" fill="none" stroke="#f1f5f9" stroke-width="12" />
            <circle
              cx="50" cy="50" r="42" fill="none"
              :stroke="result.passed ? '#22c55e' : '#ef4444'"
              stroke-width="12"
              :stroke-dasharray="`${(result.score / Math.max(result.score, result.passingScore, 100)) * 263.9} 263.9`"
              stroke-linecap="round"
              class="transition-all duration-1000 ease-out"
            />
          </svg>
          <div class="absolute inset-0 flex flex-col items-center justify-center">
            <div class="text-4xl font-stitch-serif font-bold text-stitch-foreground" :class="result.passed ? 'text-green-600' : 'text-red-600'">{{ result.score }}</div>
            <div class="text-xs text-stitch-muted-foreground font-medium uppercase tracking-wider mt-1">Điểm số</div>
            <div class="text-[10px] text-stitch-muted-foreground mt-1">/ {{ result.passingScore }} điểm đạt</div>
          </div>
        </div>
      </div>

      <!-- Quick Stats -->
      <div class="flex flex-wrap justify-center gap-4 text-sm text-stitch-muted-foreground font-medium mb-12">
        <span v-if="result.startedAt" class="flex items-center gap-1.5 bg-white px-3 py-1.5 rounded-lg border border-stitch-border">
          <span class="material-symbols-outlined text-[16px]">schedule</span>
          Bắt đầu: {{ formatDateTime(result.startedAt) }}
        </span>
        <span v-if="result.submittedAt" class="flex items-center gap-1.5 bg-white px-3 py-1.5 rounded-lg border border-stitch-border">
          <span class="material-symbols-outlined text-[16px]">send</span>
          Nộp bài: {{ formatDateTime(result.submittedAt) }}
        </span>
      </div>

      <!-- Question review -->
      <div class="space-y-5 mb-10">
        <h3 class="font-stitch-serif font-bold text-xl mb-4">Xem lại chi tiết từng câu</h3>
        
        <div
          v-for="(answer, i) in result.answers"
          :key="answer.questionId"
          class="rounded-[24px] border-2 p-6 transition-all"
          :class="answer.isCorrect ? 'bg-green-50/50 border-green-200' : 'bg-red-50/50 border-red-200'"
        >
          <div class="flex items-start gap-4 mb-4">
            <span class="text-2xl shrink-0 mt-1">{{ answer.isCorrect ? '✅' : '❌' }}</span>
            <div class="flex-1 min-w-0">
              <div class="font-bold text-lg mb-2 leading-snug">{{ answer.questionContent }}</div>
              <div class="text-sm">
                <span class="text-stitch-muted-foreground">Đáp án của bạn: </span>
                <strong :class="answer.isCorrect ? 'text-green-600' : 'text-red-500'">
                  {{ answer.selectedAnswerContent || answer.userAnswerText || 'Không trả lời' }}
                </strong>
                
                <template v-if="!answer.isCorrect && answer.correctAnswerContent">
                  <br class="sm:hidden" />
                  <span class="hidden sm:inline text-stitch-muted-foreground mx-2">•</span>
                  <span class="text-stitch-muted-foreground">Đáp án đúng: </span>
                  <strong class="text-green-600">{{ answer.correctAnswerContent }}</strong>
                </template>
              </div>
            </div>
            <div class="shrink-0 text-right">
              <div class="text-sm font-bold" :class="answer.isCorrect ? 'text-green-600' : 'text-red-500'">
                {{ answer.isCorrect ? `+${answer.pointsEarned}` : '0' }} điểm
              </div>
              <div class="text-[10px] text-stitch-muted-foreground mt-1 uppercase tracking-wider">{{ questionTypeLabel(answer.questionType) }}</div>
            </div>
          </div>
          
          <div v-if="answer.explanation" class="text-sm p-4 rounded-xl font-medium leading-relaxed" :class="answer.isCorrect ? 'bg-green-100/50 text-green-800' : 'bg-red-100/50 text-red-800'">
            💡 {{ answer.explanation }}
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="flex flex-col sm:flex-row gap-4">
        <router-link
          :to="`/student/quizzes/${result.quizId}`"
          class="flex-1 py-4 border-2 border-stitch-border bg-white rounded-2xl text-center font-bold hover:bg-stitch-muted transition-colors text-stitch-foreground"
        >
          Làm lại bài kiểm tra
        </router-link>
        <router-link
          to="/student/dashboard"
          class="flex-1 py-4 bg-stitch-primary text-white rounded-2xl text-center font-bold hover:bg-stitch-primary/90 transition-colors shadow-lg"
        >
          Về Dashboard học tập →
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

const QUESTION_TYPE_LABELS = {
  SINGLE_CHOICE: 'Trắc nghiệm',
  TRUE_FALSE: 'Đúng/Sai',
  MULTIPLE_CHOICE: 'Chọn nhiều',
  FILL_BLANK: 'Điền từ'
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
    if (error.response?.status === 404) {
      errorMsg.value = 'Kết quả bài kiểm tra không tồn tại.'
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
