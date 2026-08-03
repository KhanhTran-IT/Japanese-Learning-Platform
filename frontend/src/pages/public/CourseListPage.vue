<template>
  <div class="course-list-page">
    <!-- Page Header -->
    <div class="page-hero">
      <h1 class="page-title">Khóa Học Tiếng Nhật</h1>
      <p class="page-subtitle">Khám phá các khóa học chất lượng từ cơ bản đến nâng cao, phù hợp với mọi trình độ.</p>
    </div>

    <!-- Filters Section -->
    <div class="filters-section">
      <div class="search-box">
        <input
          id="course-search"
          type="text"
          v-model="searchInput"
          placeholder="Tìm kiếm khóa học..."
          @keyup.enter="applySearch"
        />
        <button class="btn-search" @click="applySearch">Tìm</button>
      </div>

      <div class="filter-group">
        <div class="filter-item">
          <label for="filter-level">Cấp độ</label>
          <select id="filter-level" v-model="filters.level" @change="onFilterChange">
            <option value="">Tất cả</option>
            <option value="N5">N5</option>
            <option value="N4">N4</option>
            <option value="N3">N3</option>
            <option value="N2">N2</option>
            <option value="N1">N1</option>
          </select>
        </div>

        <div class="filter-item">
          <label for="filter-type">Loại khóa học</label>
          <select id="filter-type" v-model="filters.courseType" @change="onFilterChange">
            <option value="">Tất cả</option>
            <option value="FREE">Miễn phí</option>
            <option value="PAID">Trả phí</option>
          </select>
        </div>
      </div>

      <!-- Active filter summary -->
      <div v-if="hasActiveFilters" class="active-filters">
        <span class="filter-summary">
          {{ totalElements }} khóa học
          <template v-if="filters.keyword"> cho "{{ filters.keyword }}"</template>
        </span>
        <button class="btn-clear-filters" @click="clearAllFilters">Xóa bộ lọc</button>
      </div>
    </div>

    <!-- Content Area -->
    <div class="content-area">
      <!-- Loading State -->
      <div v-if="isLoading" class="state-container loading-state">
        <div class="spinner"></div>
        <p>Đang tải khóa học...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="errorMsg" class="state-container error-state">
        <p class="error-text">{{ errorMsg }}</p>
        <button class="btn-retry" @click="fetchCourses">Thử lại</button>
      </div>

      <!-- Empty State -->
      <div v-else-if="courses.length === 0" class="state-container empty-state">
        <p>Không tìm thấy khóa học phù hợp.</p>
        <button v-if="hasActiveFilters" class="btn-retry" @click="clearAllFilters">Xóa bộ lọc</button>
      </div>

      <!-- Course Grid -->
      <div v-else class="courses-grid">
        <div v-for="course in courses" :key="course.id" class="course-card">
          <!-- Thumbnail -->
          <div class="card-thumbnail">
            <img
              v-if="course.thumbnailUrl"
              :src="course.thumbnailUrl"
              :alt="course.title"
              loading="lazy"
              @error="onImgError"
            />
            <div v-else class="thumb-placeholder">
              <span>{{ course.level || 'JP' }}</span>
            </div>
            <!-- Badges -->
            <span v-if="course.courseType === 'FREE'" class="badge-free">Miễn phí</span>
            <span v-if="course.level" class="badge-level">{{ course.level }}</span>
          </div>

          <!-- Card Body -->
          <div class="card-body">
            <h3 class="card-title">
              <router-link :to="`/courses/${course.slug}`">{{ course.title }}</router-link>
            </h3>
            <p class="card-desc">{{ course.shortDescription || 'Chưa có mô tả.' }}</p>

            <div class="card-meta">
              <span class="meta-item">{{ course.totalLessons || 0 }} bài học</span>
              <span class="meta-item">{{ formatDuration(course.totalDurationMinutes) }}</span>
              <span class="meta-item">{{ course.totalStudents || 0 }} học viên</span>
            </div>

            <div class="card-footer">
              <div class="card-price">
                <template v-if="course.courseType === 'FREE'">
                  <span class="price-free">Miễn phí</span>
                </template>
                <template v-else>
                  <span v-if="course.salePrice > 0 && course.salePrice < course.originalPrice" class="price-sale">
                    {{ formatPrice(course.salePrice) }}
                  </span>
                  <span :class="['price-original', { 'price-strikethrough': course.salePrice > 0 && course.salePrice < course.originalPrice }]">
                    {{ formatPrice(course.originalPrice) }}
                  </span>
                </template>
              </div>
              <span class="card-teacher">{{ course.teacherName || 'Giảng viên' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="!isLoading && !errorMsg && totalPages > 1" class="pagination">
        <button class="btn-page" :disabled="currentPage === 0" @click="goToPage(currentPage - 1)">
          Trang trước
        </button>
        <span class="page-info">
          Trang {{ currentPage + 1 }} / {{ totalPages }}
        </span>
        <button class="btn-page" :disabled="currentPage >= totalPages - 1" @click="goToPage(currentPage + 1)">
          Trang sau
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { CourseService } from '@/services/course.service'
import { getApiErrorMessage } from '@/utils/api-error'

// Data
const courses = ref([])
const isLoading = ref(true)
const errorMsg = ref('')

// Pagination
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const pageSize = 12

// Filters
const searchInput = ref('')
const filters = reactive({
  keyword: '',
  level: '',
  courseType: ''
})

const hasActiveFilters = computed(() => {
  return filters.keyword || filters.level || filters.courseType
})

onMounted(() => {
  fetchCourses()
})

const fetchCourses = async () => {
  isLoading.value = true
  errorMsg.value = ''

  try {
    const params = {
      page: currentPage.value,
      size: pageSize
    }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.level) params.level = filters.level
    if (filters.courseType) params.courseType = filters.courseType

    const res = await CourseService.getCourses(params)

    if (res.data.code === 1000) {
      const pageData = res.data.result
      courses.value = pageData.content || []
      currentPage.value = pageData.number
      totalPages.value = pageData.totalPages
      totalElements.value = pageData.totalElements
    }
  } catch (error) {
    errorMsg.value = getApiErrorMessage(error, 'Không thể tải danh sách khóa học.')
  } finally {
    isLoading.value = false
  }
}

const applySearch = () => {
  filters.keyword = searchInput.value.trim()
  currentPage.value = 0
  fetchCourses()
}

const onFilterChange = () => {
  currentPage.value = 0
  fetchCourses()
}

const clearAllFilters = () => {
  searchInput.value = ''
  filters.keyword = ''
  filters.level = ''
  filters.courseType = ''
  currentPage.value = 0
  fetchCourses()
}

const goToPage = (page) => {
  if (page < 0 || page >= totalPages.value) return
  currentPage.value = page
  fetchCourses()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// Helpers
const formatDuration = (minutes) => {
  if (!minutes || minutes <= 0) return '0 phút'
  if (minutes < 60) return `${minutes} phút`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m > 0 ? `${h}h ${m}p` : `${h} giờ`
}

const formatPrice = (price) => {
  if (!price || price <= 0) return '0đ'
  return new Intl.NumberFormat('vi-VN').format(price) + 'đ'
}

const onImgError = (e) => {
  e.target.style.display = 'none'
}
</script>

<style scoped>
.course-list-page {
  max-width: 1280px;
  margin: 0 auto;
}

/* Hero */
.page-hero {
  text-align: center;
  padding: 2rem 1rem 1.5rem;
}
.page-title {
  font-size: 2rem;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 0.5rem;
}
.page-subtitle {
  color: #64748b;
  font-size: 1.05rem;
  max-width: 600px;
  margin: 0 auto;
}

/* Filters */
.filters-section {
  padding: 0 1rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.search-box {
  display: flex;
  gap: 0.5rem;
  max-width: 500px;
}
.search-box input {
  flex: 1;
  padding: 0.625rem 0.75rem;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 0.95rem;
  outline: none;
}
.search-box input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}
.btn-search {
  padding: 0.625rem 1rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
}
.btn-search:hover {
  background: #2563eb;
}

.filter-group {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}
.filter-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.filter-item label {
  font-weight: 600;
  font-size: 0.9rem;
  color: #334155;
  white-space: nowrap;
}
.filter-item select {
  padding: 0.5rem 0.75rem;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 0.9rem;
  background: white;
  outline: none;
  cursor: pointer;
}
.filter-item select:focus {
  border-color: #3b82f6;
}

.active-filters {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
.filter-summary {
  font-size: 0.9rem;
  color: #475569;
  font-weight: 500;
}
.btn-clear-filters {
  padding: 0.35rem 0.75rem;
  border: 1px solid #cbd5e1;
  background: white;
  border-radius: 6px;
  font-size: 0.85rem;
  color: #ef4444;
  cursor: pointer;
}
.btn-clear-filters:hover {
  background: #fef2f2;
}

/* States */
.state-container {
  padding: 4rem 1rem;
  text-align: center;
  color: #64748b;
}
.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f1f5f9;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 1rem;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.error-text {
  color: #b91c1c;
  margin-bottom: 1rem;
}
.btn-retry {
  padding: 0.5rem 1.25rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
}
.btn-retry:hover {
  background: #2563eb;
}

/* Course Grid */
.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
  padding: 0 1rem;
}

/* Course Card */
.course-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #f1f5f9;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
}
.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

/* Thumbnail */
.card-thumbnail {
  position: relative;
  aspect-ratio: 16 / 9;
  background: #f1f5f9;
  overflow: hidden;
}
.card-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 2rem;
  font-weight: 800;
}
.badge-free {
  position: absolute;
  top: 0.5rem;
  left: 0.5rem;
  background: #22c55e;
  color: white;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 700;
}
.badge-level {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
}

/* Card Body */
.card-body {
  padding: 1rem;
  flex: 1;
  display: flex;
  flex-direction: column;
}
.card-title {
  font-size: 1rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  line-height: 1.4;
}
.card-title a {
  color: #1e293b;
  text-decoration: none;
}
.card-title a:hover {
  color: #3b82f6;
}
.card-desc {
  font-size: 0.85rem;
  color: #64748b;
  line-height: 1.5;
  margin-bottom: 0.75rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.card-meta {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  margin-bottom: 0.75rem;
}
.meta-item {
  font-size: 0.8rem;
  color: #94a3b8;
  font-weight: 500;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f1f5f9;
  padding-top: 0.75rem;
}
.card-price {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.price-free {
  color: #22c55e;
  font-weight: 700;
  font-size: 0.95rem;
}
.price-sale {
  color: #ef4444;
  font-weight: 700;
  font-size: 0.95rem;
}
.price-original {
  font-weight: 600;
  font-size: 0.95rem;
  color: #334155;
}
.price-strikethrough {
  text-decoration: line-through;
  color: #94a3b8;
  font-size: 0.85rem;
  font-weight: 400;
}
.card-teacher {
  font-size: 0.8rem;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  padding: 2rem 1rem;
}
.btn-page {
  padding: 0.5rem 1rem;
  border: 1px solid #cbd5e1;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  color: #334155;
}
.btn-page:hover:not(:disabled) {
  background: #f8fafc;
  border-color: #3b82f6;
  color: #3b82f6;
}
.btn-page:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.page-info {
  font-size: 0.9rem;
  color: #475569;
  font-weight: 500;
}

/* Responsive */
@media (max-width: 640px) {
  .page-title {
    font-size: 1.5rem;
  }
  .search-box {
    max-width: 100%;
  }
  .filter-group {
    flex-direction: column;
  }
  .courses-grid {
    grid-template-columns: 1fr;
  }
}
</style>
