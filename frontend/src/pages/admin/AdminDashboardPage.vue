<template>
  <div class="admin-dashboard">
    <div class="page-header">
      <h1 class="page-title">Tổng quan Hệ thống</h1>
      <p class="page-subtitle">Theo dõi các chỉ số cốt lõi và hoạt động mới nhất của nền tảng.</p>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="loading-container">
      <div class="spinner"></div>
      <p>Đang tải dữ liệu hệ thống...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="errorMsg" class="error-container">
      <div class="error-icon">⚠️</div>
      <p class="error-text">{{ errorMsg }}</p>
      <button v-if="!isForbidden" @click="fetchData" class="btn-retry">Thử lại</button>
      <button v-else @click="handleForbidden" class="btn-primary">Quay về Đăng nhập</button>
    </div>

    <!-- Main Content -->
    <template v-else>
      <!-- Stats Grid -->
      <section class="stats-grid">
        <StatCard title="Tổng Người dùng" :value="stats.totalUsers" icon="👥" color="#3B82F6" />
        <StatCard title="Tổng Khóa học" :value="stats.totalCourses" icon="📚" color="#8B5CF6" />
        <StatCard title="Tổng Bài học" :value="stats.totalLessons" icon="📝" color="#F59E0B" />
        <StatCard title="Lượt Ghi danh" :value="stats.totalEnrollments" icon="🚀" color="#10B981" />
      </section>

      <!-- Recent Activity Tables -->
      <section class="activity-section">
        <!-- Recent Users -->
        <div class="activity-card">
          <h2 class="card-title">Người dùng mới đăng ký</h2>
          <div v-if="stats.recentUsers && stats.recentUsers.length > 0" class="table-wrapper">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Tên</th>
                  <th>Email</th>
                  <th>Vai trò</th>
                  <th>Ngày tạo</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in stats.recentUsers" :key="user.id">
                  <td>{{ user.fullName }}</td>
                  <td>{{ user.email }}</td>
                  <td><span class="badge role-badge">{{ formatRole(user.role) }}</span></td>
                  <td class="text-gray">{{ formatDate(user.createdAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else class="empty-state">Không có dữ liệu người dùng mới.</div>
        </div>

        <!-- Recent Courses -->
        <div class="activity-card">
          <h2 class="card-title">Khóa học mới xuất bản</h2>
          <div v-if="stats.recentCourses && stats.recentCourses.length > 0" class="table-wrapper">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Tên khóa học</th>
                  <th>Giáo viên</th>
                  <th>Trạng thái</th>
                  <th>Ngày tạo</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="course in stats.recentCourses" :key="course.id">
                  <td class="course-name">{{ course.title }}</td>
                  <td>{{ course.teacherName || 'N/A' }}</td>
                  <td>
                    <span :class="['badge', course.published ? 'badge-success' : 'badge-draft']">
                      {{ course.published ? 'Xuất bản' : 'Bản nháp' }}
                    </span>
                  </td>
                  <td class="text-gray">{{ formatDate(course.createdAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else class="empty-state">Không có khóa học mới nào.</div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { AdminService } from '@/services/admin.service'
import { getApiErrorMessage } from '@/utils/api-error'
import StatCard from '@/components/admin/StatCard.vue'

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
    if (res.data.code === 1000) {
      stats.value = res.data.result
    }
  } catch (error) {
    if (error.response && error.response.status === 403) {
      isForbidden.value = true
      errorMsg.value = 'Bạn không có quyền truy cập vào khu vực Quản trị viên.'
    } else {
      errorMsg.value = getApiErrorMessage(error, 'Không thể tải dữ liệu thống kê.')
    }
    console.error('Admin Dashboard fetch error:', error)
  } finally {
    isLoading.value = false
  }
}

const handleForbidden = () => {
  // Clear auth and force logout due to tampered state or unauthorized access
  authStore.clearAuth()
  router.push('/login')
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('vi-VN', { 
    year: 'numeric', month: '2-digit', day: '2-digit' 
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

onMounted(fetchData)
</script>

<style scoped>
.admin-dashboard {
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

/* Loading & Error */
.loading-container, .error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
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
.error-text {
  color: #ef4444;
  font-size: 1.1rem;
  margin-bottom: 1.5rem;
  text-align: center;
}
.btn-retry {
  padding: 0.625rem 1.5rem;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
.btn-primary {
  padding: 0.625rem 1.5rem;
  background-color: #0f172a;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

/* Activity Section */
.activity-section {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}
@media (min-width: 1024px) {
  .activity-section {
    grid-template-columns: 1fr 1fr;
  }
}
.activity-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  padding: 1.5rem;
  overflow: hidden;
}
.card-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 1rem;
}
.table-wrapper {
  overflow-x: auto;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}
.data-table th {
  padding: 0.75rem 1rem;
  background-color: #f8fafc;
  color: #64748b;
  font-weight: 600;
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid #e2e8f0;
}
.data-table td {
  padding: 1rem;
  border-bottom: 1px solid #f1f5f9;
  font-size: 0.95rem;
  color: #334155;
  white-space: nowrap;
}
.data-table tbody tr:last-child td {
  border-bottom: none;
}
.course-name {
  font-weight: 500;
  color: #0f172a;
}
.text-gray {
  color: #64748b;
  font-size: 0.85rem;
}
.badge {
  padding: 0.25rem 0.625rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 500;
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
.empty-state {
  padding: 2rem;
  text-align: center;
  color: #64748b;
  font-style: italic;
}
</style>
