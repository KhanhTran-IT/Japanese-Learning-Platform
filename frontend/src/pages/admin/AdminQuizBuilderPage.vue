<template>
  <div class="quiz-builder">
    <div class="page-header">
      <div class="header-left">
        <button @click="$router.push('/admin/quizzes')" class="btn-back">&larr; Quay lại</button>
        <h1 class="page-title">
          Builder: {{ quiz ? quiz.title : 'Đang tải...' }}
        </h1>
        <p class="page-subtitle" v-if="quiz">
          Tạo và chỉnh sửa câu hỏi cho bài tập này.
          <span :class="['badge', getStatusBadgeClass(quiz.status)]">{{ formatStatus(quiz.status) }}</span>
        </p>
      </div>
      <div class="header-actions" v-if="quiz">
        <button class="btn-primary" @click="handleCreateQuestion">
          <span>+</span> Thêm Câu Hỏi
        </button>
      </div>
    </div>

    <div v-if="actionError" class="inline-error">
      ⚠️ {{ actionError }}
      <button @click="actionError = ''" class="btn-close-error">✕</button>
    </div>

    <!-- Loading / Error States -->
    <div v-if="isLoading" class="loading-state">
      <div class="spinner"></div>
      <p>Đang tải dữ liệu bài tập...</p>
    </div>

    <div v-else-if="errorMsg" class="error-state">
      <div class="error-icon">⚠️</div>
      <p>{{ errorMsg }}</p>
      <button @click="fetchData" class="btn-retry">Thử lại</button>
    </div>

    <!-- Questions List -->
    <div v-else class="questions-container">
      <div v-if="questions.length === 0" class="empty-state">
        <p>Bài tập này chưa có câu hỏi nào.</p>
        <button class="btn-secondary" @click="handleCreateQuestion">Thêm Câu Hỏi Đầu Tiên</button>
      </div>

      <div v-for="(question, qIndex) in questions" :key="question.id" class="question-card">
        <div class="question-header">
          <div class="question-title">
            <span class="question-number">Câu {{ qIndex + 1 }}</span>
            <div class="question-content">
              <strong>{{ question.content }}</strong>
              <div v-if="question.audioUrl" class="media-link">🔊 {{ question.audioUrl }}</div>
              <div v-if="question.imageUrl" class="media-link">🖼️ {{ question.imageUrl }}</div>
              <div class="question-meta">Loại: {{ question.questionType }} | Điểm: {{ question.points }} | Thứ tự: {{ question.sortOrder }}</div>
            </div>
          </div>
          <div class="question-actions">
            <button @click="handleEditQuestion(question)" class="btn-icon" title="Sửa câu hỏi">✏️</button>
            <button @click="handleDeleteQuestion(question.id)" class="btn-icon text-danger" title="Xóa câu hỏi">🗑️</button>
          </div>
        </div>

        <div class="answers-section">
          <div class="answers-header">
            <h4>Đáp án</h4>
            <button @click="handleCreateAnswer(question.id)" class="btn-text btn-add-answer">+ Thêm đáp án</button>
          </div>
          
          <ul class="answers-list">
            <li v-if="!question.answers || question.answers.length === 0" class="empty-answers">
              Chưa có đáp án nào.
            </li>
            <li 
              v-for="(answer, aIndex) in question.answers" 
              :key="answer.id" 
              :class="['answer-item', answer.isCorrect ? 'is-correct' : '']"
            >
              <div class="answer-content">
                <span class="answer-indicator">{{ answer.isCorrect ? '✓' : '○' }}</span>
                <span>{{ answer.content }}</span>
              </div>
              <div class="answer-actions">
                <button @click="handleEditAnswer(question.id, answer)" class="btn-icon-small">✏️</button>
                <button @click="handleDeleteAnswer(question.id, answer.id)" class="btn-icon-small text-danger">🗑️</button>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <!-- Question Modal -->
    <div v-if="showQuestionModal" class="modal-backdrop">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ editingQuestion ? 'Sửa Câu Hỏi' : 'Thêm Câu Hỏi Mới' }}</h2>
          <button @click="closeQuestionModal" class="btn-close">✕</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveQuestion">
            <div class="form-group">
              <label>Nội dung câu hỏi *</label>
              <textarea v-model="questionForm.content" rows="3" required></textarea>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>Loại câu hỏi</label>
                <select v-model="questionForm.questionType">
                  <option value="MULTIPLE_CHOICE">Trắc nghiệm nhiều lựa chọn</option>
                  <option value="SINGLE_CHOICE">Trắc nghiệm một lựa chọn</option>
                  <option value="FILL_IN_BLANK">Điền vào chỗ trống</option>
                </select>
              </div>
              <div class="form-group">
                <label>Điểm *</label>
                <input type="number" v-model.number="questionForm.points" min="0" required />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>Audio URL</label>
                <input type="text" v-model="questionForm.audioUrl" placeholder="https://..." />
              </div>
              <div class="form-group">
                <label>Image URL</label>
                <input type="text" v-model="questionForm.imageUrl" placeholder="https://..." />
              </div>
            </div>
            <div class="form-group">
              <label>Thứ tự hiển thị</label>
              <input type="number" v-model.number="questionForm.sortOrder" min="0" />
            </div>
            
            <div class="form-actions">
              <button type="button" @click="closeQuestionModal" class="btn-cancel">Hủy</button>
              <button type="submit" class="btn-submit" :disabled="isSubmitting">Lưu</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Answer Modal -->
    <div v-if="showAnswerModal" class="modal-backdrop">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ editingAnswer ? 'Sửa Đáp Án' : 'Thêm Đáp Án Mới' }}</h2>
          <button @click="closeAnswerModal" class="btn-close">✕</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveAnswer">
            <div class="form-group">
              <label>Nội dung đáp án *</label>
              <input type="text" v-model="answerForm.content" required />
            </div>
            <div class="form-group">
              <label class="checkbox-label">
                <input type="checkbox" v-model="answerForm.isCorrect" />
                Đây là đáp án ĐÚNG
              </label>
            </div>
            <div class="form-group">
              <label>Giải thích (Tùy chọn)</label>
              <textarea v-model="answerForm.explanation" rows="2"></textarea>
            </div>
            <div class="form-group">
              <label>Thứ tự</label>
              <input type="number" v-model.number="answerForm.sortOrder" min="0" />
            </div>
            
            <div class="form-actions">
              <button type="button" @click="closeAnswerModal" class="btn-cancel">Hủy</button>
              <button type="submit" class="btn-submit" :disabled="isSubmitting">Lưu</button>
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
      // API might just return questions without answers depending on implementation
      // But typically we'd fetch answers per question, or the backend includes them
      // Assuming backend includes answers list inside question DTO
      questions.value = questionsRes.data.result || []
      
      // If backend does NOT include answers, we'd need to fetch them manually for each question
      // Let's assume we do need to fetch them for completeness based on API docs:
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
    case 'PUBLISHED': return 'badge-success'
    case 'HIDDEN': return 'badge-warning'
    case 'ARCHIVED': return 'badge-danger'
    default: return 'badge-draft'
  }
}
</script>

<style scoped>
.quiz-builder {
  max-width: 1000px;
  margin: 0 auto;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
}
.btn-back {
  background: none;
  border: none;
  color: #3b82f6;
  font-weight: 500;
  cursor: pointer;
  padding: 0;
  margin-bottom: 0.5rem;
}
.btn-back:hover {
  text-decoration: underline;
}
.page-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 0.25rem;
}
.page-subtitle {
  color: #64748b;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* Badges */
.badge {
  padding: 0.2rem 0.5rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  white-space: nowrap;
}
.badge-success { background-color: #dcfce7; color: #15803d; }
.badge-draft { background-color: #f1f5f9; color: #475569; }
.badge-warning { background-color: #fef3c7; color: #b45309; }
.badge-danger { background-color: #fee2e2; color: #b91c1c; }

/* Buttons */
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.3);
}
.btn-primary:hover { background-color: #2563eb; }
.btn-secondary {
  padding: 0.6rem 1rem;
  background-color: #f1f5f9;
  color: #334155;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
}
.btn-secondary:hover { background-color: #e2e8f0; }

.btn-icon {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.2rem;
  padding: 0.25rem;
  border-radius: 4px;
}
.btn-icon:hover { background: #f1f5f9; }
.btn-icon-small {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  padding: 0.2rem;
  opacity: 0.5;
}
.btn-icon-small:hover { opacity: 1; }

.text-danger { color: #ef4444; }
.text-danger:hover { background: #fef2f2 !important; }

/* Content */
.inline-error {
  background-color: #fef2f2;
  color: #b91c1c;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  display: flex;
  justify-content: space-between;
  border-left: 4px solid #ef4444;
}
.btn-close-error { background: none; border: none; color: #b91c1c; cursor: pointer; }

.loading-state, .error-state, .empty-state {
  text-align: center;
  padding: 4rem;
  color: #64748b;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
.spinner {
  width: 40px; height: 40px;
  border: 4px solid #f1f5f9; border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 1rem;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Questions */
.questions-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
.question-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  overflow: hidden;
  border: 1px solid #e2e8f0;
}
.question-header {
  padding: 1.25rem 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid #f1f5f9;
  background-color: #f8fafc;
}
.question-title {
  display: flex;
  gap: 1rem;
}
.question-number {
  font-weight: 700;
  color: #3b82f6;
  white-space: nowrap;
}
.question-content {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.question-meta {
  font-size: 0.8rem;
  color: #64748b;
}
.media-link {
  font-size: 0.85rem;
  color: #475569;
  background: #f1f5f9;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  display: inline-block;
}

/* Answers */
.answers-section {
  padding: 1rem 1.5rem;
}
.answers-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}
.answers-header h4 {
  font-size: 0.95rem;
  color: #475569;
  margin: 0;
}
.btn-add-answer {
  background: none; border: none; color: #3b82f6; cursor: pointer; font-size: 0.85rem;
}
.btn-add-answer:hover { text-decoration: underline; }
.answers-list {
  list-style: none; padding: 0; margin: 0;
  display: flex; flex-direction: column; gap: 0.5rem;
}
.answer-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 0.75rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
}
.answer-item.is-correct {
  background: #f0fdf4;
  border-color: #bbf7d0;
}
.answer-content { display: flex; align-items: center; gap: 0.75rem; font-size: 0.95rem; color: #334155;}
.answer-indicator { font-weight: bold; color: #94a3b8; }
.is-correct .answer-indicator { color: #22c55e; }
.is-correct .answer-content { color: #166534; font-weight: 500; }
.empty-answers { font-size: 0.9rem; color: #94a3b8; font-style: italic; }

/* Modals */
.modal-backdrop {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(15,23,42,0.6);
  display: flex; justify-content: center; align-items: center; z-index: 1000;
}
.modal-content {
  background: white; width: 100%; max-width: 600px;
  border-radius: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); overflow: hidden;
}
.modal-header { padding: 1.5rem; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; }
.modal-body { padding: 1.5rem; }
.form-group { margin-bottom: 1.25rem; }
.form-row { display: flex; gap: 1rem; }
.form-row .form-group { flex: 1; }
label { display: block; font-size: 0.85rem; font-weight: 600; color: #475569; margin-bottom: 0.5rem; }
input, textarea, select { width: 100%; padding: 0.75rem; border: 1px solid #cbd5e1; border-radius: 6px; }
.checkbox-label { display: flex; align-items: center; gap: 0.5rem; cursor: pointer; }
.checkbox-label input { width: auto; }
.form-actions { display: flex; justify-content: flex-end; gap: 1rem; margin-top: 1.5rem; }
.btn-cancel { padding: 0.75rem 1.25rem; background: white; border: 1px solid #cbd5e1; border-radius: 6px; cursor: pointer; }
.btn-submit { padding: 0.75rem 1.25rem; background: #3b82f6; color: white; border: none; border-radius: 6px; cursor: pointer; }
</style>
