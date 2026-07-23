<template>
  <div class="admin-course-management">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Quản lý Khóa học</h1>
        <p class="page-subtitle">Theo dõi, kiểm duyệt và quản lý các khóa học trên hệ thống.</p>
      </div>
      <div class="header-actions">
        <button class="btn-primary" @click="handleCreateCourse">
          <span>+</span> Tạo Khóa Học
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
        <p>Đang tải danh sách khóa học...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="errorMsg" class="error-state">
        <div class="error-icon">⚠️</div>
        <p>{{ errorMsg }}</p>
        <button @click="fetchCourses" class="btn-retry">Thử lại</button>
      </div>

      <!-- Data Table -->
      <div v-else class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>Khóa học</th>
              <th>Giảng viên</th>
              <th>Cấp độ</th>
              <th>Loại</th>
              <th>Học viên</th>
              <th>Bài học</th>
              <th>Trạng thái</th>
              <th class="text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="courses.length === 0">
              <td colspan="8" class="empty-state">Không tìm thấy khóa học nào.</td>
            </tr>
            <tr v-for="course in courses" :key="course.id">
              <td>
                <div class="course-info">
                  <span class="course-title">{{ course.title }}</span>
                  <span class="course-date text-gray">Ngày tạo: {{ formatDate(course.createdAt) }}</span>
                </div>
              </td>
              <td>
                <div class="teacher-info">
                  <span class="teacher-name">{{ course.teacherName }}</span>
                </div>
              </td>
              <td>
                <span class="badge badge-outline">{{ course.level }}</span>
              </td>
              <td>
                <span :class="['badge', course.courseType === 'FREE' ? 'badge-info' : 'badge-premium']">
                  {{ course.courseType === 'FREE' ? 'Miễn phí' : 'Trả phí' }}
                </span>
              </td>
              <td class="text-center">
                <span class="stat-number">{{ course.totalStudents }}</span>
              </td>
              <td class="text-center">
                <span class="stat-number">{{ course.totalLessons }}</span>
              </td>
              <td>
                <span :class="['badge', getStatusBadgeClass(course.status)]">
                  {{ formatStatus(course.status) }}
                </span>
              </td>
              <td class="text-right actions-cell">
                <div class="action-buttons">
                  <button 
                    @click="handleEditCourse(course)" 
                    class="btn-text btn-edit"
                    :disabled="isProcessingId === course.id"
                  >
                    Sửa
                  </button>
                  <button 
                    v-if="course.status === 'DRAFT' || course.status === 'HIDDEN'"
                    @click="handlePublish(course)" 
                    class="btn-text btn-publish"
                    :disabled="isProcessingId === course.id"
                  >
                    Xuất bản
                  </button>
                  <button 
                    v-if="course.status === 'PUBLISHED'"
                    @click="handleHide(course)" 
                    class="btn-text btn-hide"
                    :disabled="isProcessingId === course.id"
                  >
                    Ẩn
                  </button>
                  <button 
                    v-if="course.status !== 'ARCHIVED'"
                    @click="handleDelete(course)" 
                    class="btn-text btn-delete"
                    :disabled="isProcessingId === course.id"
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
        Hiển thị {{ courses.length }} / {{ pagination.totalElements }} khóa học
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
    <!-- Course Form Modal -->
    <CourseFormModal
      v-if="showFormModal"
      :editingCourse="editingCourse"
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
import CourseFormModal from '@/components/admin/CourseFormModal.vue'

const router = useRouter()

// State
const courses = ref([])
const isLoading = ref(true)
const errorMsg = ref('')
const actionError = ref('')
const isProcessingId = ref(null)

// Modal State
const showFormModal = ref(false)
const editingCourse = ref(null)

const pagination = reactive({
  currentPage: 0,
  pageSize: 10,
  totalPages: 0,
  totalElements: 0
})

// Methods
const fetchCourses = async () => {
  isLoading.value = true
  errorMsg.value = ''
  
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize
    }

    const res = await AdminService.getCourses(params)
    if (res.data.code === 1000) {
      courses.value = res.data.result.content || []
      pagination.currentPage = res.data.result.number || 0
      pagination.totalPages = res.data.result.totalPages || 0
      pagination.totalElements = res.data.result.totalElements || 0
    }
  } catch (error) {
    if (error.response && error.response.status === 403) {
      errorMsg.value = 'Bạn không có quyền truy cập trang này.'
    } else {
      errorMsg.value = getApiErrorMessage(error, 'Không thể tải danh sách khóa học.')
    }
    console.error('Fetch courses error:', error)
  } finally {
    isLoading.value = false
  }
}

const changePage = (newPage) => {
  if (newPage >= 0 && newPage < pagination.totalPages) {
    pagination.currentPage = newPage
    fetchCourses()
  }
}

const handleCreateCourse = () => {
  editingCourse.value = null
  showFormModal.value = true
}

const handleEditCourse = async (course) => {
  // Option: Có thể gọi getCourseDetail(course.id) để lấy dữ liệu mới nhất
  // Ở đây để đơn giản ta dùng data từ row hiện tại
  editingCourse.value = { ...course }
  showFormModal.value = true
}

const closeFormModal = () => {
  showFormModal.value = false
  editingCourse.value = null
}

const handleFormSaved = () => {
  closeFormModal()
  fetchCourses()
}

const handlePublish = async (course) => {
  if (!window.confirm(`Xác nhận XUẤT BẢN khóa học "${course.title}"?`)) {
    return
  }
  
  actionError.value = ''
  isProcessingId.value = course.id
  
  try {
    const res = await AdminService.publishCourse(course.id)
    if (res.data.code === 1000) {
      const index = courses.value.findIndex(c => c.id === course.id)
      if (index !== -1) {
        courses.value[index].status = res.data.result.status
      }
    }
  } catch (error) {
    actionError.value = getApiErrorMessage(error, 'Không thể xuất bản khóa học này.')
  } finally {
    isProcessingId.value = null
  }
}

const handleHide = async (course) => {
  if (!window.confirm(`Bạn có chắc chắn muốn ẨN khóa học "${course.title}"? Khóa học sẽ không hiển thị trên trang chủ nữa.`)) {
    return
  }
  
  actionError.value = ''
  isProcessingId.value = course.id
  
  try {
    const res = await AdminService.hideCourse(course.id)
    if (res.data.code === 1000) {
      const index = courses.value.findIndex(c => c.id === course.id)
      if (index !== -1) {
        courses.value[index].status = res.data.result.status
      }
    }
  } catch (error) {
    actionError.value = getApiErrorMessage(error, 'Không thể ẩn khóa học này.')
  } finally {
    isProcessingId.value = null
  }
}

const handleDelete = async (course) => {
  if (!window.confirm(`CẢNH BÁO: Xóa khóa học "${course.title}"?\nKhóa học sẽ chuyển sang trạng thái Lưu trữ (ARCHIVED).`)) {
    return
  }
  
  actionError.value = ''
  isProcessingId.value = course.id
  
  try {
    const res = await AdminService.deleteCourse(course.id)
    if (res.data.code === 1000) {
      const index = courses.value.findIndex(c => c.id === course.id)
      if (index !== -1) {
        courses.value[index].status = 'ARCHIVED'
      }
    }
  } catch (error) {
    actionError.value = getApiErrorMessage(error, 'Không thể xóa khóa học này.')
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
  fetchCourses()
})
</script>

<style scoped>
.admin-course-management {
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

.course-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.course-title {
  font-weight: 600;
  color: #0f172a;
  font-size: 1rem;
}
.course-date {
  font-size: 0.8rem;
}

.teacher-info {
  font-weight: 500;
  color: #334155;
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
.badge-info {
  background-color: #e0f2fe;
  color: #0369a1;
}
.badge-premium {
  background-color: #fce7f3;
  color: #be185d;
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
