<template>
  <div class="admin-dashboard max-w-[1280px] mx-auto">
    <!-- Loading State -->
    <div v-if="isLoading" class="flex flex-col items-center justify-center py-20 text-white/50">
      <span class="material-symbols-outlined animate-spin text-4xl mb-4">autorenew</span>
      <p>Đang tải dữ liệu thống kê...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="errorMsg" class="bg-red-500/10 border border-red-500/20 rounded-2xl p-8 text-center max-w-lg mx-auto mt-10">
      <div class="text-4xl mb-4">⚠️</div>
      <h2 class="text-xl font-bold text-red-400 mb-2">Không thể tải dữ liệu</h2>
      <p class="text-red-400/80 mb-6">{{ errorMsg }}</p>
      <button 
        v-if="!isForbidden" 
        @click="fetchData" 
        class="bg-red-500/20 text-red-400 px-6 py-2.5 rounded-lg hover:bg-red-500/30 transition-colors font-medium"
      >
        Thử lại
      </button>
      <button 
        v-else 
        @click="handleForbidden" 
        class="bg-stitch-primary text-white px-6 py-2.5 rounded-lg hover:bg-stitch-primary/90 transition-colors font-medium shadow-lg"
      >
        Quay về Đăng nhập
      </button>
    </div>

    <!-- Main Content -->
    <template v-else>
      <!-- Stat cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <div class="bg-[#161b27] rounded-2xl border border-white/5 p-5">
          <div class="flex items-start justify-between mb-4">
            <span class="text-2xl">👥</span>
            <span class="text-xs px-2 py-0.5 rounded-full bg-white/5 text-white/40">—</span>
          </div>
          <div class="text-2xl font-stitch-serif font-bold mb-1">{{ stats.totalUsers?.toLocaleString() || 0 }}</div>
          <div class="text-xs text-white/40">Tổng học viên</div>
        </div>
        
        <div class="bg-[#161b27] rounded-2xl border border-white/5 p-5">
          <div class="flex items-start justify-between mb-4">
            <span class="text-2xl">📚</span>
            <span class="text-xs px-2 py-0.5 rounded-full bg-white/5 text-white/40">—</span>
          </div>
          <div class="text-2xl font-stitch-serif font-bold mb-1">{{ stats.totalCourses?.toLocaleString() || 0 }}</div>
          <div class="text-xs text-white/40">Tổng khóa học</div>
        </div>

        <div class="bg-[#161b27] rounded-2xl border border-white/5 p-5">
          <div class="flex items-start justify-between mb-4">
            <span class="text-2xl">📝</span>
            <span class="text-xs px-2 py-0.5 rounded-full bg-white/5 text-white/40">—</span>
          </div>
          <div class="text-2xl font-stitch-serif font-bold mb-1">{{ stats.totalLessons?.toLocaleString() || 0 }}</div>
          <div class="text-xs text-white/40">Tổng bài học</div>
        </div>

        <div class="bg-[#161b27] rounded-2xl border border-white/5 p-5">
          <div class="flex items-start justify-between mb-4">
            <span class="text-2xl">🚀</span>
            <span class="text-xs px-2 py-0.5 rounded-full bg-green-500/10 text-green-400">▲</span>
          </div>
          <div class="text-2xl font-stitch-serif font-bold mb-1">{{ stats.totalEnrollments?.toLocaleString() || 0 }}</div>
          <div class="text-xs text-white/40">Lượt ghi danh</div>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Recent users -->
        <div class="bg-[#161b27] rounded-2xl border border-white/5 p-5 flex flex-col h-[400px]">
          <div class="flex items-center justify-between mb-4 shrink-0">
            <h3 class="font-semibold text-white/90">Học viên mới đăng ký</h3>
            <router-link to="/admin/users" class="text-xs text-stitch-accent hover:underline">Xem tất cả</router-link>
          </div>
          
          <div v-if="stats.recentUsers && stats.recentUsers.length > 0" class="space-y-3 overflow-y-auto pr-2 custom-scrollbar flex-1">
            <div v-for="u in stats.recentUsers" :key="u.id" class="flex items-center gap-3 p-2 hover:bg-white/5 rounded-xl transition-colors">
              <div class="w-10 h-10 rounded-full bg-gradient-to-br from-stitch-primary/30 to-stitch-accent/30 flex items-center justify-center text-sm font-bold flex-shrink-0 text-white/80 border border-white/10">
                {{ getUserInitials(u.fullName) }}
              </div>
              <div class="flex-1 min-w-0">
                <div class="text-sm text-white/90 font-medium truncate">{{ u.fullName }}</div>
                <div class="text-xs text-white/40 truncate">{{ u.email }}</div>
              </div>
              <div class="text-right flex-shrink-0">
                <div class="text-xs text-white/40 mb-1">{{ formatDateShort(u.createdAt) }}</div>
                <span :class="['text-[10px] px-2 py-0.5 rounded-full uppercase tracking-wide font-medium', getRoleBadgeClass(u.role)]">
                  {{ formatRole(u.role) }}
                </span>
              </div>
            </div>
          </div>
          <div v-else class="flex-1 flex items-center justify-center text-white/30 text-sm italic">
            Không có dữ liệu người dùng mới.
          </div>
        </div>

        <!-- Recent Courses -->
        <div class="bg-[#161b27] rounded-2xl border border-white/5 p-5 flex flex-col h-[400px]">
          <div class="flex items-center justify-between mb-4 shrink-0">
            <h3 class="font-semibold text-white/90">Khóa học mới nhất</h3>
            <router-link to="/admin/courses" class="text-xs text-stitch-accent hover:underline">Xem tất cả</router-link>
          </div>
          
          <div v-if="stats.recentCourses && stats.recentCourses.length > 0" class="space-y-3 overflow-y-auto pr-2 custom-scrollbar flex-1">
            <div v-for="c in stats.recentCourses" :key="c.id" class="flex items-center gap-3 p-2 hover:bg-white/5 rounded-xl transition-colors">
              <div class="w-10 h-10 rounded-lg bg-white/5 border border-white/10 flex items-center justify-center text-lg flex-shrink-0">
                📚
              </div>
              <div class="flex-1 min-w-0">
                <div class="text-sm text-white/90 font-medium truncate mb-0.5">{{ c.title }}</div>
                <div class="text-xs text-white/40 truncate">{{ c.teacherName || 'Chưa phân công' }}</div>
              </div>
              <div class="text-right flex-shrink-0">
                <div class="text-xs text-white/40 mb-1">{{ formatDateShort(c.createdAt) }}</div>
                <span :class="['text-[10px] px-2 py-0.5 rounded-full uppercase tracking-wide font-medium', c.published ? 'bg-green-500/10 text-green-400' : 'bg-white/5 text-white/40']">
                  {{ c.published ? 'Xuất bản' : 'Bản nháp' }}
                </span>
              </div>
            </div>
          </div>
          <div v-else class="flex-1 flex items-center justify-center text-white/30 text-sm italic">
            Không có khóa học mới nào.
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { AuthService } from '@/services/auth.service'
import { AdminService } from '@/services/admin.service'
import { getApiErrorMessage } from '@/utils/api-error'

const router = useRouter()
const authStore = useAuthStore()

const isLoading = ref(true)
const errorMsg = ref('')
const isForbidden = ref(false)

const stats = ref({
  totalUsers: 0,
  totalCourses: 0,
  totalLessons: 0,
  totalEnrollments: 0,
  recentUsers: [],
  recentCourses: []
})

const fetchData = async () => {
  isLoading.value = true
  errorMsg.value = ''
  isForbidden.value = false

  try {
    const res = await AdminService.getDashboardStats()
    if (res.data && res.data.code === 1000) {
      stats.value = res.data.result
    } else {
      throw new Error(res.data?.message || 'Failed to load stats')
    }
  } catch (error) {
    if (error.response?.status === 403) {
      isForbidden.value = true
      errorMsg.value = 'Bạn không có quyền truy cập vào khu vực Quản trị viên.'
    } else {
      errorMsg.value = getApiErrorMessage(error, 'Không thể tải dữ liệu thống kê.')
    }
  } finally {
    isLoading.value = false
  }
}

const handleForbidden = async () => {
  try {
    await AuthService.logout()
  } catch (e) {}
  authStore.clearAuth()
  router.push('/login')
}

const formatDateShort = (dateString) => {
  if (!dateString) return ''
  const d = new Date(dateString)
  return `${d.getDate().toString().padStart(2, '0')}/${(d.getMonth()+1).toString().padStart(2, '0')}/${d.getFullYear()}`
}

const getUserInitials = (name) => {
  if (!name) return '?'
  const parts = name.split(' ')
  if (parts.length >= 2) return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
  return name.substring(0, 2).toUpperCase()
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
  if (role === 'ADMIN' || role === 'SUPER_ADMIN') return 'bg-purple-500/10 text-purple-400'
  if (role === 'TEACHER') return 'bg-blue-500/10 text-blue-400'
  return 'bg-white/5 text-white/40'
}

onMounted(fetchData)
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background-color: rgba(255, 255, 255, 0.2);
}
</style>
