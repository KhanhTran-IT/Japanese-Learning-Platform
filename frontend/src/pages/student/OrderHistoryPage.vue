<template>
  <div class="max-w-6xl mx-auto space-y-6 font-stitch-sans">
    
    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 class="font-stitch-serif text-2xl md:text-3xl text-stitch-foreground font-bold mb-1">
          Lịch sử đơn hàng
        </h1>
        <p class="text-sm md:text-base text-stitch-muted-foreground">
          Quản lý toàn bộ đơn hàng khóa học, kiểm tra trạng thái và tiếp tục thanh toán.
        </p>
      </div>

      <!-- Action: Discover more courses -->
      <router-link
        to="/courses"
        class="inline-flex items-center justify-center gap-2 px-4 py-2.5 text-sm font-semibold rounded-xl border border-stitch-border hover:bg-stitch-muted text-stitch-foreground transition-all shadow-sm shrink-0 active:scale-95"
      >
        <span>Khám phá khóa học</span>
        <span>&rarr;</span>
      </router-link>
    </div>

    <!-- Filter Tabs (when orders loaded) -->
    <div v-if="!isLoading && !errorMsg && orders.length > 0" class="flex flex-wrap items-center gap-2 border-b border-stitch-border pb-3">
      <button
        v-for="tab in filterTabs"
        :key="tab.key"
        @click="activeFilter = tab.key"
        class="px-3.5 py-1.5 rounded-lg text-xs sm:text-sm font-medium transition-all flex items-center gap-1.5"
        :class="activeFilter === tab.key
          ? 'bg-stitch-primary text-white shadow-sm font-semibold'
          : 'bg-stitch-muted/60 text-stitch-muted-foreground hover:bg-stitch-muted hover:text-stitch-foreground'"
      >
        <span>{{ tab.label }}</span>
        <span 
          class="px-1.5 py-0.2 rounded-full text-[11px] font-bold"
          :class="activeFilter === tab.key ? 'bg-white/20 text-white' : 'bg-stitch-border text-stitch-muted-foreground'"
        >
          {{ tab.count }}
        </span>
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="flex flex-col items-center justify-center py-28 text-stitch-muted-foreground bg-stitch-card rounded-2xl border border-stitch-border p-8">
      <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-stitch-primary mb-4"></div>
      <p class="text-sm font-medium">Đang tải lịch sử đơn hàng...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="errorMsg" class="bg-red-50 border border-red-200 rounded-2xl p-8 text-center shadow-sm">
      <div class="text-4xl mb-3">⚠️</div>
      <h2 class="font-stitch-serif font-bold text-xl text-red-800 mb-2">Không thể tải danh sách đơn hàng</h2>
      <p class="text-sm text-red-700 max-w-md mx-auto mb-6">{{ errorMsg }}</p>
      <button
        @click="fetchOrders"
        class="px-6 py-2.5 bg-red-600 hover:bg-red-700 text-white text-sm font-semibold rounded-xl transition-all shadow-sm active:scale-95"
      >
        Thử lại
      </button>
    </div>

    <!-- Empty State: Zero orders total -->
    <div 
      v-else-if="orders.length === 0" 
      class="bg-stitch-card rounded-2xl border-2 border-dashed border-stitch-border p-12 md:p-16 text-center shadow-sm flex flex-col items-center justify-center"
    >
      <div class="w-16 h-16 bg-stitch-muted rounded-full flex items-center justify-center text-3xl mb-4 text-stitch-muted-foreground">
        🛒
      </div>
      <h2 class="font-stitch-serif font-bold text-xl md:text-2xl text-stitch-foreground mb-2">
        Bạn chưa có đơn hàng nào
      </h2>
      <p class="text-sm text-stitch-muted-foreground max-w-md mb-6">
        Tất cả các đơn hàng khóa học bạn từng đăng ký sẽ được lưu trữ an toàn và hiển thị tại đây.
      </p>
      <router-link
        to="/courses"
        class="px-6 py-3 bg-stitch-primary hover:bg-stitch-primary/90 text-white text-sm font-semibold rounded-xl transition-all shadow-md hover:shadow-lg active:scale-95"
      >
        Khám phá các khóa học ngay
      </router-link>
    </div>

    <!-- Empty Filter Result State -->
    <div 
      v-else-if="filteredOrders.length === 0" 
      class="bg-stitch-card rounded-2xl border border-stitch-border p-10 text-center shadow-sm"
    >
      <div class="text-3xl mb-3">🔍</div>
      <h3 class="font-stitch-serif font-bold text-lg text-stitch-foreground mb-1">
        Không có đơn hàng nào trong mục này
      </h3>
      <p class="text-xs sm:text-sm text-stitch-muted-foreground mb-4">
        Không tìm thấy đơn hàng nào tương ứng với bộ lọc đã chọn.
      </p>
      <button
        @click="activeFilter = 'ALL'"
        class="px-4 py-2 text-xs font-semibold rounded-lg bg-stitch-muted hover:bg-stitch-muted/80 text-stitch-foreground transition-all"
      >
        Xem tất cả đơn hàng
      </button>
    </div>

    <!-- Orders List -->
    <div v-else class="space-y-4">
      <div
        v-for="order in filteredOrders"
        :key="order.id"
        class="bg-stitch-card rounded-2xl border border-stitch-border p-5 sm:p-6 shadow-sm hover:border-stitch-primary/30 transition-all flex flex-col justify-between gap-5"
      >
        <!-- Card Top Bar: Code, Date, Status -->
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-stitch-border/70">
          <div class="flex items-center flex-wrap gap-2.5">
            <span class="text-xs text-stitch-muted-foreground">Mã đơn:</span>
            <span class="font-mono font-bold text-sm text-stitch-foreground bg-stitch-muted/80 px-2.5 py-0.5 rounded-md">
              {{ order.orderCode }}
            </span>
            <button
              @click="copyOrderCode(order.orderCode)"
              class="text-xs text-stitch-muted-foreground hover:text-stitch-primary transition-colors focus:outline-none"
              :title="copiedCode === order.orderCode ? 'Đã sao chép!' : 'Sao chép mã đơn hàng'"
            >
              <span v-if="copiedCode === order.orderCode" class="text-green-600 font-medium">✓ Đã sao chép</span>
              <span v-else class="underline">Sao chép</span>
            </button>
          </div>

          <div class="flex items-center gap-3">
            <span class="text-xs text-stitch-muted-foreground">
              {{ formatDateTime(order.createdAt) }}
            </span>

            <!-- Status Badge -->
            <span
              class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold border shrink-0"
              :class="getStatusBadgeClass(order.status)"
            >
              <span>{{ getStatusIcon(order.status) }}</span>
              <span>{{ getStatusLabel(order.status) }}</span>
            </span>
          </div>
        </div>

        <!-- Card Middle: Item Details (Server Snapshot) -->
        <div class="space-y-3">
          <div
            v-for="item in (order.items || [])"
            :key="item.id || item.courseId"
            class="flex items-start justify-between gap-4"
          >
            <div class="flex-1">
              <h3 class="font-semibold text-sm sm:text-base text-stitch-foreground leading-snug">
                {{ item.courseTitle }}
              </h3>
              <p class="text-xs text-stitch-muted-foreground mt-0.5">
                Số lượng: {{ item.quantity || 1 }} &bull; Quyền học trực tuyến trọn đời
              </p>
            </div>
            
            <div class="text-right shrink-0">
              <span class="font-stitch-serif font-bold text-sm sm:text-base text-stitch-foreground">
                {{ formatPrice(item.price) }}
              </span>
            </div>
          </div>

          <div v-if="!order.items || order.items.length === 0" class="text-xs text-stitch-muted-foreground italic py-1">
            Không có dữ liệu bài học trong đơn hàng này.
          </div>
        </div>

        <!-- Card Bottom Bar: Price Summary & Status-Aware Recovery Action -->
        <div class="pt-4 border-t border-stitch-border/70 flex flex-col md:flex-row md:items-center justify-between gap-4 bg-stitch-muted/20 -mx-5 -mb-5 sm:-mx-6 sm:-mb-6 p-4 sm:p-5 rounded-b-2xl">
          
          <!-- Server-Authoritative Price Breakdown -->
          <div class="flex flex-wrap items-baseline gap-x-4 gap-y-1 text-xs sm:text-sm">
            <div v-if="order.discountAmount && Number(order.discountAmount) > 0" class="text-stitch-muted-foreground">
              <span>Tạm tính: </span>
              <span class="font-medium text-stitch-foreground">{{ formatPrice(order.totalAmount) }}</span>
              <span class="text-green-600 ml-2 font-medium">(-{{ formatPrice(order.discountAmount) }})</span>
            </div>

            <div class="flex items-baseline gap-1.5">
              <span class="text-stitch-muted-foreground font-medium">Tổng thanh toán:</span>
              <span class="font-stitch-serif font-bold text-lg sm:text-xl text-stitch-primary">
                {{ formatPrice(order.finalAmount) }}
              </span>
            </div>
          </div>

          <!-- Status-Aware Actions -->
          <div class="flex flex-wrap items-center gap-2.5">
            
            <!-- PENDING: Resume Checkout -->
            <template v-if="order.status === 'PENDING'">
              <router-link
                :to="`/checkout/${order.id}`"
                class="px-5 py-2.5 bg-stitch-primary hover:bg-stitch-primary/90 text-white text-xs sm:text-sm font-semibold rounded-xl transition-all shadow-sm active:scale-95 inline-flex items-center gap-1.5"
              >
                <span>Tiếp tục thanh toán</span>
                <span>&rarr;</span>
              </router-link>
            </template>

            <!-- PAID: Safe Navigation to Learning or My Courses -->
            <template v-else-if="order.status === 'PAID'">
              <button
                @click="goToLearningFlow(order)"
                class="px-5 py-2.5 bg-green-600 hover:bg-green-700 text-white text-xs sm:text-sm font-semibold rounded-xl transition-all shadow-sm active:scale-95 inline-flex items-center gap-1.5"
              >
                <span>▶ Vào học ngay</span>
              </button>
              <router-link
                to="/student/my-courses"
                class="px-4 py-2 border border-stitch-border hover:bg-stitch-muted text-stitch-foreground text-xs sm:text-sm font-medium rounded-xl transition-all"
              >
                Khóa học của tôi
              </router-link>
            </template>

            <!-- CANCELLED, FAILED, EXPIRED, REFUNDED: Terminal status explanation & safe course discovery -->
            <template v-else>
              <div class="text-xs text-stitch-muted-foreground italic mr-2 text-right">
                {{ getTerminalStatusExplanation(order.status) }}
              </div>
              <router-link
                to="/courses"
                class="px-4 py-2 border border-stitch-border hover:bg-stitch-muted text-stitch-foreground text-xs font-semibold rounded-xl transition-all shrink-0"
              >
                Xem khóa học
              </router-link>
            </template>

          </div>

        </div>

      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { OrderService } from '@/services/order.service'
import { StudentService } from '@/services/student.service'
import { getApiErrorMessage } from '@/utils/api-error'

const router = useRouter()

const orders = ref([])
const isLoading = ref(true)
const errorMsg = ref('')
const activeFilter = ref('ALL')
const copiedCode = ref(null)

const fetchOrders = async () => {
  isLoading.value = true
  errorMsg.value = ''

  try {
    const res = await OrderService.getMyOrders()
    if (res.data && res.data.code === 1000) {
      orders.value = res.data.result || []
    } else {
      orders.value = []
    }
  } catch (err) {
    console.error('Fetch orders error:', err)
    errorMsg.value = getApiErrorMessage(err, 'Không thể tải danh sách đơn hàng. Vui lòng kiểm tra lại kết nối.')
  } finally {
    isLoading.value = false
  }
}

// Filter Tabs Configuration
const filterTabs = computed(() => {
  const allCount = orders.value.length
  const pendingCount = orders.value.filter(o => o.status === 'PENDING').length
  const paidCount = orders.value.filter(o => o.status === 'PAID').length
  const otherCount = orders.value.filter(o => !['PENDING', 'PAID'].includes(o.status)).length

  return [
    { key: 'ALL', label: 'Tất cả', count: allCount },
    { key: 'PENDING', label: 'Chờ thanh toán', count: pendingCount },
    { key: 'PAID', label: 'Đã thanh toán', count: paidCount },
    { key: 'OTHER', label: 'Khác', count: otherCount }
  ]
})

// Filtered orders based on selected tab
const filteredOrders = computed(() => {
  if (activeFilter.value === 'PENDING') {
    return orders.value.filter(o => o.status === 'PENDING')
  }
  if (activeFilter.value === 'PAID') {
    return orders.value.filter(o => o.status === 'PAID')
  }
  if (activeFilter.value === 'OTHER') {
    return orders.value.filter(o => !['PENDING', 'PAID'].includes(o.status))
  }
  return orders.value
})

/**
 * Route student to learning flow or My Courses fallback
 */
const goToLearningFlow = async (order) => {
  const targetCourseId = order?.items?.[0]?.courseId

  if (targetCourseId) {
    try {
      const myCoursesRes = await StudentService.getMyCourses()
      if (myCoursesRes.data && myCoursesRes.data.code === 1000) {
        const enrolled = myCoursesRes.data.result?.find(c => c.courseId === targetCourseId)
        if (enrolled?.lastLessonId) {
          router.push(`/student/lessons/${enrolled.lastLessonId}`)
          return
        }
      }
    } catch (err) {
      console.error('Error fetching enrolled courses for redirect:', err)
    }
  }

  router.push('/student/my-courses')
}

// Helpers
const formatPrice = (price) => {
  if (price === null || price === undefined || Number(price) <= 0) return '0đ'
  return new Intl.NumberFormat('vi-VN').format(price) + 'đ'
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  try {
    const d = new Date(dateStr)
    return d.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return dateStr
  }
}

const copyOrderCode = (code) => {
  if (!code) return
  if (navigator?.clipboard?.writeText) {
    navigator.clipboard.writeText(code)
    copiedCode.value = code
    setTimeout(() => {
      if (copiedCode.value === code) copiedCode.value = null
    }, 2000)
  }
}

const getStatusLabel = (status) => {
  switch (status) {
    case 'PENDING':
      return 'Chờ thanh toán'
    case 'PAID':
      return 'Đã thanh toán'
    case 'CANCELLED':
      return 'Đã hủy'
    case 'FAILED':
      return 'Thất bại'
    case 'EXPIRED':
      return 'Hết hạn'
    case 'REFUNDED':
      return 'Đã hoàn tiền'
    default:
      return status || 'Không xác định'
  }
}

const getStatusIcon = (status) => {
  switch (status) {
    case 'PENDING':
      return '⏳'
    case 'PAID':
      return '✓'
    case 'CANCELLED':
      return '✕'
    case 'FAILED':
      return '⚠️'
    case 'EXPIRED':
      return '⏰'
    case 'REFUNDED':
      return '↩'
    default:
      return 'ℹ️'
  }
}

const getStatusBadgeClass = (status) => {
  switch (status) {
    case 'PENDING':
      return 'bg-amber-100 text-amber-800 border-amber-200'
    case 'PAID':
      return 'bg-green-100 text-green-800 border-green-200'
    case 'CANCELLED':
      return 'bg-slate-100 text-slate-700 border-slate-200'
    case 'FAILED':
      return 'bg-red-100 text-red-700 border-red-200'
    case 'EXPIRED':
      return 'bg-zinc-100 text-zinc-700 border-zinc-200'
    case 'REFUNDED':
      return 'bg-purple-100 text-purple-700 border-purple-200'
    default:
      return 'bg-gray-100 text-gray-700 border-gray-200'
  }
}

const getTerminalStatusExplanation = (status) => {
  switch (status) {
    case 'CANCELLED':
      return 'Đơn hàng đã hủy'
    case 'FAILED':
      return 'Thanh toán không thành công'
    case 'EXPIRED':
      return 'Đơn hàng quá hạn'
    case 'REFUNDED':
      return 'Đã hoàn tiền'
    default:
      return 'Đơn hàng đã đóng'
  }
}

onMounted(() => {
  fetchOrders()
})
</script>
