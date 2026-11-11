import api from './api'

/**
 * Order Service
 * Quản lý đơn hàng khóa học trả phí cho học viên.
 */
export const OrderService = {
  /**
   * POST /api/v1/orders
   * Tạo đơn hàng mới cho khóa học trả phí (chỉ cần courseId, giá do server snapshot).
   * Hỗ trợ idempotencyKey cung cấp từ client (gửi cả trong payload và header Idempotency-Key).
   * @param {number|string|Object} courseIdOrPayload
   * @param {string} [idempotencyKey]
   */
  createOrder(courseIdOrPayload, idempotencyKey) {
    if (typeof courseIdOrPayload === 'object' && courseIdOrPayload !== null) {
      const payload = { ...courseIdOrPayload }
      const key = payload.idempotencyKey || idempotencyKey
      if (key) {
        payload.idempotencyKey = key
        return api.post('/v1/orders', payload, { headers: { 'Idempotency-Key': key } })
      }
      return api.post('/v1/orders', payload)
    }

    if (idempotencyKey) {
      return api.post(
        '/v1/orders',
        { courseId: courseIdOrPayload, idempotencyKey },
        { headers: { 'Idempotency-Key': idempotencyKey } }
      )
    }

    return api.post('/v1/orders', { courseId: courseIdOrPayload })
  },

  /**
   * GET /api/v1/orders/me
   * Lấy danh sách các đơn hàng của học viên đang đăng nhập (hỗ trợ pagination & backward compatibility).
   * @param {Object} [params] - { page, size }
   */
  async getMyOrders(params) {
    const response = await api.get('/v1/orders/me', { params })
    if (response?.data?.result && !Array.isArray(response.data.result) && Array.isArray(response.data.result.data)) {
      const pageData = response.data.result
      const items = [...pageData.data]
      items.currentPage = pageData.currentPage
      items.pageSize = pageData.pageSize
      items.totalPages = pageData.totalPages
      items.totalElements = pageData.totalElements
      items.data = pageData.data
      response.data.result = items
    }
    return response
  },

  /**
   * GET /api/v1/orders/:id
   * Lấy chi tiết đơn hàng theo ID (chỉ chủ đơn hàng mới xem được).
   * @param {number|string} orderId
   */
  getOrderById(orderId) {
    return api.get(`/v1/orders/${orderId}`)
  }
}

export default OrderService
