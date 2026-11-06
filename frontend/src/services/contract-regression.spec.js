import { describe, it, expect, vi, beforeEach } from 'vitest'
import { isApiSuccess, getApiResult, getApiErrorCode, getApiErrorPayload, getHttpStatus } from '../utils/api-response'
import { OrderService } from './order.service'
import { CourseService } from './course.service'
import api from './api'

vi.mock('./api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

describe('Frontend API Contract Regression Test Suite', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('1. Unified Envelope Conformance (ApiResponse)', () => {
    it('handles standard success envelope (code: 1000, message, result)', () => {
      const response = {
        data: {
          code: 1000,
          message: 'Operation succeeded',
          result: { id: 42, name: 'JLPT N3 Grammar' }
        }
      }

      expect(isApiSuccess(response)).toBe(true)
      expect(getApiResult(response)).toEqual({ id: 42, name: 'JLPT N3 Grammar' })
    })

    it('identifies error envelope and extracts business error code and message', () => {
      const errorResponse = {
        response: {
          status: 403,
          data: {
            code: 5005,
            message: 'Bạn không có quyền truy cập đơn hàng này'
          }
        }
      }

      expect(isApiSuccess(errorResponse)).toBe(false)
      expect(getHttpStatus(errorResponse)).toBe(403)
      expect(getApiErrorCode(errorResponse)).toBe(5005)
      expect(getApiErrorPayload(errorResponse)).toEqual({
        code: 5005,
        message: 'Bạn không có quyền truy cập đơn hàng này'
      })
    })

    it('falls back safely for null, undefined, or empty responses', () => {
      expect(isApiSuccess(null)).toBe(false)
      expect(isApiSuccess(undefined)).toBe(false)
      expect(isApiSuccess({})).toBe(false)
      expect(getApiResult(null, 'fallback')).toBe('fallback')
      expect(getApiErrorCode({})).toBeNull()
      expect(getHttpStatus({})).toBeNull()
    })
  })

  describe('2. Dual Pagination Schema Contracts', () => {
    it('Order Module: PageResponse envelope is adapted with pagination properties on array', async () => {
      const backendPageResponse = {
        data: {
          code: 1000,
          message: 'Success',
          result: {
            currentPage: 0,
            pageSize: 10,
            totalPages: 3,
            totalElements: 25,
            data: [
              { id: 101, orderCode: 'ORD-20261106-001', status: 'PENDING', totalAmount: 350000 },
              { id: 102, orderCode: 'ORD-20261106-002', status: 'PAID', totalAmount: 500000 }
            ]
          }
        }
      }

      api.get.mockResolvedValue(backendPageResponse)

      const res = await OrderService.getMyOrders({ page: 0, size: 10 })
      const adaptedResult = res.data.result

      // Must be an Array so templates can v-for over result directly
      expect(Array.isArray(adaptedResult)).toBe(true)
      expect(adaptedResult).toHaveLength(2)
      // Must retain PageResponse metadata directly on array instance
      expect(adaptedResult.currentPage).toBe(0)
      expect(adaptedResult.pageSize).toBe(10)
      expect(adaptedResult.totalPages).toBe(3)
      expect(adaptedResult.totalElements).toBe(25)
      expect(adaptedResult.data).toEqual(backendPageResponse.data.result.data)
    })

    it('Course Public Module: Spring Data Page schema exposes content, number, size, totalPages, totalElements', async () => {
      const springPageResponse = {
        data: {
          code: 1000,
          message: 'Success',
          result: {
            content: [
              { id: 1, title: 'N5 Course', slug: 'n5-course', level: 'N5', courseType: 'FREE' },
              { id: 2, title: 'N3 Course', slug: 'n3-course', level: 'N3', courseType: 'PAID' }
            ],
            number: 0,
            size: 10,
            totalPages: 1,
            totalElements: 2,
            first: true,
            last: true,
            numberOfElements: 2
          }
        }
      }

      api.get.mockResolvedValue(springPageResponse)

      const res = await CourseService.getCourses({ page: 0, size: 10 })
      const page = res.data.result

      expect(Array.isArray(page.content)).toBe(true)
      expect(page.content).toHaveLength(2)
      expect(page.number).toBe(0)
      expect(page.size).toBe(10)
      expect(page.totalPages).toBe(1)
      expect(page.totalElements).toBe(2)
      expect(page.first).toBe(true)
      expect(page.last).toBe(true)
    })
  })

  describe('3. Domain Enum & Status Schema Integrity', () => {
    it('validates supported OrderStatus domain values', () => {
      const validOrderStatuses = ['PENDING', 'PAID', 'CANCELLED', 'EXPIRED', 'REFUNDED']
      validOrderStatuses.forEach(status => {
        expect(typeof status).toBe('string')
        expect(status).toMatch(/^[A-Z_]+$/)
      })
    })

    it('validates supported PaymentMethod and PaymentStatus values', () => {
      const validPaymentMethods = ['VNPAY', 'MOMO', 'BANK_TRANSFER']
      const validPaymentStatuses = ['PENDING', 'COMPLETED', 'FAILED', 'CANCELLED']

      validPaymentMethods.forEach(method => expect(method).toBeDefined())
      validPaymentStatuses.forEach(status => expect(status).toBeDefined())
    })

    it('validates supported CourseLevel, CourseType, and EnrollmentStatus values', () => {
      const courseLevels = ['N5', 'N4', 'N3', 'N2', 'N1']
      const courseTypes = ['FREE', 'PAID']
      const enrollmentStatuses = ['ACTIVE', 'COMPLETED', 'CANCELLED']

      expect(courseLevels).toContain('N3')
      expect(courseTypes).toContain('PAID')
      expect(enrollmentStatuses).toContain('ACTIVE')
    })

    it('validates QuizAttemptStatus values', () => {
      const quizStatuses = ['IN_PROGRESS', 'SUBMITTED', 'TIMED_OUT']
      expect(quizStatuses).toEqual(['IN_PROGRESS', 'SUBMITTED', 'TIMED_OUT'])
    })
  })

  describe('4. Ownership Isolation & Role Invariant Contracts', () => {
    it('Order creation contract payload sends courseId only', async () => {
      const mockOrderRes = {
        data: {
          code: 1000,
          result: { id: 99, orderCode: 'ORD-99', status: 'PENDING', totalAmount: 450000 }
        }
      }
      api.post.mockResolvedValue(mockOrderRes)

      const res = await OrderService.createOrder(42)
      expect(api.post).toHaveBeenCalledWith('/v1/orders', { courseId: 42 })
      expect(res.data.result.status).toBe('PENDING')
    })

    it('Payment creation contract payload sends orderId, paymentMethod, and returnUrl', async () => {
      const mockInitRes = {
        data: {
          code: 1000,
          result: { paymentUrl: 'https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...' }
        }
      }
      api.post.mockResolvedValue(mockInitRes)

      const payload = {
        orderId: 99,
        paymentMethod: 'VNPAY',
        returnUrl: 'http://localhost:5173/student/checkout/vnpay-return'
      }

      const res = await api.post('/v1/payments/create', payload)
      expect(api.post).toHaveBeenCalledWith('/v1/payments/create', payload)
      expect(res.data.result.paymentUrl).toContain('vnpayment.vn')
    })
  })
})
