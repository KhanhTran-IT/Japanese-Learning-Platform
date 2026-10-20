import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import CheckoutPage from './CheckoutPage.vue'
import { createTestingPinia } from '@pinia/testing'
import { OrderService } from '@/services/order.service'
import { PaymentService } from '@/services/payment.service'
import { StudentService } from '@/services/student.service'
import { createRouter, createWebHistory } from 'vue-router'

vi.mock('@/services/order.service', () => ({
  OrderService: {
    getOrderById: vi.fn(),
    createOrder: vi.fn(),
    getMyOrders: vi.fn()
  }
}))

vi.mock('@/services/payment.service', () => ({
  PaymentService: {
    simulatePayment: vi.fn()
  }
}))

vi.mock('@/services/student.service', () => ({
  StudentService: {
    getMyCourses: vi.fn()
  }
}))

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div>Home</div>' } },
    { path: '/checkout/:orderId', name: 'Checkout', component: CheckoutPage },
    { path: '/courses', name: 'Courses', component: { template: '<div>Courses</div>' } },
    { path: '/student/my-courses', name: 'StudentMyCourses', component: { template: '<div>My Courses</div>' } },
    { path: '/student/lessons/:id', name: 'LessonLearning', component: { template: '<div>Lesson</div>' } }
  ]
})

describe('CheckoutPage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    router.currentRoute.value.params = { orderId: '101' }
  })

  const mountComponent = async (props = { orderId: 101 }) => {
    const pinia = createTestingPinia({ createSpy: vi.fn })
    const wrapper = mount(CheckoutPage, {
      props,
      global: {
        plugins: [pinia, router]
      }
    })
    await flushPromises()
    return wrapper
  }

  it('displays loading state initially', async () => {
    OrderService.getOrderById.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)))

    const pinia = createTestingPinia({ createSpy: vi.fn })
    const wrapper = mount(CheckoutPage, {
      props: { orderId: 101 },
      global: { plugins: [pinia, router], stubs: { RouterLink: true } }
    })

    expect(wrapper.find('.animate-spin').exists()).toBe(true)
    expect(wrapper.text()).toContain('Đang tải thông tin đơn hàng...')
  })

  it('displays not found state on 404 or code 5002', async () => {
    OrderService.getOrderById.mockRejectedValue({
      response: { status: 404, data: { code: 5002, message: 'Không tìm thấy đơn hàng' } }
    })

    const wrapper = await mountComponent({ orderId: 999 })

    expect(wrapper.text()).toContain('Không tìm thấy đơn hàng')
    expect(wrapper.text()).toContain('Khám phá khóa học')
  })

  it('displays unauthorized state on 403 or code 5005 (foreign order)', async () => {
    OrderService.getOrderById.mockRejectedValue({
      response: { status: 403, data: { code: 5005, message: 'Bạn không có quyền truy cập đơn hàng này' } }
    })

    const wrapper = await mountComponent({ orderId: 888 })

    expect(wrapper.text()).toContain('Không có quyền truy cập')
    expect(wrapper.text()).toContain('đơn hàng của người khác')
  })

  it('renders pending order snapshot details and local simulation action', async () => {
    const mockOrder = {
      id: 101,
      orderCode: 'ORD-20261020-001',
      status: 'PENDING',
      totalAmount: 500000,
      discountAmount: 0,
      finalAmount: 500000,
      createdAt: '2026-10-20T10:00:00',
      items: [
        {
          id: 1,
          courseId: 10,
          courseTitle: 'Khoá học N3 Cấp tốc',
          price: 500000,
          quantity: 1
        }
      ]
    }
    OrderService.getOrderById.mockResolvedValue({
      data: { code: 1000, result: mockOrder }
    })

    const wrapper = await mountComponent({ orderId: 101 })

    expect(wrapper.text()).toContain('ORD-20261020-001')
    expect(wrapper.text()).toContain('Khoá học N3 Cấp tốc')
    expect(wrapper.text()).toContain('Chờ thanh toán')
    expect(wrapper.text()).toMatch(/500[.,]000đ/)
    expect(wrapper.text()).toContain('Môi trường thử nghiệm (Local / Demo Simulation)')
    expect(wrapper.text()).toContain('Mô phỏng thanh toán (Demo)')
  })

  it('executes simulation payment on button click and updates to PAID state', async () => {
    const mockOrder = {
      id: 101,
      orderCode: 'ORD-20261020-001',
      status: 'PENDING',
      totalAmount: 500000,
      finalAmount: 500000,
      items: [{ id: 1, courseId: 10, courseTitle: 'Khoá học N3', price: 500000 }]
    }
    OrderService.getOrderById.mockResolvedValue({
      data: { code: 1000, result: mockOrder }
    })
    PaymentService.simulatePayment.mockResolvedValue({
      data: {
        code: 1000,
        result: {
          id: 501,
          orderId: 101,
          transactionCode: 'TXN-DEMO-101',
          paymentMethod: 'SIMULATION',
          status: 'SUCCESS',
          paidAt: '2026-10-20T10:05:00'
        }
      }
    })
    StudentService.getMyCourses.mockResolvedValue({
      data: { code: 1000, result: [{ courseId: 10, lastLessonId: 42 }] }
    })

    const wrapper = await mountComponent({ orderId: 101 })

    const simulateBtn = wrapper.findAll('button').find(b => b.text().includes('Mô phỏng thanh toán'))
    expect(simulateBtn).toBeTruthy()

    await simulateBtn.trigger('click')
    await flushPromises()

    expect(PaymentService.simulatePayment).toHaveBeenCalledWith(101)
    expect(wrapper.text()).toContain('Thanh toán mô phỏng thành công!')
    expect(wrapper.text()).toContain('TXN-DEMO-101')
    expect(wrapper.text()).toContain('Đã thanh toán')
  })

  it('handles simulation disabled error (code 5007)', async () => {
    const mockOrder = {
      id: 101,
      orderCode: 'ORD-101',
      status: 'PENDING',
      finalAmount: 500000,
      items: [{ id: 1, courseId: 10, courseTitle: 'N3', price: 500000 }]
    }
    OrderService.getOrderById.mockResolvedValue({
      data: { code: 1000, result: mockOrder }
    })
    PaymentService.simulatePayment.mockRejectedValue({
      response: {
        status: 403,
        data: { code: 5007, message: 'Tính năng giả lập thanh toán đang bị vô hiệu hóa' }
      }
    })

    const wrapper = await mountComponent({ orderId: 101 })

    const simulateBtn = wrapper.findAll('button').find(b => b.text().includes('Mô phỏng thanh toán'))
    await simulateBtn.trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('Tính năng giả lập thanh toán đang bị vô hiệu hóa')
  })

  it('renders already-paid order correctly and hides simulation action', async () => {
    const mockOrder = {
      id: 102,
      orderCode: 'ORD-PAID-001',
      status: 'PAID',
      finalAmount: 800000,
      items: [{ id: 2, courseId: 12, courseTitle: 'N2 Kanji Master', price: 800000 }]
    }
    OrderService.getOrderById.mockResolvedValue({
      data: { code: 1000, result: mockOrder }
    })

    const wrapper = await mountComponent({ orderId: 102 })

    expect(wrapper.text()).toContain('ORD-PAID-001')
    expect(wrapper.text()).toContain('Đã thanh toán')
    expect(wrapper.text()).toContain('Vào học ngay')
    // Simulation button should NOT exist
    const simBtn = wrapper.findAll('button').find(b => b.text().includes('Mô phỏng thanh toán'))
    expect(simBtn).toBeUndefined()
  })
})
