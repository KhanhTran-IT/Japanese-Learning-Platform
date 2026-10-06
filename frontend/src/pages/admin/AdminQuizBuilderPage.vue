<template>
  <div class="admin-quiz-builder max-w-[1000px] mx-auto pb-12">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-4">
        <button @click="$router.push('/admin/quizzes')" class="w-10 h-10 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center transition-colors text-white/70 hover:text-white">
          <span class="material-symbols-outlined">arrow_back</span>
        </button>
        <div>
          <h1 class="text-xl font-stitch-serif font-bold text-white mb-1">
            Builder: <span class="text-stitch-primary">{{ quiz ? quiz.title : 'Đang tải...' }}</span>
          </h1>
          <div class="flex items-center gap-2">
            <p class="text-sm text-white/40">Tạo và chỉnh sửa câu hỏi cho bài tập này.</p>
            <span v-if="quiz" :class="['text-[9px] px-1.5 py-0.5 rounded-full uppercase tracking-wider font-medium border', getStatusBadgeClass(quiz.status)]">
              {{ formatStatus(quiz.status) }}
            </span>
          </div>
        </div>
      </div>
      
      <button v-if="quiz" @click="handleCreateQuestion" class="bg-stitch-primary text-white px-5 py-2.5 rounded-xl font-medium hover:bg-stitch-primary/90 transition-colors shadow-lg flex items-center gap-2">
        <span class="text-xl leading-none">+</span> Thêm Câu Hỏi
      </button>
    </div>

    <!-- Inline Error -->
    <div v-if="actionError" class="bg-red-500/10 border border-red-500/20 rounded-xl p-4 mb-6 flex items-center justify-between">
      <div class="flex items-center gap-3 text-red-400 font-medium">
        <span>⚠️</span>
        {{ actionError }}
      </div>
      <button @click="actionError = ''" class="text-red-400 hover:text-red-300 text-lg">✕</button>
    </div>

    <!-- Loading / Error States -->
    <div v-if="isLoading" class="bg-[#161b27] border border-white/5 rounded-2xl flex flex-col items-center justify-center py-20 text-white/50">
      <span class="material-symbols-outlined animate-spin text-4xl mb-4">autorenew</span>
      <p>Đang tải dữ liệu bài tập...</p>
    </div>

    <div v-else-if="errorMsg" class="bg-[#161b27] border border-white/5 rounded-2xl flex flex-col items-center justify-center py-20 text-white/50">
      <div class="text-4xl mb-4 text-red-400">⚠️</div>
      <p class="text-red-400/80 mb-6">{{ errorMsg }}</p>
      <button @click="fetchData" class="bg-white/10 text-white px-6 py-2.5 rounded-lg hover:bg-white/20 transition-colors font-medium">Thử lại</button>
    </div>

    <!-- Questions List -->
    <div v-else class="flex flex-col gap-6">
      <div v-if="questions.length === 0" class="bg-[#161b27] border border-white/5 rounded-2xl flex flex-col items-center justify-center py-20 text-white/50">
        <span class="text-4xl mb-4">📝</span>
        <p class="mb-4">Bài tập này chưa có câu hỏi nào.</p>
        <button @click="handleCreateQuestion" class="px-5 py-2 rounded-lg border border-white/20 bg-white/5 text-white/80 hover:bg-white/10 hover:text-white transition-colors">Thêm Câu Hỏi Đầu Tiên</button>
      </div>

      <div v-for="(question, qIndex) in questions" :key="question.id" class="bg-[#161b27] border border-white/5 rounded-2xl overflow-hidden shadow-lg">
        <div class="p-5 border-b border-white/5 bg-white/[0.02] flex items-start justify-between group">
          <div class="flex gap-4">
            <div class="w-10 h-10 rounded-full bg-stitch-primary/10 border border-stitch-primary/20 flex items-center justify-center text-stitch-primary font-bold shrink-0">
              {{ qIndex + 1 }}
            </div>
            <div class="flex flex-col gap-2">
              <strong class="text-white/90 text-base leading-relaxed">{{ question.content }}</strong>
              <div class="flex flex-wrap gap-2 mt-1">
                <a v-if="question.audioUrl" :href="question.audioUrl" target="_blank" class="inline-flex items-center gap-1 px-2 py-1 rounded bg-white/5 text-xs text-white/60 hover:text-white hover:bg-white/10 transition-colors">
                  <span>🔊</span> Audio đính kèm
                </a>
                <a v-if="question.imageUrl" :href="question.imageUrl" target="_blank" class="inline-flex items-center gap-1 px-2 py-1 rounded bg-white/5 text-xs text-white/60 hover:text-white hover:bg-white/10 transition-colors">
                  <span>🖼️</span> Ảnh đính kèm
                </a>
              </div>
              <div class="flex items-center gap-3 mt-1 text-xs text-white/40 font-medium uppercase tracking-wider">
                <span class="px-2 py-0.5 rounded bg-white/5">{{ question.questionType }}</span>
                <span>•</span>
                <span>Điểm: <strong class="text-white/70">{{ question.points }}</strong></span>
                <span>•</span>
                <span>Thứ tự: <strong class="text-white/70">{{ question.sortOrder }}</strong></span>
              </div>
            </div>
          </div>
          
          <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
            <button @click="handleEditQuestion(question)" class="w-8 h-8 rounded hover:bg-white/10 text-white/50 hover:text-white flex items-center justify-center transition-colors" title="Sửa câu hỏi">
              <span class="material-symbols-outlined text-[18px]">edit</span>
            </button>
            <button @click="handleDeleteQuestion(question.id)" class="w-8 h-8 rounded hover:bg-red-500/10 text-red-400/50 hover:text-red-400 flex items-center justify-center transition-colors" title="Xóa câu hỏi">
              <span class="material-symbols-outlined text-[18px]">delete</span>
            </button>
          </div>
        </div>

        <div class="p-5 bg-[#161b27]">
          <div class="flex items-center justify-between mb-4">
            <h4 class="text-sm font-semibold text-white/70 uppercase tracking-wider">Đáp án</h4>
            <button @click="handleCreateAnswer(question.id)" class="text-xs font-medium text-stitch-primary hover:text-stitch-primary/80 transition-colors flex items-center gap-1">
              <span>+</span> Thêm đáp án
            </button>
          </div>
          
          <ul class="flex flex-col gap-2">
            <li v-if="!question.answers || question.answers.length === 0" class="py-3 text-center text-xs text-white/30 italic bg-white/[0.01] rounded border border-white/5 border-dashed">
              Chưa có đáp án nào.
            </li>
            <li 
              v-for="(answer, aIndex) in question.answers" 
              :key="answer.id" 
              :class="['flex items-center justify-between p-3 rounded-lg border transition-colors group', answer.isCorrect ? 'bg-green-500/5 border-green-500/20' : 'bg-white/[0.02] border-white/5']"
            >
              <div class="flex items-start gap-3 flex-1 min-w-0">
                <div :class="['w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0 mt-0.5', answer.isCorrect ? 'border-green-500 text-green-500' : 'border-white/20']">
                  <span v-if="answer.isCorrect" class="material-symbols-outlined text-[14px] font-bold">check</span>
                </div>
                <div class="flex flex-col min-w-0">
                  <span :class="['text-sm font-medium', answer.isCorrect ? 'text-green-400' : 'text-white/80']">{{ answer.content }}</span>
                  <span v-if="answer.explanation" class="text-[11px] text-white/40 mt-0.5 truncate">{{ answer.explanation }}</span>
                </div>
              </div>
              <div class="flex items-center gap-1 ml-4 opacity-0 group-hover:opacity-100 transition-opacity">
                <button @click="handleEditAnswer(question.id, answer)" class="w-7 h-7 rounded hover:bg-white/10 text-white/40 hover:text-white flex items-center justify-center transition-colors">
                  <span class="material-symbols-outlined text-[16px]">edit</span>
                </button>
                <button @click="handleDeleteAnswer(question.id, answer.id)" class="w-7 h-7 rounded hover:bg-red-500/10 text-red-400/40 hover:text-red-400 flex items-center justify-center transition-colors">
                  <span class="material-symbols-outlined text-[16px]">delete</span>
                </button>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Question Modal -->
    <div v-if="showQuestionModal" class="fixed inset-0 bg-[#0f1117]/80 backdrop-blur-sm flex justify-center items-center z-50 p-4">
      <div class="bg-[#161b27] w-full max-w-2xl rounded-2xl border border-white/10 shadow-2xl overflow-hidden flex flex-col max-h-full">
        <div class="p-6 border-b border-white/10 flex justify-between items-center bg-white/[0.02]">
          <h2 class="text-xl font-stitch-serif font-bold text-white">{{ editingQuestion ? 'Sửa Câu Hỏi' : 'Thêm Câu Hỏi Mới' }}</h2>
          <button @click="closeQuestionModal" class="w-8 h-8 rounded-full hover:bg-white/10 flex items-center justify-center text-white/50 hover:text-white transition-colors">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        
        <div class="p-6 overflow-y-auto">
          <form @submit.prevent="saveQuestion" class="flex flex-col gap-5">
            <div>
              <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Nội dung câu hỏi *</label>
              <textarea v-model="questionForm.content" rows="3" required class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-sm text-white placeholder-white/20 focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors resize-none"></textarea>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Loại câu hỏi</label>
                <select v-model="questionForm.questionType" class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors">
                  <option value="MULTIPLE_CHOICE" class="bg-[#161b27]">Trắc nghiệm nhiều lựa chọn</option>
                  <option value="SINGLE_CHOICE" class="bg-[#161b27]">Trắc nghiệm một lựa chọn</option>
                  <option value="FILL_IN_BLANK" class="bg-[#161b27]">Điền vào chỗ trống</option>
                </select>
              </div>
              <div>
                <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Điểm *</label>
                <input type="number" v-model.number="questionForm.points" min="0" required class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors" />
              </div>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Audio URL</label>
                <input type="text" v-model="questionForm.audioUrl" placeholder="https://..." class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white placeholder-white/20 focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors" />
              </div>
              <div>
                <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Image URL</label>
                <input type="text" v-model="questionForm.imageUrl" placeholder="https://..." class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white placeholder-white/20 focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors" />
              </div>
            </div>
            
            <div>
              <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Thứ tự hiển thị</label>
              <input type="number" v-model.number="questionForm.sortOrder" min="0" class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors" />
            </div>
            
            <div class="pt-4 border-t border-white/10 flex justify-end gap-3">
              <button type="button" @click="closeQuestionModal" class="px-5 py-2.5 rounded-xl border border-white/10 bg-transparent text-white/80 hover:bg-white/5 transition-colors font-medium text-sm">Hủy</button>
              <button type="submit" :disabled="isSubmitting" class="px-6 py-2.5 rounded-xl bg-stitch-primary text-white font-medium text-sm hover:bg-stitch-primary/90 transition-colors shadow-lg disabled:opacity-50">
                {{ isSubmitting ? 'Đang lưu...' : 'Lưu câu hỏi' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Answer Modal -->
    <div v-if="showAnswerModal" class="fixed inset-0 bg-[#0f1117]/80 backdrop-blur-sm flex justify-center items-center z-50 p-4">
      <div class="bg-[#161b27] w-full max-w-lg rounded-2xl border border-white/10 shadow-2xl overflow-hidden flex flex-col max-h-full">
        <div class="p-6 border-b border-white/10 flex justify-between items-center bg-white/[0.02]">
          <h2 class="text-xl font-stitch-serif font-bold text-white">{{ editingAnswer ? 'Sửa Đáp Án' : 'Thêm Đáp Án Mới' }}</h2>
          <button @click="closeAnswerModal" class="w-8 h-8 rounded-full hover:bg-white/10 flex items-center justify-center text-white/50 hover:text-white transition-colors">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        
        <div class="p-6 overflow-y-auto">
          <form @submit.prevent="saveAnswer" class="flex flex-col gap-5">
            <div>
              <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Nội dung đáp án *</label>
              <input type="text" v-model="answerForm.content" required class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-sm text-white placeholder-white/20 focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors" />
            </div>
            
            <label class="flex items-center gap-3 p-4 rounded-xl border border-white/10 bg-white/[0.02] cursor-pointer hover:bg-white/[0.04] transition-colors">
              <input type="checkbox" v-model="answerForm.isCorrect" class="w-4 h-4 rounded border-white/20 text-stitch-primary focus:ring-stitch-primary focus:ring-offset-[#161b27]" />
              <span class="text-sm font-medium text-white/90">Đây là đáp án ĐÚNG</span>
            </label>
            
            <div>
              <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Giải thích (Tùy chọn)</label>
              <textarea v-model="answerForm.explanation" rows="2" class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-sm text-white placeholder-white/20 focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors resize-none"></textarea>
            </div>
            
            <div>
              <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Thứ tự</label>
              <input type="number" v-model.number="answerForm.sortOrder" min="0" class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors" />
            </div>
            
            <div class="pt-4 border-t border-white/10 flex justify-end gap-3">
              <button type="button" @click="closeAnswerModal" class="px-5 py-2.5 rounded-xl border border-white/10 bg-transparent text-white/80 hover:bg-white/5 transition-colors font-medium text-sm">Hủy</button>
              <button type="submit" :disabled="isSubmitting" class="px-6 py-2.5 rounded-xl bg-stitch-primary text-white font-medium text-sm hover:bg-stitch-primary/90 transition-colors shadow-lg disabled:opacity-50">
                {{ isSubmitting ? 'Đang lưu...' : 'Lưu đáp án' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { AdminService } from '@/services/admin.service'
import { getApiErrorMessage } from '@/utils/api-error'

const props = defineProps({
  id: {
    type: [String, Number],
    required: true
  }
})

const quiz = ref(null)
const questions = ref([])
const isLoading = ref(true)
const errorMsg = ref('')
const actionError = ref('')
const isSubmitting = ref(false)

// Modals state
const showQuestionModal = ref(false)
const editingQuestion = ref(null)
const questionForm = reactive({
  content: '',
  questionType: 'SINGLE_CHOICE',
  points: 10,
  sortOrder: 0,
  audioUrl: '',
  imageUrl: ''
})

const showAnswerModal = ref(false)
const editingAnswer = ref(null)
const targetQuestionId = ref(null)
const answerForm = reactive({
  content: '',
  isCorrect: false,
  explanation: '',
  sortOrder: 0
})

const fetchData = async () => {
  isLoading.value = true
  errorMsg.value = ''
  
  try {
    const quizRes = await AdminService.getQuizDetail(props.id)
    if (quizRes.data.code === 1000) {
      quiz.value = quizRes.data.result
    }
    
    await fetchQuestions()
  } catch (error) {
    errorMsg.value = getApiErrorMessage(error, 'Không thể tải dữ liệu bài tập.')
  } finally {
    isLoading.value = false
  }
}

const fetchQuestions = async () => {
  try {
    const questionsRes = await AdminService.getQuestionsByQuiz(props.id)
    if (questionsRes.data.code === 1000) {
      questions.value = questionsRes.data.result || []
      
      for (let q of questions.value) {
        const ansRes = await AdminService.getAnswersByQuestion(q.id)
        if (ansRes.data.code === 1000) {
          q.answers = ansRes.data.result || []
        }
      }
    }
  } catch (error) {
    console.error("Error fetching questions", error)
    throw error
  }
}

onMounted(() => {
  fetchData()
})

// Question Actions
const handleCreateQuestion = () => {
  editingQuestion.value = null
  questionForm.content = ''
  questionForm.questionType = 'SINGLE_CHOICE'
  questionForm.points = 10
  questionForm.sortOrder = questions.value.length * 10
  questionForm.audioUrl = ''
  questionForm.imageUrl = ''
  showQuestionModal.value = true
}

const handleEditQuestion = (question) => {
  editingQuestion.value = question
  questionForm.content = question.content
  questionForm.questionType = question.questionType
  questionForm.points = question.points
  questionForm.sortOrder = question.sortOrder
  questionForm.audioUrl = question.audioUrl || ''
  questionForm.imageUrl = question.imageUrl || ''
  showQuestionModal.value = true
}

const closeQuestionModal = () => {
  showQuestionModal.value = false
  editingQuestion.value = null
}

const saveQuestion = async () => {
  isSubmitting.value = true
  actionError.value = ''
  try {
    if (editingQuestion.value) {
      await AdminService.updateQuestion(editingQuestion.value.id, questionForm)
    } else {
      await AdminService.createQuestion(props.id, questionForm)
    }
    closeQuestionModal()
    await fetchQuestions()
  } catch (error) {
    actionError.value = getApiErrorMessage(error, 'Không thể lưu câu hỏi.')
  } finally {
    isSubmitting.value = false
  }
}

const handleDeleteQuestion = async (id) => {
  if (!window.confirm('Xác nhận xóa câu hỏi này cùng toàn bộ đáp án?')) return
  
  actionError.value = ''
  try {
    await AdminService.deleteQuestion(id)
    await fetchQuestions()
  } catch (error) {
    actionError.value = getApiErrorMessage(error, 'Không thể xóa câu hỏi.')
  }
}

// Answer Actions
const handleCreateAnswer = (questionId) => {
  targetQuestionId.value = questionId
  editingAnswer.value = null
  
  const q = questions.value.find(x => x.id === questionId)
  const currentCount = q?.answers?.length || 0
  
  answerForm.content = ''
  answerForm.isCorrect = false
  answerForm.explanation = ''
  answerForm.sortOrder = currentCount * 10
  showAnswerModal.value = true
}

const handleEditAnswer = (questionId, answer) => {
  targetQuestionId.value = questionId
  editingAnswer.value = answer
  answerForm.content = answer.content
  answerForm.isCorrect = answer.isCorrect
  answerForm.explanation = answer.explanation || ''
  answerForm.sortOrder = answer.sortOrder
  showAnswerModal.value = true
}

const closeAnswerModal = () => {
  showAnswerModal.value = false
  editingAnswer.value = null
  targetQuestionId.value = null
}

const saveAnswer = async () => {
  isSubmitting.value = true
  actionError.value = ''
  try {
    if (editingAnswer.value) {
      await AdminService.updateAnswer(editingAnswer.value.id, answerForm)
    } else {
      await AdminService.createAnswer(targetQuestionId.value, answerForm)
    }
    closeAnswerModal()
    await fetchQuestions() // Refresh everything
  } catch (error) {
    actionError.value = getApiErrorMessage(error, 'Không thể lưu đáp án.')
  } finally {
    isSubmitting.value = false
  }
}

const handleDeleteAnswer = async (questionId, answerId) => {
  if (!window.confirm('Xóa đáp án này?')) return
  
  actionError.value = ''
  try {
    await AdminService.deleteAnswer(answerId)
    await fetchQuestions()
  } catch (error) {
    actionError.value = getApiErrorMessage(error, 'Không thể xóa đáp án.')
  }
}

const formatStatus = (status) => {
  const map = {
    'DRAFT': 'Bản nháp',
    'PUBLISHED': 'Đã xuất bản',
    'HIDDEN': 'Đang ẩn',
    'ARCHIVED': 'Đã lưu trữ'
  }
  return map[status] || status
}

const getStatusBadgeClass = (status) => {
  switch (status) {
    case 'PUBLISHED': return 'bg-green-500/10 border-green-500/20 text-green-400'
    case 'HIDDEN': return 'bg-yellow-500/10 border-yellow-500/20 text-yellow-400'
    case 'ARCHIVED': return 'bg-red-500/10 border-red-500/20 text-red-400'
    default: return 'bg-white/5 border-white/10 text-white/50'
  }
}
</script>
