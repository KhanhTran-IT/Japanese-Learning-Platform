<template>
  <div class="admin-course-management max-w-[1280px] mx-auto">
    <div class="flex items-center justify-between mb-6">
      <!-- Mute headers if needed, but layout already has page title so we skip repeating it here -->
      <button @click="handleCreateCourse" class="bg-stitch-primary text-white px-5 py-2.5 rounded-xl font-medium hover:bg-stitch-primary/90 transition-colors shadow-lg flex items-center gap-2 ml-auto">
        <span class="text-xl leading-none">+</span> Tạo Khóa Học
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

    <!-- Main Content Area -->
    <div class="bg-[#161b27] border border-white/5 rounded-2xl overflow-hidden">
      <!-- Loading State -->
      <div v-if="isLoading" class="flex flex-col items-center justify-center py-20 text-white/50">
        <span class="material-symbols-outlined animate-spin text-4xl mb-4">autorenew</span>
        <p>Đang tải danh sách khóa học...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="errorMsg" class="flex flex-col items-center justify-center py-20 text-white/50">
        <div class="text-4xl mb-4 text-red-400">⚠️</div>
        <p class="text-red-400/80 mb-6">{{ errorMsg }}</p>
        <button @click="fetchCourses" class="bg-white/10 text-white px-6 py-2.5 rounded-lg hover:bg-white/20 transition-colors font-medium">
          Thử lại
        </button>
      </div>

      <!-- Data Table -->
      <div v-else class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-white/5 border-b border-white/10">
              <th class="p-4 text-xs font-semibold text-white/40 uppercase tracking-wider">Khóa học</th>
              <th class="p-4 text-xs font-semibold text-white/40 uppercase tracking-wider">Giảng viên</th>
              <th class="p-4 text-xs font-semibold text-white/40 uppercase tracking-wider">Cấp độ</th>
              <th class="p-4 text-xs font-semibold text-white/40 uppercase tracking-wider">Loại</th>
              <th class="p-4 text-xs font-semibold text-white/40 uppercase tracking-wider text-center">Học viên</th>
              <th class="p-4 text-xs font-semibold text-white/40 uppercase tracking-wider text-center">Bài học</th>
              <th class="p-4 text-xs font-semibold text-white/40 uppercase tracking-wider">Trạng thái</th>
              <th class="p-4 text-xs font-semibold text-white/40 uppercase tracking-wider text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="courses.length === 0">
              <td colspan="8" class="p-8 text-center text-white/30 italic">Không tìm thấy khóa học nào.</td>
            </tr>
            <tr v-for="course in courses" :key="course.id" class="border-b border-white/5 hover:bg-white/5 transition-colors">
              <td class="p-4">
                <div class="flex flex-col gap-1">
                  <span class="text-sm font-medium text-white/90">{{ course.title }}</span>
                  <span class="text-[11px] text-white/40">Tạo: {{ formatDate(course.createdAt) }}</span>
                </div>
              </td>
              <td class="p-4 text-sm text-white/70 font-medium">{{ course.teacherName || 'Chưa có' }}</td>
              <td class="p-4">
                <span class="text-[10px] px-2 py-0.5 rounded border border-white/20 text-white/70 uppercase font-medium tracking-wider">
                  {{ course.level }}
                </span>
              </td>
              <td class="p-4">
                <span :class="['text-[10px] px-2 py-0.5 rounded-full uppercase tracking-wide font-medium border', course.courseType === 'FREE' ? 'bg-blue-500/10 border-blue-500/20 text-blue-400' : 'bg-pink-500/10 border-pink-500/20 text-pink-400']">
                  {{ course.courseType === 'FREE' ? 'Miễn phí' : 'Trả phí' }}
                </span>
              </td>
              <td class="p-4 text-center text-sm font-bold text-white/90">{{ course.totalStudents }}</td>
              <td class="p-4 text-center text-sm font-bold text-white/90">{{ course.totalLessons }}</td>
              <td class="p-4">
                <span :class="['text-[10px] px-2 py-0.5 rounded-full uppercase tracking-wide font-medium border', getStatusBadgeClass(course.status)]">
                  {{ formatStatus(course.status) }}
                </span>
              </td>
              <td class="p-4 text-right">
                <div class="flex items-center justify-end gap-2">
                  <button 
                    @click="handleStructureCourse(course)" 
                    class="px-2.5 py-1.5 text-xs font-medium rounded border border-white/10 bg-white/5 text-white/90 hover:bg-white/10 transition-colors disabled:opacity-50"
                    :disabled="isProcessingId === course.id"
                  >
                    Cấu trúc
                  </button>
                  <button 
                    @click="handleEditCourse(course)" 
                    class="px-2.5 py-1.5 text-xs font-medium rounded border border-white/10 bg-white/5 text-white/90 hover:bg-white/10 transition-colors disabled:opacity-50"
                    :disabled="isProcessingId === course.id"
                  >
                    Sửa
                  </button>
                  <button 
                    v-if="course.status === 'DRAFT' || course.status === 'HIDDEN'"
                    @click="handlePublish(course)" 
                    class="px-2.5 py-1.5 text-xs font-medium rounded border border-green-500/30 bg-green-500/10 text-green-400 hover:bg-green-500/20 transition-colors disabled:opacity-50"
                    :disabled="isProcessingId === course.id"
                  >
                    Xuất bản
                  </button>
                  <button 
                    v-if="course.status === 'PUBLISHED'"
                    @click="handleHide(course)" 
                    class="px-2.5 py-1.5 text-xs font-medium rounded border border-yellow-500/30 bg-yellow-500/10 text-yellow-400 hover:bg-yellow-500/20 transition-colors disabled:opacity-50"
                    :disabled="isProcessingId === course.id"
                  >
                    Ẩn
                  </button>
                  <button 
                    v-if="course.status !== 'ARCHIVED'"
                    @click="handleDelete(course)" 
                    class="px-2.5 py-1.5 text-xs font-medium rounded border border-red-500/30 bg-red-500/10 text-red-400 hover:bg-red-500/20 transition-colors disabled:opacity-50"
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
    <div v-if="!isLoading && !errorMsg && pagination.totalPages > 0" class="mt-6 flex items-center justify-between">
      <div class="text-sm text-white/50">
        Hiển thị {{ courses.length }} / {{ pagination.totalElements }} khóa học
      </div>
      <div class="flex items-center gap-3">
        <button 
          @click="changePage(pagination.currentPage - 1)" 
          :disabled="pagination.currentPage === 0"
          class="px-4 py-2 text-sm font-medium rounded-lg border border-white/10 bg-[#161b27] text-white hover:bg-white/5 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          &laquo; Trước
        </button>
        
        <span class="text-sm font-medium text-white/90">Trang {{ pagination.currentPage + 1 }} / {{ pagination.totalPages }}</span>
        
        <button 
          @click="changePage(pagination.currentPage + 1)" 
          :disabled="pagination.currentPage >= pagination.totalPages - 1"
          class="px-4 py-2 text-sm font-medium rounded-lg border border-white/10 bg-[#161b27] text-white hover:bg-white/5 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
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
    if (error.response?.status === 403) {
      errorMsg.value = 'Bạn không có quyền truy cập trang này.'
    } else {
      errorMsg.value = getApiErrorMessage(error, 'Không thể tải danh sách khóa học.')
    }
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

const handleEditCourse = (course) => {
  editingCourse.value = { ...course }
  showFormModal.value = true
}

const handleStructureCourse = (course) => {
  router.push(`/admin/courses/${course.id}/structure`)
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
  if (!window.confirm(`Xác nhận XUẤT BẢN khóa học "${course.title}"?`)) return
  
  actionError.value = ''
  isProcessingId.value = course.id
  
  try {
    const res = await AdminService.publishCourse(course.id)
    if (res.data.code === 1000) {
      const index = courses.value.findIndex(c => c.id === course.id)
      if (index !== -1) courses.value[index].status = res.data.result.status
    }
  } catch (error) {
    actionError.value = getApiErrorMessage(error, 'Không thể xuất bản khóa học này.')
  } finally {
    isProcessingId.value = null
  }
}

const handleHide = async (course) => {
  if (!window.confirm(`Bạn có chắc chắn muốn ẨN khóa học "${course.title}"? Khóa học sẽ không hiển thị trên trang chủ nữa.`)) return
  
  actionError.value = ''
  isProcessingId.value = course.id
  
  try {
    const res = await AdminService.hideCourse(course.id)
    if (res.data.code === 1000) {
      const index = courses.value.findIndex(c => c.id === course.id)
      if (index !== -1) courses.value[index].status = res.data.result.status
    }
  } catch (error) {
    actionError.value = getApiErrorMessage(error, 'Không thể ẩn khóa học này.')
  } finally {
    isProcessingId.value = null
  }
}

const handleDelete = async (course) => {
  if (!window.confirm(`CẢNH BÁO: Xóa khóa học "${course.title}"?\nKhóa học sẽ chuyển sang trạng thái Lưu trữ (ARCHIVED).`)) return
  
  actionError.value = ''
  isProcessingId.value = course.id
  
  try {
    const res = await AdminService.deleteCourse(course.id)
    if (res.data.code === 1000) {
      const index = courses.value.findIndex(c => c.id === course.id)
      if (index !== -1) courses.value[index].status = 'ARCHIVED'
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
    case 'PUBLISHED': return 'bg-green-500/10 border-green-500/20 text-green-400'
    case 'HIDDEN': return 'bg-yellow-500/10 border-yellow-500/20 text-yellow-400'
    case 'ARCHIVED': return 'bg-red-500/10 border-red-500/20 text-red-400'
    default: return 'bg-white/5 border-white/10 text-white/50'
  }
}

onMounted(() => {
  fetchCourses()
})
</script>
