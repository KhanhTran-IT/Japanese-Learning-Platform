<template>
  <div class="admin-quiz-management">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Quản lý Bài tập (Quiz)</h1>
        <p class="page-subtitle">Quản lý tất cả các bài tập trên hệ thống.</p>
      </div>
      <div class="header-actions">
        <button class="btn-primary" @click="handleCreateQuiz">
          <span>+</span> Tạo Bài Tập
        </button>
      </div>
    </div>

    <!-- Inline Error -->
    <div v-if="actionError" class="inline-error">
      ⚠️ {{ actionError }}
      <button @click="actionError = ''" class="btn-close-error">✕</button>
    </div>

    <!-- Main Content Area -->
    <div class="content-area">
      <!-- Loading State -->
      <div v-if="isLoading" class="loading-state">
        <div class="spinner"></div>
        <p>Đang tải danh sách bài tập...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="errorMsg" class="error-state">
        <div class="error-icon">⚠️</div>
        <p>{{ errorMsg }}</p>
        <button @click="fetchQuizzes" class="btn-retry">Thử lại</button>
      </div>

      <!-- Data Table -->
      <div v-else class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Tiêu đề</th>
              <th>Thuộc về</th>
              <th>Thời gian (Phút)</th>
              <th>Điểm qua môn</th>
              <th>Trạng thái</th>
              <th class="text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="quizzes.length === 0">
              <td colspan="7" class="empty-state">Không tìm thấy bài tập nào.</td>
            </tr>
            <tr v-for="quiz in quizzes" :key="quiz.id">
              <td>{{ quiz.id }}</td>
              <td>
                <div class="quiz-info">
                  <span class="quiz-title">{{ quiz.title }}</span>
                  <span class="quiz-date text-gray">Ngày tạo: {{ formatDate(quiz.createdAt) }}</span>
                </div>
              </td>
              <td>
                <div v-if="quiz.courseId" class="badge badge-outline">Khóa học: {{ quiz.courseId }}</div>
                <div v-else-if="quiz.lessonId" class="badge badge-outline">Bài học: {{ quiz.lessonId }}</div>
              </td>
              <td class="text-center">
                <span class="stat-number">{{ quiz.timeLimitMinutes || 'Không giới hạn' }}</span>
              </td>
              <td class="text-center">
                <span class="stat-number">{{ quiz.passingScore }}</span>
              </td>
              <td>
                <span :class="['badge', getStatusBadgeClass(quiz.status)]">
                  {{ formatStatus(quiz.status) }}
                </span>
              </td>
              <td class="text-right actions-cell">
                <div class="action-buttons">
                  <button 
                    @click="handleBuilder(quiz)" 
                    class="btn-text btn-structure"
                    :disabled="isProcessingId === quiz.id"
                  >
                    Builder
                  </button>
                  <button 
                    @click="handleEditQuiz(quiz)" 
                    class="btn-text btn-edit"
                    :disabled="isProcessingId === quiz.id"
                  >
                    Sửa
                  </button>
                  <button 
                    v-if="quiz.status === 'DRAFT' || quiz.status === 'HIDDEN'"
                    @click="handlePublish(quiz)" 
                    class="btn-text btn-publish"
                    :disabled="isProcessingId === quiz.id"
                  >
                    Xuất bản
                  </button>
                  <button 
                    v-if="quiz.status === 'PUBLISHED'"
                    @click="handleHide(quiz)" 
                    class="btn-text btn-hide"
                    :disabled="isProcessingId === quiz.id"
                  >
                    Ẩn
                  </button>
                  <button 
                    v-if="quiz.status !== 'ARCHIVED'"
                    @click="handleDelete(quiz)" 
                    class="btn-text btn-delete"
                    :disabled="isProcessingId === quiz.id"
                  >
                    Xóa
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="!isLoading && !errorMsg && pagination.totalPages > 0" class="pagination">
      <div class="pagination-info">
        Hiển thị {{ quizzes.length }} / {{ pagination.totalElements }} bài tập
      </div>
      <div class="pagination-controls">
        <button 
          @click="changePage(pagination.currentPage - 1)" 
          :disabled="pagination.currentPage === 0"
          class="btn-page"
        >
          &laquo; Trước
        </button>
        
        <span class="page-current">Trang {{ pagination.currentPage + 1 }} / {{ pagination.totalPages }}</span>
        
        <button 
          @click="changePage(pagination.currentPage + 1)" 
          :disabled="pagination.currentPage >= pagination.totalPages - 1"
          class="btn-page"
        >
          Sau &raquo;
        </button>
      </div>
    </div>
    
    <!-- Quiz Form Modal -->
    <QuizFormModal
      v-if="showFormModal"
      :editingQuiz="editingQuiz"
      @close="closeFormModal"
      @saved="handleFormSaved"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { AdminService } from '@/services/admin.service'
import { getApiErrorMessage } from '@/utils/api-error'
import { useRouter } from 'vue-router'
import QuizFormModal from '@/components/admin/QuizFormModal.vue'

const router = useRouter()

// State
const quizzes = ref([])
const isLoading = ref(true)
const errorMsg = ref('')
const actionError = ref('')
const isProcessingId = ref(null)

// Modal State
const showFormModal = ref(false)
const editingQuiz = ref(null)

const pagination = reactive({
  currentPage: 0,
  pageSize: 10,
  totalPages: 0,
  totalElements: 0
})

// Methods
const fetchQuizzes = async () => {
  isLoading.value = true
  errorMsg.value = ''
  
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize
    }

    const res = await AdminService.getQuizzes(params)
    if (res.data.code === 1000) {
      quizzes.value = res.data.result.content || []
      pagination.currentPage = res.data.result.number || 0
      pagination.totalPages = res.data.result.totalPages || 0
      pagination.totalElements = res.data.result.totalElements || 0
    }
  } catch (error) {
    if (error.response && error.response.status === 403) {
      errorMsg.value = 'Bạn không có quyền truy cập trang này.'
    } else {
      errorMsg.value = getApiErrorMessage(error, 'Không thể tải danh sách bài tập.')
    }
    console.error('Fetch quizzes error:', error)
  } finally {
    isLoading.value = false
  }
}

const changePage = (newPage) => {
  if (newPage >= 0 && newPage < pagination.totalPages) {
    pagination.currentPage = newPage
    fetchQuizzes()
  }
}

const handleCreateQuiz = () => {
  editingQuiz.value = null
  showFormModal.value = true
}

const handleEditQuiz = (quiz) => {
  editingQuiz.value = { ...quiz }
  showFormModal.value = true
}

const handleBuilder = (quiz) => {
  router.push(`/admin/quizzes/${quiz.id}/builder`)
}

const closeFormModal = () => {
  showFormModal.value = false
  editingQuiz.value = null
}

const handleFormSaved = () => {
  closeFormModal()
  fetchQuizzes()
}

const handlePublish = async (quiz) => {
  if (!window.confirm(`Xác nhận XUẤT BẢN bài tập "${quiz.title}"?`)) {
    return
  }
  
  actionError.value = ''
  isProcessingId.value = quiz.id
  
  try {
    const res = await AdminService.publishQuiz(quiz.id)
    if (res.data.code === 1000) {
      const index = quizzes.value.findIndex(q => q.id === quiz.id)
      if (index !== -1) {
        quizzes.value[index].status = res.data.result.status
      }
    }
  } catch (error) {
    actionError.value = getApiErrorMessage(error, 'Không thể xuất bản bài tập này. Lưu ý bài tập phải có ít nhất 1 câu hỏi.')
  } finally {
    isProcessingId.value = null
  }
}

const handleHide = async (quiz) => {
  if (!window.confirm(`Bạn có chắc chắn muốn ẨN bài tập "${quiz.title}"?`)) {
    return
  }
  
  actionError.value = ''
  isProcessingId.value = quiz.id
  
  try {
    const res = await AdminService.hideQuiz(quiz.id)
    if (res.data.code === 1000) {
      const index = quizzes.value.findIndex(q => q.id === quiz.id)
      if (index !== -1) {
        quizzes.value[index].status = res.data.result.status
      }
    }
  } catch (error) {
    actionError.value = getApiErrorMessage(error, 'Không thể ẩn bài tập này.')
  } finally {
    isProcessingId.value = null
  }
}

const handleDelete = async (quiz) => {
  if (!window.confirm(`CẢNH BÁO: Xóa bài tập "${quiz.title}"?\nBài tập sẽ bị chuyển sang trạng thái ARCHIVED.`)) {
    return
  }
  
  actionError.value = ''
  isProcessingId.value = quiz.id
  
  try {
    const res = await AdminService.deleteQuiz(quiz.id)
    if (res.data.code === 1000) {
      const index = quizzes.value.findIndex(q => q.id === quiz.id)
      if (index !== -1) {
        quizzes.value[index].status = 'ARCHIVED'
      }
    }
  } catch (error) {
    actionError.value = getApiErrorMessage(error, 'Không thể xóa bài tập này.')
  } finally {
    isProcessingId.value = null
  }
}

// Helpers
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('vi-VN', { 
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  }).format(date)
}

const formatStatus = (status) => {
  const statusMap = {
    'DRAFT': 'Bản nháp',
    'PUBLISHED': 'Đã xuất bản',
    'HIDDEN': 'Đang ẩn',
    'ARCHIVED': 'Đã lưu trữ'
  }
  return statusMap[status] || status
}

const getStatusBadgeClass = (status) => {
  switch (status) {
    case 'PUBLISHED': return 'badge-success'
    case 'HIDDEN': return 'badge-warning'
    case 'ARCHIVED': return 'badge-danger'
    default: return 'badge-draft' // DRAFT
  }
}

// Init
onMounted(() => {
  fetchQuizzes()
})
</script>

<style scoped>
.admin-quiz-management {
  max-width: 1280px;
  margin: 0 auto;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
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
}

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
  font-size: 0.95rem;
  cursor: pointer;
  box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.3);
  transition: all 0.2s ease;
}
.btn-primary:hover {
  background-color: #2563eb;
  transform: translateY(-1px);
}
.btn-primary span {
  font-size: 1.2rem;
  font-weight: bold;
}

/* Inline Error */
.inline-error {
  background-color: #fef2f2;
  color: #b91c1c;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-left: 4px solid #ef4444;
}
.btn-close-error {
  background: none;
  border: none;
  color: #b91c1c;
  cursor: pointer;
  font-size: 1.2rem;
}

/* Main Content */
.content-area {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  overflow: hidden;
}

/* States */
.loading-state, .error-state {
  padding: 4rem;
  text-align: center;
  color: #64748b;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f1f5f9;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 1rem;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.error-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}
.btn-retry {
  margin-top: 1rem;
  padding: 0.5rem 1.5rem;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

/* Table */
.table-wrapper {
  overflow-x: auto;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}
.data-table th {
  padding: 1rem;
  background-color: #f8fafc;
  color: #475569;
  font-weight: 600;
  font-size: 0.85rem;
  text-transform: uppercase;
  border-bottom: 1px solid #e2e8f0;
}
.data-table td {
  padding: 1rem;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}
.data-table tbody tr:hover {
  background-color: #f8fafc;
}

.quiz-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.quiz-title {
  font-weight: 600;
  color: #0f172a;
  font-size: 1rem;
}
.quiz-date {
  font-size: 0.8rem;
}

/* Badges */
.badge {
  padding: 0.25rem 0.6rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  white-space: nowrap;
}
.badge-outline {
  border: 1px solid #cbd5e1;
  color: #475569;
  background: white;
}
.badge-success {
  background-color: #dcfce7;
  color: #15803d;
}
.badge-draft {
  background-color: #f1f5f9;
  color: #475569;
}
.badge-warning {
  background-color: #fef3c7;
  color: #b45309;
}
.badge-danger {
  background-color: #fee2e2;
  color: #b91c1c;
}

.stat-number {
  font-weight: 600;
  color: #0f172a;
}

/* Utilities */
.text-gray {
  color: #64748b;
}
.text-right {
  text-align: right;
}
.text-center {
  text-align: center;
}
.empty-state {
  text-align: center;
  padding: 3rem !important;
  color: #64748b;
  font-style: italic;
}

/* Actions */
.actions-cell {
  min-width: 120px;
}
.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}
.btn-text {
  padding: 0.25rem 0.6rem;
  border: 1px solid transparent;
  border-radius: 6px;
  cursor: pointer;
  background: white;
  transition: all 0.2s;
  font-size: 0.85rem;
  font-weight: 500;
}
.btn-text:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-structure {
  border-color: #e2e8f0;
  color: #3b82f6;
}
.btn-structure:hover:not(:disabled) {
  background: #eff6ff;
}
.btn-edit {
  border-color: #cbd5e1;
  color: #334155;
}
.btn-edit:hover:not(:disabled) {
  background: #f1f5f9;
}
.btn-publish {
  border-color: #86efac;
  color: #15803d;
}
.btn-publish:hover:not(:disabled) {
  background: #dcfce7;
}
.btn-hide {
  border-color: #fde047;
  color: #854d0e;
}
.btn-hide:hover:not(:disabled) {
  background: #fef9c3;
}
.btn-delete {
  border-color: #fca5a5;
  color: #b91c1c;
}
.btn-delete:hover:not(:disabled) {
  background: #fef2f2;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1.5rem;
  padding: 0 0.5rem;
}
.pagination-info {
  color: #64748b;
  font-size: 0.9rem;
}
.pagination-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
}
.btn-page {
  padding: 0.5rem 1rem;
  border: 1px solid #cbd5e1;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  color: #0f172a;
}
.btn-page:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f8fafc;
}
.btn-page:hover:not(:disabled) {
  background: #f1f5f9;
}
.page-current {
  font-size: 0.9rem;
  color: #334155;
  font-weight: 500;
}
</style>
