<template>
  <div class="min-h-[calc(100vh-4rem)] bg-stitch-background py-8 md:py-12 px-4 sm:px-6 lg:px-8 font-stitch-sans">
    <div class="max-w-4xl mx-auto">
      
      <!-- Back Link -->
      <div class="mb-6">
        <router-link 
          to="/courses" 
          class="inline-flex items-center gap-1.5 text-sm font-medium text-stitch-muted-foreground hover:text-stitch-primary transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-1"
        >
          &larr; Khám phá thêm khóa học
        </router-link>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="flex flex-col items-center justify-center py-28 text-stitch-muted-foreground">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-stitch-primary mb-4"></div>
        <p class="text-sm font-medium">Đang tải thông tin đơn hàng...</p>
      </div>

      <!-- Not Found State -->
      <div v-else-if="isNotFound" class="bg-stitch-card rounded-2xl border border-stitch-border p-8 md:p-12 text-center shadow-sm">
        <div class="w-16 h-16 bg-red-100 text-red-600 rounded-full flex items-center justify-center mx-auto mb-4 text-2xl font-bold">
          ✕
        </div>
        <h2 class="font-stitch-serif font-bold text-2xl text-stitch-foreground mb-2">Không tìm thấy đơn hàng</h2>
        <p class="text-sm text-stitch-muted-foreground max-w-md mx-auto mb-6">
          Đơn hàng không tồn tại, đường dẫn không đúng hoặc đơn hàng đã bị xóa khỏi hệ thống.
        </p>
        <div class="flex flex-wrap items-center justify-center gap-3">
          <router-link 
            to="/courses" 
            class="px-6 py-2.5 bg-stitch-primary text-white text-sm font-semibold rounded-xl hover:bg-stitch-primary/90 transition-all shadow-sm active:scale-95"
          >
            Khám phá khóa học
          </router-link>
          <router-link 
            to="/student/my-courses" 
            class="px-6 py-2.5 border border-stitch-border text-stitch-foreground text-sm font-semibold rounded-xl hover:bg-stitch-muted transition-all"
          >
            Khóa học của tôi
          </router-link>
        </div>
      </div>

      <!-- Unauthorized / Access Denied State -->
      <div v-else-if="isUnauthorized" class="bg-stitch-card rounded-2xl border border-stitch-border p-8 md:p-12 text-center shadow-sm">
        <div class="w-16 h-16 bg-amber-100 text-amber-600 rounded-full flex items-center justify-center mx-auto mb-4 text-2xl font-bold">
          🔒
        </div>
        <h2 class="font-stitch-serif font-bold text-2xl text-stitch-foreground mb-2">Không có quyền truy cập</h2>
        <p class="text-sm text-stitch-muted-foreground max-w-md mx-auto mb-6">
          Bạn không có quyền xem hoặc thực hiện thanh toán cho đơn hàng của người khác. Vui lòng kiểm tra lại tài khoản.
        </p>
        <router-link 
          to="/student/my-courses" 
          class="inline-block px-6 py-2.5 bg-stitch-primary text-white text-sm font-semibold rounded-xl hover:bg-stitch-primary/90 transition-all shadow-sm active:scale-95"
        >
          Về khóa học của tôi
        </router-link>
      </div>

      <!-- General Error State -->
      <div v-else-if="errorMsg" class="bg-stitch-card rounded-2xl border border-stitch-border p-8 text-center shadow-sm">
        <div class="text-4xl mb-3">⚠️</div>
        <h2 class="font-stitch-serif font-bold text-xl text-stitch-foreground mb-2">Đã xảy ra lỗi</h2>
        <p class="text-sm text-stitch-muted-foreground mb-6">{{ errorMsg }}</p>
        <button 
          @click="fetchOrder" 
          class="px-6 py-2.5 bg-stitch-primary text-white text-sm font-semibold rounded-xl hover:bg-stitch-primary/90 transition-all active:scale-95"
        >
          Thử lại
        </button>
      </div>

      <!-- Main Checkout Content -->
      <template v-else-if="order">
        <!-- Page Title & Status Banner -->
        <div class="mb-8">
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <h1 class="font-stitch-serif text-2xl md:text-3xl font-bold text-stitch-foreground mb-1">
                Chi tiết thanh toán
              </h1>
              <p class="text-sm text-stitch-muted-foreground">
                Mã đơn hàng: <span class="font-mono font-semibold text-stitch-foreground">{{ order.orderCode }}</span>
              </p>
            </div>
            
            <div>
              <span 
                v-if="order.status === 'PAID'"
                class="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full text-xs font-bold bg-green-100 text-green-700 border border-green-200"
              >
                ✓ Đã thanh toán
              </span>
              <span 
                v-else-if="order.status === 'PENDING'"
                class="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full text-xs font-bold bg-amber-100 text-amber-700 border border-amber-200"
              >
                ⏳ Chờ thanh toán
              </span>
              <span 
                v-else
                class="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full text-xs font-bold bg-gray-100 text-gray-700 border border-gray-200"
              >
                {{ order.status }}
              </span>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8 items-start">
          
          <!-- Left Column: Order Items & Summary -->
          <div class="lg:col-span-2 space-y-6">
            
            <!-- Items Card -->
            <div class="bg-stitch-card rounded-2xl border border-stitch-border p-6 shadow-sm">
              <h2 class="font-stitch-serif font-bold text-lg text-stitch-foreground mb-4">
                Khóa học đã chọn
              </h2>

              <div v-if="order.items && order.items.length > 0" class="divide-y divide-stitch-border">
                <div 
                  v-for="item in order.items" 
                  :key="item.id"
                  class="py-4 first:pt-0 last:pb-0 flex items-start justify-between gap-4"
                >
                  <div class="flex-1">
                    <h3 class="font-semibold text-base text-stitch-foreground leading-snug mb-1">
                      {{ item.courseTitle }}
                    </h3>
                    <div class="text-xs text-stitch-muted-foreground">
                      Số lượng: {{ item.quantity || 1 }} &bull; Truy cập trọn đời
                    </div>
                  </div>
                  <div class="text-right shrink-0">
                    <div class="font-stitch-serif font-bold text-base text-stitch-foreground">
                      {{ formatPrice(item.price) }}
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="text-sm text-stitch-muted-foreground py-4 italic">
                Không có dữ liệu bài học trong đơn hàng này.
              </div>
            </div>

            <!-- Simulation Demo Notice Banner -->
            <div class="bg-amber-50/70 border border-amber-200/80 rounded-2xl p-5 text-amber-900 shadow-sm">
              <div class="flex items-start gap-3">
                <span class="text-xl shrink-0 mt-0.5">ℹ️</span>
                <div class="text-xs sm:text-sm leading-relaxed">
                  <span class="font-bold">Môi trường thử nghiệm (Local / Demo Simulation):</span>
                  Đây là tính năng mô phỏng thanh toán nội bộ phục vụ kiểm thử và demo. Hệ thống không kết nối cổng thanh toán thật (VNPay/MoMo) và không trừ tiền thực tế. Khi bấm xác nhận, đơn hàng sẽ ngay lập tức chuyển sang trạng thái <strong>PAID</strong> và tự động kích hoạt quyền học viên cho tài khoản của bạn.
                </div>
              </div>
            </div>

            <!-- Error Banner during simulation -->
            <div 
              v-if="simulationError" 
              class="bg-red-50 border border-red-200 text-red-700 p-4 rounded-xl text-sm flex items-start gap-3"
            >
              <span class="text-lg shrink-0">⚠️</span>
              <div>
                <div class="font-semibold mb-0.5">Không thể xử lý thanh toán</div>
                <div>{{ simulationError }}</div>
              </div>
            </div>

            <!-- Success Banner after simulation -->
            <div 
              v-if="paymentSuccessMsg" 
              class="bg-green-50 border border-green-200 text-green-700 p-5 rounded-xl text-sm flex items-start gap-3 shadow-sm"
            >
              <span class="text-2xl shrink-0">✅</span>
              <div>
                <div class="font-bold text-base mb-1">Thanh toán mô phỏng thành công!</div>
                <div class="leading-relaxed mb-3">
                  {{ paymentSuccessMsg }}
                </div>
                <div v-if="paymentResult" class="text-xs bg-white/80 p-2.5 rounded-lg border border-green-200/60 font-mono space-y-1 mb-3">
                  <div>Mã giao dịch: <strong class="text-green-800">{{ paymentResult.transactionCode }}</strong></div>
                  <div>Phương thức: <strong>{{ paymentResult.paymentMethod }}</strong></div>
                  <div>Thời gian: <strong>{{ formatDateTime(paymentResult.paidAt) }}</strong></div>
                </div>
                <button
                  @click="goToLearningFlow"
                  class="px-5 py-2.5 bg-green-600 hover:bg-green-700 text-white font-semibold text-xs rounded-lg transition-all shadow-sm active:scale-95 inline-flex items-center gap-1.5"
                >
                  ▶ Vào học ngay bây giờ
                </button>
              </div>
            </div>
          </div>

          <!-- Right Column: Price Summary & Action -->
          <div class="space-y-6">
            
            <!-- Summary Card -->
            <div class="bg-stitch-card rounded-2xl border border-stitch-border p-6 shadow-sm sticky top-24">
              <h2 class="font-stitch-serif font-bold text-lg text-stitch-foreground mb-4">
                Tổng cộng đơn hàng
              </h2>

              <div class="space-y-3 text-sm pb-4 border-b border-stitch-border">
                <div class="flex items-center justify-between text-stitch-muted-foreground">
                  <span>Tạm tính</span>
                  <span class="font-medium text-stitch-foreground">{{ formatPrice(order.totalAmount) }}</span>
                </div>
                
                <div v-if="order.discountAmount && Number(order.discountAmount) > 0" class="flex items-center justify-between text-green-600">
                  <span>Giảm giá</span>
                  <span>-{{ formatPrice(order.discountAmount) }}</span>
                </div>

                <div class="flex items-center justify-between text-xs text-stitch-muted-foreground">
                  <span>Ngày khởi tạo</span>
                  <span>{{ formatDateTime(order.createdAt) }}</span>
                </div>
              </div>

              <!-- Final Server-Authoritative Price -->
              <div class="py-4 flex items-baseline justify-between">
                <span class="font-semibold text-stitch-foreground">Tổng thanh toán</span>
                <span class="font-stitch-serif font-bold text-2xl text-stitch-primary">
                  {{ formatPrice(order.finalAmount) }}
                </span>
              </div>

              <!-- Actions based on Order Status -->
              <div class="pt-2">
                <!-- Case 1: PENDING -> Show Simulation Button -->
                <template v-if="order.status === 'PENDING'">
                  <button
                    @click="handleSimulatePayment"
                    :disabled="isSimulating"
                    class="w-full py-3.5 px-4 rounded-xl font-semibold text-sm transition-all shadow-md hover:shadow-lg active:scale-95 flex items-center justify-center gap-2"
                    :class="isSimulating 
                      ? 'bg-stitch-muted text-stitch-muted-foreground cursor-not-allowed' 
                      : 'bg-stitch-primary text-white hover:bg-stitch-primary/90'"
                  >
                    <span v-if="isSimulating" class="animate-spin rounded-full h-4 w-4 border-2 border-white/30 border-t-white"></span>
                    <span>{{ isSimulating ? 'Đang xử lý thanh toán mô phỏng...' : 'Mô phỏng thanh toán (Demo)' }}</span>
                  </button>
                  <p class="text-center text-xs text-stitch-muted-foreground mt-3">
                    Xác nhận giao dịch thanh toán giả lập trên môi trường thử nghiệm
                  </p>
                </template>

                <!-- Case 2: PAID -> Action is disabled / already paid flow -->
                <template v-else-if="order.status === 'PAID'">
                  <button
                    @click="goToLearningFlow"
                    class="w-full py-3.5 px-4 rounded-xl font-semibold text-sm transition-all bg-green-600 hover:bg-green-700 text-white shadow-md hover:shadow-lg active:scale-95 flex items-center justify-center gap-2"
                  >
                    ▶ Vào học ngay
                  </button>
                  <router-link
                    to="/student/my-courses"
                    class="block text-center mt-3 text-xs text-stitch-primary hover:underline font-medium"
                  >
                    Xem danh sách khóa học của tôi &rarr;
                  </router-link>
                </template>

                <!-- Case 3: Invalid state (CANCELLED / EXPIRED / FAILED) -->
                <template v-else>
                  <div class="p-3 bg-stitch-muted rounded-xl text-center text-xs text-stitch-muted-foreground mb-3 font-medium">
                    Đơn hàng này đang ở trạng thái <strong>{{ order.status }}</strong> và không thể thanh toán.
                  </div>
                  <router-link
                    to="/courses"
                    class="block w-full py-3 px-4 text-center rounded-xl font-semibold text-sm border border-stitch-border hover:bg-stitch-muted text-stitch-foreground transition-all"
                  >
                    Xem các khóa học khác
                  </router-link>
                </template>
              </div>

            </div>
          </div>

        </div>
      </template>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { OrderService } from '@/services/order.service'
import { PaymentService } from '@/services/payment.service'
import { StudentService } from '@/services/student.service'
import { getApiErrorMessage } from '@/utils/api-error'

const props = defineProps({
  orderId: {
    type: [String, Number],
    default: null
  }
})

const route = useRoute()
const router = useRouter()

const order = ref(null)
const isLoading = ref(true)
const isNotFound = ref(false)
const isUnauthorized = ref(false)
const errorMsg = ref('')

const isSimulating = ref(false)
const simulationError = ref('')
const paymentResult = ref(null)
const paymentSuccessMsg = ref('')

const getEffectiveOrderId = () => {
  return props.orderId || route.params.orderId
}

const fetchOrder = async () => {
  const id = getEffectiveOrderId()
  if (!id) {
    isNotFound.value = true
    isLoading.value = false
    return
  }

  isLoading.value = true
  isNotFound.value = false
  isUnauthorized.value = false
  errorMsg.value = ''
  simulationError.value = ''

  try {
    const res = await OrderService.getOrderById(id)
    if (res.data && res.data.code === 1000 && res.data.result) {
      order.value = res.data.result
    } else {
      isNotFound.value = true
    }
  } catch (err) {
    const status = err.response?.status
    const code = err.response?.data?.code

    if (status === 404 || code === 5002) {
      isNotFound.value = true
    } else if (status === 403 || code === 5005) {
      isUnauthorized.value = true
    } else {
      errorMsg.value = getApiErrorMessage(err, 'Không thể tải thông tin đơn hàng.')
    }
  } finally {
    isLoading.value = false
  }
}

const handleSimulatePayment = async () => {
  if (!order.value || isSimulating.value) return
  if (order.value.status !== 'PENDING') return

  isSimulating.value = true
  simulationError.value = ''
  paymentSuccessMsg.value = ''

  try {
    const res = await PaymentService.simulatePayment(order.value.id)
    if (res.data && res.data.code === 1000 && res.data.result) {
      paymentResult.value = res.data.result
      order.value.status = 'PAID'
      paymentSuccessMsg.value = 'Hệ thống đã kích hoạt quyền học viên và ghi danh bạn vào khóa học.'

      // Auto redirect to course learning flow after 2 seconds
      setTimeout(() => {
        goToLearningFlow()
      }, 2000)
    }
  } catch (err) {
    const code = err.response?.data?.code
    if (code === 5007) {
      simulationError.value = 'Tính năng giả lập thanh toán đang bị vô hiệu hóa trên môi trường này.'
    } else if (code === 5006) {
      simulationError.value = 'Trạng thái đơn hàng không hợp lệ để thực hiện thanh toán.'
    } else {
      simulationError.value = getApiErrorMessage(err, 'Thanh toán giả lập thất bại. Vui lòng thử lại.')
    }
  } finally {
    isSimulating.value = false
  }
}

/**
 * Route student to the learning flow of the purchased course.
 */
const goToLearningFlow = async () => {
  const targetCourseId = order.value?.items?.[0]?.courseId

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

  // Fallback to student my-courses page
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

onMounted(() => {
  fetchOrder()
})
</script>
