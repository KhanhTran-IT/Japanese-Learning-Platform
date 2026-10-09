<template>
  <div class="admin-user-management max-w-[1280px] mx-auto">
    <!-- Filters Section -->
    <div class="bg-[#161b27] border border-white/5 rounded-2xl p-5 mb-6 flex flex-wrap items-center justify-between gap-4">
      <div class="flex items-center flex-1 min-w-[300px] max-w-[400px]">
        <input 
          type="text" 
          v-model="filters.keyword" 
          placeholder="Tìm theo tên hoặc email..." 
          @keyup.enter="handleFilterChange"
          class="flex-1 bg-white/5 border border-white/10 rounded-l-lg px-4 py-2.5 text-sm text-white placeholder-white/30 focus:outline-none focus:border-stitch-primary transition-colors"
        />
        <button 
          @click="handleFilterChange"
          class="bg-white/10 border border-white/10 border-l-0 rounded-r-lg px-4 py-2.5 text-white/70 hover:bg-white/20 hover:text-white transition-colors"
        >
          🔍
        </button>
      </div>
      
      <div class="flex items-center gap-3 flex-wrap">
        <select 
          v-model="filters.role" 
          @change="handleFilterChange" 
          class="bg-white/5 border border-white/10 rounded-lg px-4 py-2.5 text-sm text-white focus:outline-none focus:border-stitch-primary transition-colors"
        >
          <option value="" class="bg-[#161b27]">Tất cả Vai trò</option>
          <option value="STUDENT" class="bg-[#161b27]">Học viên</option>
          <option value="TEACHER" class="bg-[#161b27]">Giáo viên</option>
          <option value="CONTENT_EDITOR" class="bg-[#161b27]">Biên tập viên</option>
          <option value="ADMIN" class="bg-[#161b27]">Quản trị viên</option>
          <option value="SUPER_ADMIN" class="bg-[#161b27]">Super Admin</option>
        </select>
        
        <select 
          v-model="filters.status" 
          @change="handleFilterChange" 
          class="bg-white/5 border border-white/10 rounded-lg px-4 py-2.5 text-sm text-white focus:outline-none focus:border-stitch-primary transition-colors"
        >
          <option value="" class="bg-[#161b27]">Tất cả Trạng thái</option>
          <option value="ACTIVE" class="bg-[#161b27]">Hoạt động (Active)</option>
          <option value="LOCKED" class="bg-[#161b27]">Bị khóa (Locked)</option>
          <option value="INACTIVE" class="bg-[#161b27]">Ngừng hoạt động (Inactive)</option>
        </select>

        <button 
          @click="resetFilters" 
          class="bg-white/5 border border-white/10 rounded-lg px-4 py-2.5 text-sm text-white/70 hover:bg-white/10 hover:text-white transition-colors flex items-center gap-2"
        >
          <span>↺</span> Làm mới
        </button>
      </div>
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
        <p>Đang tải danh sách người dùng...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="errorMsg" class="flex flex-col items-center justify-center py-20 text-white/50">
        <div class="text-4xl mb-4 text-red-400">⚠️</div>
        <p class="text-red-400/80 mb-6">{{ errorMsg }}</p>
        <button @click="fetchUsers" class="bg-white/10 text-white px-6 py-2.5 rounded-lg hover:bg-white/20 transition-colors font-medium">
          Thử lại
        </button>
      </div>

      <!-- Data Table -->
      <div v-else class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-white/5 border-b border-white/10">
              <th class="p-4 text-xs font-semibold text-white/40 uppercase tracking-wider">ID</th>
              <th class="p-4 text-xs font-semibold text-white/40 uppercase tracking-wider">Người dùng</th>
              <th class="p-4 text-xs font-semibold text-white/40 uppercase tracking-wider">Vai trò</th>
              <th class="p-4 text-xs font-semibold text-white/40 uppercase tracking-wider">Trạng thái</th>
              <th class="p-4 text-xs font-semibold text-white/40 uppercase tracking-wider">Xác thực Email</th>
              <th class="p-4 text-xs font-semibold text-white/40 uppercase tracking-wider">Ngày tham gia</th>
              <th class="p-4 text-xs font-semibold text-white/40 uppercase tracking-wider">Đăng nhập cuối</th>
              <th class="p-4 text-xs font-semibold text-white/40 uppercase tracking-wider text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="users.length === 0">
              <td colspan="8" class="p-8 text-center text-white/30 italic">Không tìm thấy người dùng nào phù hợp.</td>
            </tr>
            <tr v-for="user in users" :key="user.id" class="border-b border-white/5 hover:bg-white/5 transition-colors">
              <td class="p-4 text-sm text-white/40">#{{ user.id }}</td>
              <td class="p-4">
                <div class="flex flex-col">
                  <span class="text-sm font-medium text-white/90">{{ user.fullName }}</span>
                  <span class="text-xs text-white/40">{{ user.email }}</span>
                </div>
              </td>
              <td class="p-4">
                <div class="flex flex-wrap gap-1.5">
                  <span v-for="role in user.roles" :key="role" :class="['text-[10px] px-2 py-0.5 rounded-full uppercase tracking-wide font-medium', getRoleBadgeClass(role)]">
                    {{ formatRole(role) }}
                  </span>
                </div>
              </td>
              <td class="p-4">
                <span :class="['text-[10px] px-2 py-0.5 rounded-full uppercase tracking-wide font-medium', getStatusBadgeClass(user.status)]">
                  {{ formatStatus(user.status) }}
                </span>
              </td>
              <td class="p-4">
                <span :class="['text-[10px] px-2 py-0.5 rounded-full uppercase tracking-wide font-medium', user.emailVerified ? 'bg-green-500/10 text-green-400' : 'bg-white/5 text-white/40']">
                  {{ user.emailVerified ? 'Đã xác thực' : 'Chưa xác thực' }}
                </span>
              </td>
              <td class="p-4 text-sm text-white/50">{{ formatDate(user.createdAt) }}</td>
              <td class="p-4 text-sm text-white/50">{{ formatDate(user.lastLoginAt) || 'Chưa đăng nhập' }}</td>
              <td class="p-4 text-right">
                <template v-if="!user.roles.includes('SUPER_ADMIN')">
                  <button 
                    v-if="user.status !== 'LOCKED'"
                    @click="handleLockUser(user)" 
                    class="px-3 py-1.5 text-xs font-medium rounded-lg border border-red-500/30 text-red-400 hover:bg-red-500/10 transition-colors disabled:opacity-50"
                    :disabled="isProcessingId === user.id"
                    title="Khóa tài khoản"
                  >
                    🔒 Khóa
                  </button>
                  <button 
                    v-else
                    @click="handleUnlockUser(user)" 
                    class="px-3 py-1.5 text-xs font-medium rounded-lg border border-green-500/30 text-green-400 hover:bg-green-500/10 transition-colors disabled:opacity-50"
                    :disabled="isProcessingId === user.id"
                    title="Mở khóa tài khoản"
                  >
                    🔓 Mở khóa
                  </button>
                </template>
                <span v-else class="text-xs text-white/30 italic">Không thể sửa</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="!isLoading && !errorMsg && pagination.totalPages > 0" class="mt-6 flex items-center justify-between">
      <div class="text-sm text-white/50">
        Hiển thị {{ users.length }} / {{ pagination.totalElements }} người dùng
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { AdminService } from '@/services/admin.service'
import { getApiErrorMessage } from '@/utils/api-error'

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
    if (error.response?.status === 403) {
      errorMsg.value = 'Bạn không có quyền truy cập trang này.'
    } else {
      errorMsg.value = getApiErrorMessage(error, 'Không thể tải danh sách người dùng.')
    }
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
    'SUPER_ADMIN': 'Super',
    'TEACHER': 'Giáo viên',
    'CONTENT_EDITOR': 'Editor',
    'STUDENT': 'Học viên',
    'GUEST': 'Khách'
  }
  return roleMap[role] || role
}

const getRoleBadgeClass = (role) => {
  if (role === 'ADMIN' || role === 'SUPER_ADMIN') return 'bg-purple-500/10 text-purple-400 border border-purple-500/20'
  if (role === 'TEACHER') return 'bg-blue-500/10 text-blue-400 border border-blue-500/20'
  if (role === 'CONTENT_EDITOR') return 'bg-orange-500/10 text-orange-400 border border-orange-500/20'
  return 'bg-white/5 text-white/50 border border-white/10'
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
    case 'ACTIVE': return 'bg-green-500/10 text-green-400 border border-green-500/20'
    case 'LOCKED': return 'bg-red-500/10 text-red-400 border border-red-500/20'
    case 'INACTIVE': return 'bg-yellow-500/10 text-yellow-400 border border-yellow-500/20'
    default: return 'bg-white/5 text-white/40 border border-white/10'
  }
}

onMounted(() => {
  fetchUsers()
})
</script>
