<template>
  <div class="min-h-screen bg-stitch-background font-stitch-sans">
    <!-- Header -->
    <div class="bg-stitch-foreground text-white py-16">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <p class="text-sm text-stitch-accent font-medium uppercase tracking-widest mb-2 font-stitch-serif">Danh sách khóa học</p>
        <h1 class="text-4xl font-stitch-serif font-bold mb-4">Tìm khóa học phù hợp</h1>
        <p class="text-white/60 mb-8">Từ N5 đến N1 — miễn phí và trả phí, học theo tốc độ của bạn.</p>
        <div class="relative max-w-lg">
          <span class="absolute left-4 top-1/2 -translate-y-1/2 text-white/40">🔍</span>
          <input
            type="text"
            placeholder="Tìm kiếm khóa học..."
            v-model="searchInput"
            @keyup.enter="applySearch"
            class="w-full pl-12 pr-4 py-3 bg-white/10 border border-white/20 rounded-xl text-white placeholder-white/40 focus:outline-none focus:border-stitch-accent text-sm transition-colors"
          />
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Filters -->
      <div class="flex flex-wrap gap-4 items-center justify-between mb-8">
        <div class="flex flex-wrap gap-3">
          <!-- Level Filter -->
          <div class="flex flex-wrap gap-2">
            <button
              v-for="l in levels"
              :key="l.value"
              @click="setFilter('level', l.value)"
              :class="[
                'px-3 py-1.5 rounded-lg text-sm font-medium transition-all',
                filters.level === l.value
                  ? 'bg-stitch-primary text-white'
                  : 'bg-white border border-stitch-border text-stitch-muted-foreground hover:border-stitch-primary/50'
              ]"
            >
              {{ l.label }}
            </button>
          </div>
          <!-- Type Filter -->
          <div class="flex flex-wrap gap-2">
            <button
              v-for="t in types"
              :key="t.value"
              @click="setFilter('courseType', t.value)"
              :class="[
                'px-3 py-1.5 rounded-lg text-sm font-medium transition-all',
                filters.courseType === t.value
                  ? 'bg-stitch-foreground text-white'
                  : 'bg-white border border-stitch-border text-stitch-muted-foreground hover:border-stitch-foreground/30'
              ]"
            >
              {{ t.label }}
            </button>
          </div>
        </div>
        <!-- Sorting -->
        <div class="flex items-center gap-2">
          <span class="text-sm text-stitch-muted-foreground">Sắp xếp:</span>
          <select
            v-model="filters.sort"
            @change="onFilterChange"
            class="text-sm border border-stitch-border rounded-lg px-3 py-1.5 bg-white focus:outline-none focus:border-stitch-primary text-stitch-card-foreground cursor-pointer"
          >
            <option value="id,desc">Mới nhất</option>
            <option value="totalStudents,desc">Phổ biến nhất</option>
            <option value="averageRating,desc">Đánh giá cao nhất</option>
            <option value="originalPrice,asc">Giá thấp đến cao</option>
            <option value="originalPrice,desc">Giá cao đến thấp</option>
          </select>
        </div>
      </div>

      <div class="flex justify-between items-center mb-6">
        <p class="text-sm text-stitch-muted-foreground">Tìm thấy {{ totalElements }} khóa học</p>
        <button v-if="hasActiveFilters" @click="clearAllFilters" class="text-sm text-red-500 hover:underline">Xóa bộ lọc</button>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="flex justify-center items-center py-20 text-stitch-muted-foreground">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-stitch-primary"></div>
      </div>

      <!-- Error State -->
      <div v-else-if="errorMsg" class="text-center py-20">
        <div class="text-5xl mb-4">⚠️</div>
        <h3 class="font-stitch-serif font-bold text-xl mb-2 text-stitch-foreground">Đã xảy ra lỗi</h3>
        <p class="text-stitch-muted-foreground mb-4">{{ errorMsg }}</p>
        <button @click="fetchCourses" class="px-6 py-2 bg-stitch-primary text-white rounded-lg hover:bg-stitch-primary/90 transition-colors">Thử lại</button>
      </div>

      <!-- Empty State -->
      <div v-else-if="courses.length === 0" class="text-center py-20">
        <div class="text-5xl mb-4">🔍</div>
        <h3 class="font-stitch-serif font-bold text-xl mb-2 text-stitch-foreground">Không tìm thấy khóa học</h3>
        <p class="text-stitch-muted-foreground mb-4">Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm.</p>
        <button v-if="hasActiveFilters" @click="clearAllFilters" class="px-6 py-2 border border-stitch-border text-stitch-muted-foreground rounded-lg hover:bg-stitch-muted transition-colors">Xóa bộ lọc</button>
      </div>

      <!-- Course grid -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <router-link
          v-for="c in courses"
          :key="c.id"
          :to="`/courses/${c.slug}`"
          class="group bg-white rounded-2xl overflow-hidden border border-stitch-border hover:shadow-xl hover:shadow-black/5 transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring flex flex-col"
        >
          <div class="relative h-48 overflow-hidden bg-stitch-muted shrink-0">
            <img v-if="c.thumbnailUrl" :src="c.thumbnailUrl" :alt="c.title" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" @error="onImgError" />
            <div v-else class="w-full h-full flex items-center justify-center bg-stitch-muted group-hover:scale-105 transition-transform duration-500">
              <span class="text-4xl font-bold text-stitch-muted-foreground">{{ c.level || 'JP' }}</span>
            </div>
            
            <div class="absolute top-3 left-3 flex gap-2">
              <span v-if="c.level" class="px-2.5 py-1 bg-stitch-foreground text-white text-xs font-bold rounded-full">JLPT {{ c.level }}</span>
              <span :class="['px-2.5 py-1 text-xs font-bold rounded-full text-white', c.courseType === 'FREE' ? 'bg-green-500' : 'bg-stitch-primary']">
                {{ c.courseType === 'FREE' ? 'Miễn phí' : 'Trả phí' }}
              </span>
            </div>
          </div>
          
          <div class="p-5 flex flex-col flex-1">
            <h3 class="font-stitch-serif font-bold text-base mb-1.5 leading-snug text-stitch-card-foreground line-clamp-2 group-hover:text-stitch-primary transition-colors">{{ c.title }}</h3>
            <p class="text-xs text-stitch-muted-foreground mb-3 leading-relaxed line-clamp-2 flex-1">{{ c.shortDescription || 'Chưa có mô tả.' }}</p>
            
            <div class="flex items-center gap-2 text-xs text-stitch-muted-foreground mb-4 shrink-0">
              <span class="text-amber-500 font-medium">★ {{ c.averageRating?.toFixed(1) || '0.0' }}</span>
              <span v-if="c.totalStudents">({{ c.totalStudents.toLocaleString() }})</span>
              <span>•</span>
              <span>{{ c.totalLessons || 0 }} bài</span>
            </div>
            
            <div class="flex items-center justify-between pt-3 border-t border-stitch-border shrink-0">
              <div>
                <span v-if="c.courseType === 'FREE'" class="font-bold text-green-600 text-base">Miễn phí</span>
                <div v-else class="flex flex-col">
                  <span v-if="c.salePrice > 0 && c.salePrice < c.originalPrice" class="text-xs line-through text-stitch-muted-foreground">{{ formatPrice(c.originalPrice) }}</span>
                  <span class="font-bold text-base text-stitch-card-foreground">{{ formatPrice(c.salePrice > 0 ? c.salePrice : c.originalPrice) }}</span>
                </div>
              </div>
              <button class="px-4 py-2 bg-stitch-primary text-white text-xs font-semibold rounded-lg hover:bg-stitch-primary/90 transition-colors">
                {{ c.courseType === 'FREE' ? 'Học ngay' : 'Mua ngay' }}
              </button>
            </div>
          </div>
        </router-link>
      </div>

      <!-- Pagination -->
      <div v-if="!isLoading && !errorMsg && totalPages > 1" class="mt-12 flex justify-center items-center gap-4">
        <button 
          class="p-2 rounded-lg bg-white border border-stitch-border text-stitch-foreground hover:bg-stitch-muted disabled:opacity-50 disabled:cursor-not-allowed transition-colors" 
          :disabled="currentPage === 0" 
          @click="goToPage(currentPage - 1)"
          aria-label="Previous page"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m15 18-6-6 6-6"/></svg>
        </button>
        
        <span class="text-sm font-medium text-stitch-foreground">
          Trang {{ currentPage + 1 }} / {{ totalPages }}
        </span>
        
        <button 
          class="p-2 rounded-lg bg-white border border-stitch-border text-stitch-foreground hover:bg-stitch-muted disabled:opacity-50 disabled:cursor-not-allowed transition-colors" 
          :disabled="currentPage >= totalPages - 1" 
          @click="goToPage(currentPage + 1)"
          aria-label="Next page"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6"/></svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { CourseService } from '@/services/course.service'
import { getApiErrorMessage } from '@/utils/api-error'

const router = useRouter()
const route = useRoute()

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
  courseType: '',
  sort: 'id,desc'
})

const levels = [
  { label: 'Tất cả', value: '' },
  { label: 'N5', value: 'N5' },
  { label: 'N4', value: 'N4' },
  { label: 'N3', value: 'N3' },
  { label: 'N2', value: 'N2' },
  { label: 'N1', value: 'N1' }
]

const types = [
  { label: 'Tất cả', value: '' },
  { label: 'Miễn phí', value: 'FREE' },
  { label: 'Trả phí', value: 'PAID' }
]

const hasActiveFilters = computed(() => {
  return filters.keyword || filters.level || filters.courseType
})

// Sync state from URL
const syncFiltersFromUrl = () => {
  filters.keyword = route.query.keyword || ''
  searchInput.value = filters.keyword
  filters.level = route.query.level || ''
  filters.courseType = route.query.courseType || ''
  filters.sort = route.query.sort || 'id,desc'
  currentPage.value = parseInt(route.query.page) || 0
}

// Update URL without triggering full reload
const updateUrl = () => {
  const query = {}
  if (filters.keyword) query.keyword = filters.keyword
  if (filters.level) query.level = filters.level
  if (filters.courseType) query.courseType = filters.courseType
  if (filters.sort !== 'id,desc') query.sort = filters.sort
  if (currentPage.value > 0) query.page = currentPage.value

  router.replace({ query }).catch(() => {}) // Catch duplicate navigation error
}

const fetchCourses = async () => {
  isLoading.value = true
  errorMsg.value = ''

  try {
    const params = {
      page: currentPage.value,
      size: pageSize,
      sort: filters.sort
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
  updateUrl()
  fetchCourses()
}

const setFilter = (key, value) => {
  filters[key] = value
  currentPage.value = 0
  updateUrl()
  fetchCourses()
}

const onFilterChange = () => {
  currentPage.value = 0
  updateUrl()
  fetchCourses()
}

const clearAllFilters = () => {
  searchInput.value = ''
  filters.keyword = ''
  filters.level = ''
  filters.courseType = ''
  filters.sort = 'id,desc'
  currentPage.value = 0
  updateUrl()
  fetchCourses()
}

const goToPage = (page) => {
  if (page < 0 || page >= totalPages.value) return
  currentPage.value = page
  updateUrl()
  fetchCourses()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// Helpers
const formatPrice = (price) => {
  if (!price || price <= 0) return '0đ'
  return new Intl.NumberFormat('vi-VN').format(price) + 'đ'
}

const onImgError = (e) => {
  e.target.style.display = 'none'
}

// Watch for URL changes if user uses back/forward buttons
watch(() => route.query, (newQuery, oldQuery) => {
  // Simple check to avoid infinite loops if we triggered the change
  if (JSON.stringify(newQuery) !== JSON.stringify(oldQuery)) {
    syncFiltersFromUrl()
    fetchCourses()
  }
})

onMounted(() => {
  syncFiltersFromUrl()
  fetchCourses()
})
</script>
