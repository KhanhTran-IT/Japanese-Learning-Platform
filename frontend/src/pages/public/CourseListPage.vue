<template>
  <div class="flex flex-col lg:flex-row pt-4 min-h-[calc(100vh-64px)] bg-background font-body-md text-on-surface">
    <!-- Sidebar Navigation / Filter -->
    <aside class="hidden lg:flex flex-col w-[280px] bg-surface-container-low border-r border-paper-shadow p-6 flex-shrink-0">
      <div class="mb-8">
        <h2 class="font-headline-md text-headline-md text-primary mb-1">Cấp độ</h2>
        <p class="text-on-surface-variant font-body-md">Chọn lộ trình của bạn</p>
      </div>
      <div class="space-y-2 mb-8">
        <button 
          v-for="level in ['N5', 'N4', 'N3', 'N2', 'N1']" 
          :key="level"
          class="w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-colors"
          :class="filters.level === level ? 'bg-primary-container text-on-primary-container font-bold' : 'text-on-surface-variant hover:bg-surface-container-high'"
          @click="filters.level = (filters.level === level ? '' : level); onFilterChange()"
        >
          <span class="material-symbols-outlined">filter_{{ level.replace('N', '') }}</span>
          <span class="font-label-sm text-label-sm">JLPT {{ level }}</span>
        </button>
      </div>

      <!-- Detailed Filters -->
      <div class="space-y-6">
        <div>
          <p class="font-button text-button mb-3 text-secondary uppercase tracking-wider text-[11px]">Loại khóa học</p>
          <div class="space-y-2">
            <label class="flex items-center gap-3 cursor-pointer group">
              <input type="radio" value="" v-model="filters.courseType" @change="onFilterChange" class="rounded border-outline-variant text-primary focus:ring-primary h-5 w-5">
              <span class="text-body-md text-on-secondary-container group-hover:text-primary">Tất cả</span>
            </label>
            <label class="flex items-center gap-3 cursor-pointer group">
              <input type="radio" value="FREE" v-model="filters.courseType" @change="onFilterChange" class="rounded border-outline-variant text-primary focus:ring-primary h-5 w-5">
              <span class="text-body-md text-on-secondary-container group-hover:text-primary">Miễn phí</span>
            </label>
            <label class="flex items-center gap-3 cursor-pointer group">
              <input type="radio" value="PAID" v-model="filters.courseType" @change="onFilterChange" class="rounded border-outline-variant text-primary focus:ring-primary h-5 w-5">
              <span class="text-body-md text-on-secondary-container group-hover:text-primary">Trả phí</span>
            </label>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="flex-1 p-margin-mobile md:p-12 max-w-[1440px] min-w-0">
      <!-- Header & Prominent Search -->
      <div class="mb-12">
        <h1 class="font-headline-lg text-headline-lg-mobile md:text-headline-lg text-on-background mb-4">Khám phá lộ trình học tập</h1>
        <p class="text-body-lg text-on-surface-variant max-w-2xl mb-8">Nâng tầm kỹ năng tiếng Nhật của bạn với những khóa học được thiết kế bài bản, kết hợp tinh hoa thiền định và tính kỷ luật.</p>
        
        <!-- Search Bar Mobile/Tablet -->
        <div class="flex flex-col gap-4 mb-8">
          <div class="relative w-full md:w-96">
            <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-on-surface-variant">search</span>
            <input 
              class="w-full pl-12 pr-4 py-4 rounded-2xl bg-surface-container-lowest border border-paper-shadow focus:border-primary focus:ring-1 focus:ring-primary transition-all text-body-md" 
              placeholder="Tìm kiếm khóa học..." 
              type="text"
              v-model="searchInput"
              @keyup.enter="applySearch"
            >
          </div>
          <div v-if="hasActiveFilters" class="flex items-center gap-4">
            <span class="font-body-md text-secondary text-sm">
              {{ totalElements }} khóa học
              <template v-if="filters.keyword"> cho "{{ filters.keyword }}"</template>
            </span>
            <button @click="clearAllFilters" class="text-error font-button text-sm hover:underline">Xóa bộ lọc</button>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="flex flex-col justify-center items-center py-20 text-secondary">
        <span class="material-symbols-outlined animate-spin text-4xl mb-4">autorenew</span>
        <p class="font-body-md">Đang tải tinh hoa...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="errorMsg" class="flex flex-col justify-center items-center py-20 text-error">
        <span class="material-symbols-outlined text-4xl mb-4">error</span>
        <p class="font-body-md mb-4">{{ errorMsg }}</p>
        <button @click="fetchCourses" class="bg-primary text-on-primary px-6 py-2 rounded-xl font-button hover:opacity-90 transition-all">Thử lại</button>
      </div>

      <!-- Empty State -->
      <div v-else-if="courses.length === 0" class="flex flex-col justify-center items-center py-20 text-secondary">
        <span class="material-symbols-outlined text-4xl mb-4">inbox</span>
        <p class="font-body-md mb-4">Không tìm thấy khóa học nào phù hợp với tâm ý của bạn.</p>
        <button v-if="hasActiveFilters" @click="clearAllFilters" class="border border-outline text-primary px-6 py-2 rounded-xl font-button hover:bg-surface-container-low transition-all">Xóa bộ lọc</button>
      </div>

      <!-- Course Grid -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
        <!-- Course Card -->
        <div v-for="course in courses" :key="course.id" class="group card-lift bg-surface-container-lowest rounded-[16px] border border-paper-shadow overflow-hidden flex flex-col">
          <router-link :to="`/courses/${course.slug}`" class="block h-full flex flex-col">
            <div class="aspect-video relative overflow-hidden bg-surface-container-high">
              <img 
                v-if="course.thumbnailUrl" 
                class="w-full h-full object-cover transform group-hover:scale-105 transition-transform duration-500" 
                :src="course.thumbnailUrl" 
                :alt="course.title"
                @error="onImgError"
              >
              <div v-else class="w-full h-full flex items-center justify-center bg-secondary-container transform group-hover:scale-105 transition-transform duration-500">
                <span class="text-4xl text-on-secondary-container font-bold">{{ course.level || 'JP' }}</span>
              </div>
              
              <div v-if="course.level" class="absolute top-4 left-4 bg-primary text-white px-3 py-1 rounded-full text-label-sm font-label-sm">
                JLPT {{ course.level }}
              </div>
              <div v-if="course.courseType === 'FREE'" class="absolute top-4 right-4 bg-success-green text-white px-3 py-1 rounded-full text-label-sm font-label-sm">
                Miễn phí
              </div>
            </div>

            <div class="p-6 flex-1 flex flex-col">
              <h3 class="font-headline-md text-headline-md text-on-surface mb-2 line-clamp-2 group-hover:text-primary transition-colors">{{ course.title }}</h3>
              <p class="text-body-md text-on-surface-variant line-clamp-2 mb-6 flex-1">{{ course.shortDescription || 'Chưa có mô tả.' }}</p>
              
              <div class="mt-auto flex items-center justify-between border-t border-paper-shadow pt-4">
                <div class="flex items-center gap-1.5 text-on-secondary-container">
                  <span class="material-symbols-outlined text-[18px]">menu_book</span>
                  <span class="font-label-sm text-label-sm">{{ course.totalLessons || 0 }} bài</span>
                </div>
                
                <div class="font-headline-md text-[18px] flex items-center gap-2">
                  <template v-if="course.courseType === 'FREE'">
                    <span class="text-success-green">FREE</span>
                  </template>
                  <template v-else>
                    <span v-if="course.salePrice > 0 && course.salePrice < course.originalPrice" class="text-error">
                      {{ formatPrice(course.salePrice) }}
                    </span>
                    <span :class="course.salePrice > 0 && course.salePrice < course.originalPrice ? 'text-sm line-through text-on-surface-variant' : 'text-primary'">
                      {{ formatPrice(course.originalPrice) }}
                    </span>
                  </template>
                </div>
              </div>
            </div>
          </router-link>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="!isLoading && !errorMsg && totalPages > 1" class="mt-16 flex justify-center items-center gap-2">
        <button 
          class="p-2 rounded-lg hover:bg-surface-container-high text-on-surface-variant disabled:opacity-30" 
          :disabled="currentPage === 0" 
          @click="goToPage(currentPage - 1)"
        >
          <span class="material-symbols-outlined">chevron_left</span>
        </button>
        
        <span class="px-4 font-button text-button text-secondary">
          Trang {{ currentPage + 1 }} / {{ totalPages }}
        </span>
        
        <button 
          class="p-2 rounded-lg hover:bg-surface-container-high text-on-surface-variant disabled:opacity-30" 
          :disabled="currentPage >= totalPages - 1" 
          @click="goToPage(currentPage + 1)"
        >
          <span class="material-symbols-outlined">chevron_right</span>
        </button>
      </div>
    </main>
    
    <!-- Floating Action Button for Mobile Filter -->
    <button class="fixed bottom-8 right-8 lg:hidden bg-primary text-on-primary w-14 h-14 rounded-full shadow-lg flex items-center justify-center active:scale-95 transition-transform z-50">
      <span class="material-symbols-outlined">filter_list</span>
    </button>
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
