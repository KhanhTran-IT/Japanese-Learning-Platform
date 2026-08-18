<template>
  <div class="admin-user-management">
    <div class="page-header">
      <h1 class="page-title">Quản lý Người dùng</h1>
      <p class="page-subtitle">Theo dõi và quản lý tài khoản người dùng trên hệ thống.</p>
    </div>

    <!-- Filters Section -->
    <div class="filters-section">
      <div class="search-box">
        <input 
          type="text" 
          v-model="filters.keyword" 
          placeholder="Tìm theo tên hoặc email..." 
          @keyup.enter="handleFilterChange"
        />
        <button class="btn-search" @click="handleFilterChange">🔍</button>
      </div>
      
      <div class="filter-group">
        <select v-model="filters.role" @change="handleFilterChange" class="filter-select">
          <option value="">Tất cả Vai trò</option>
          <option value="STUDENT">Học viên</option>
          <option value="TEACHER">Giáo viên</option>
          <option value="CONTENT_EDITOR">Biên tập viên</option>
          <option value="ADMIN">Quản trị viên</option>
          <option value="SUPER_ADMIN">Super Admin</option>
        </select>
        
        <select v-model="filters.status" @change="handleFilterChange" class="filter-select">
          <option value="">Tất cả Trạng thái</option>
          <option value="ACTIVE">Hoạt động (Active)</option>
          <option value="LOCKED">Bị khóa (Locked)</option>
          <option value="INACTIVE">Ngừng hoạt động (Inactive)</option>
        </select>

        <button @click="resetFilters" class="btn-reset" title="Xóa bộ lọc">↺ Làm mới</button>
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
        <p>Đang tải danh sách người dùng...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="errorMsg" class="error-state">
        <div class="error-icon">⚠️</div>
        <p>{{ errorMsg }}</p>
        <button @click="fetchUsers" class="btn-retry">Thử lại</button>
      </div>

      <!-- Data Table -->
      <div v-else class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Người dùng</th>
              <th>Vai trò</th>
              <th>Trạng thái</th>
              <th>Xác thực Email</th>
              <th>Ngày tham gia</th>
              <th>Đăng nhập cuối</th>
              <th class="text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="users.length === 0">
              <td colspan="8" class="empty-state">Không tìm thấy người dùng nào phù hợp.</td>
            </tr>
            <tr v-for="user in users" :key="user.id">
              <td class="text-gray">#{{ user.id }}</td>
              <td>
                <div class="user-info">
                  <span class="user-name">{{ user.fullName }}</span>
                  <span class="user-email">{{ user.email }}</span>
                </div>
              </td>
              <td>
                <div class="role-list">
                  <span v-for="role in user.roles" :key="role" class="badge role-badge">
                    {{ formatRole(role) }}
                  </span>
                </div>
              </td>
              <td>
                <span :class="['badge', getStatusBadgeClass(user.status)]">
                  {{ formatStatus(user.status) }}
                </span>
              </td>
              <td>
                <span :class="['badge', user.emailVerified ? 'badge-success' : 'badge-draft']">
                  {{ user.emailVerified ? 'Đã xác thực' : 'Chưa xác thực' }}
                </span>
              </td>
              <td class="text-gray">{{ formatDate(user.createdAt) }}</td>
              <td class="text-gray">{{ formatDate(user.lastLoginAt) || 'Chưa đăng nhập' }}</td>
              <td class="text-right actions-cell">
                <template v-if="!user.roles.includes('SUPER_ADMIN')">
                  <button 
                    v-if="user.status !== 'LOCKED'"
                    @click="handleLockUser(user)" 
                    class="btn-action btn-lock"
                    :disabled="isProcessingId === user.id"
                    title="Khóa tài khoản"
                  >
                    🔒 Khóa
                  </button>
                  <button 
                    v-else
                    @click="handleUnlockUser(user)" 
                    class="btn-action btn-unlock"
                    :disabled="isProcessingId === user.id"
                    title="Mở khóa tài khoản"
                  >
                    🔓 Mở khóa
                  </button>
                </template>
                <span v-else class="text-gray text-small">Không thể sửa</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="!isLoading && !errorMsg && pagination.totalPages > 0" class="pagination">
      <div class="pagination-info">
        Hiển thị {{ users.length }} / {{ pagination.totalElements }} người dùng
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { AdminService } from '@/services/admin.service'
import { getApiErrorMessage } from '@/utils/api-error'
import { useRouter } from 'vue-router'

const router = useRouter()

// State
const users = ref([])
const isLoading = ref(true)
const errorMsg = ref('')
const actionError = ref('')
const isProcessingId = ref(null)

const pagination = reactive({
  currentPage: 0,
  pageSize: 10,
  totalPages: 0,
  totalElements: 0
})

const filters = reactive({
  keyword: '',
  role: '',
  status: ''
})

// Methods
const fetchUsers = async () => {
  isLoading.value = true
  errorMsg.value = ''
  
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize
    }
    
    if (filters.keyword.trim()) params.keyword = filters.keyword.trim()
    if (filters.role) params.role = filters.role
    if (filters.status) params.status = filters.status

    const res = await AdminService.getUsers(params)
    if (res.data.code === 1000) {
      users.value = res.data.result.data
      pagination.currentPage = res.data.result.currentPage
      pagination.totalPages = res.data.result.totalPages
      pagination.totalElements = res.data.result.totalElements
    }
  } catch (error) {
    if (error.response && error.response.status === 403) {
      errorMsg.value = 'Bạn không có quyền truy cập trang này.'
    } else {
      errorMsg.value = getApiErrorMessage(error, 'Không thể tải danh sách người dùng.')
    }
    console.error('Fetch users error:', error)
  } finally {
    isLoading.value = false
  }
}

const handleFilterChange = () => {
  pagination.currentPage = 0
  fetchUsers()
}

const resetFilters = () => {
  filters.keyword = ''
  filters.role = ''
  filters.status = ''
  pagination.currentPage = 0
  fetchUsers()
}

const changePage = (newPage) => {
  if (newPage >= 0 && newPage < pagination.totalPages) {
    pagination.currentPage = newPage
    fetchUsers()
  }
}

const handleLockUser = async (user) => {
  if (!window.confirm(`Bạn có chắc chắn muốn khóa tài khoản của ${user.fullName} (${user.email})?`)) {
    return
  }
  
  actionError.value = ''
  isProcessingId.value = user.id
  
  try {
    const res = await AdminService.lockUser(user.id)
    if (res.data.code === 1000) {
      // Cập nhật ngay trên UI thay vì load lại nguyên list cho mượt
      const index = users.value.findIndex(u => u.id === user.id)
      if (index !== -1) {
        users.value[index].status = res.data.result.status
      }
    }
  } catch (error) {
    actionError.value = getApiErrorMessage(error, 'Không thể khóa tài khoản này.')
  } finally {
    isProcessingId.value = null
  }
}

const handleUnlockUser = async (user) => {
  if (!window.confirm(`Xác nhận MỞ KHÓA tài khoản của ${user.fullName} (${user.email})?`)) {
    return
  }
  
  actionError.value = ''
  isProcessingId.value = user.id
  
  try {
    const res = await AdminService.unlockUser(user.id)
    if (res.data.code === 1000) {
      const index = users.value.findIndex(u => u.id === user.id)
      if (index !== -1) {
        users.value[index].status = res.data.result.status
      }
    }
  } catch (error) {
    actionError.value = getApiErrorMessage(error, 'Không thể mở khóa tài khoản này.')
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

const formatRole = (role) => {
  const roleMap = {
    'ADMIN': 'Admin',
    'SUPER_ADMIN': 'Super Admin',
    'TEACHER': 'Giáo viên',
    'CONTENT_EDITOR': 'Biên tập viên',
    'STUDENT': 'Học viên',
    'GUEST': 'Khách'
  }
  return roleMap[role] || role
}

const formatStatus = (status) => {
  const statusMap = {
    'ACTIVE': 'Hoạt động',
    'LOCKED': 'Đã khóa',
    'INACTIVE': 'Ngừng HĐ',
    'DELETED': 'Đã xóa'
  }
  return statusMap[status] || status
}

const getStatusBadgeClass = (status) => {
  switch (status) {
    case 'ACTIVE': return 'badge-success'
    case 'LOCKED': return 'badge-danger'
    case 'INACTIVE': return 'badge-warning'
    default: return 'badge-draft'
  }
}

// Init
onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.admin-user-management {
  max-width: 1280px;
  margin: 0 auto;
}
.page-header {
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

/* Filters */
.filters-section {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.5rem;
  background: white;
  padding: 1.25rem;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.search-box {
  display: flex;
  flex: 1;
  min-width: 300px;
  max-width: 400px;
}
.search-box input {
  flex: 1;
  padding: 0.625rem 1rem;
  border: 1px solid #cbd5e1;
  border-right: none;
  border-radius: 6px 0 0 6px;
  outline: none;
}
.search-box input:focus {
  border-color: #3b82f6;
}
.btn-search {
  padding: 0.625rem 1rem;
  background-color: #f1f5f9;
  border: 1px solid #cbd5e1;
  border-radius: 0 6px 6px 0;
  cursor: pointer;
}
.filter-group {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}
.filter-select {
  padding: 0.625rem;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  outline: none;
  background-color: white;
  min-width: 150px;
}
.filter-select:focus {
  border-color: #3b82f6;
}
.btn-reset {
  padding: 0.625rem 1rem;
  background-color: white;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  cursor: pointer;
  color: #64748b;
}
.btn-reset:hover {
  background-color: #f8fafc;
  color: #0f172a;
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
.user-info {
  display: flex;
  flex-direction: column;
}
.user-name {
  font-weight: 600;
  color: #0f172a;
}
.user-email {
  font-size: 0.85rem;
  color: #64748b;
}
.role-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
}

/* Badges */
.badge {
  padding: 0.25rem 0.5rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 500;
  white-space: nowrap;
}
.role-badge {
  background-color: #e0f2fe;
  color: #0369a1;
}
.badge-success {
  background-color: #dcfce7;
  color: #15803d;
}
.badge-draft {
  background-color: #f1f5f9;
  color: #475569;
}
.badge-danger {
  background-color: #fee2e2;
  color: #b91c1c;
}
.badge-warning {
  background-color: #fef3c7;
  color: #b45309;
}

/* Utilities */
.text-gray {
  color: #64748b;
  font-size: 0.9rem;
}
.text-right {
  text-align: right;
}
.text-small {
  font-size: 0.8rem;
}
.empty-state {
  text-align: center;
  padding: 3rem !important;
  color: #64748b;
  font-style: italic;
}

/* Actions */
.actions-cell {
  min-width: 100px;
}
.btn-action {
  padding: 0.35rem 0.75rem;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
}
.btn-action:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-lock {
  background-color: white;
  color: #ef4444;
  border-color: #fca5a5;
}
.btn-lock:hover:not(:disabled) {
  background-color: #fef2f2;
}
.btn-unlock {
  background-color: white;
  color: #10b981;
  border-color: #6ee7b7;
}
.btn-unlock:hover:not(:disabled) {
  background-color: #ecfdf5;
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
