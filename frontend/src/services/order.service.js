import api from './api'

/**
 * Order Service
 * Quản lý đơn hàng khóa học trả phí cho học viên.
 */
export const OrderService = {
  /**
   * POST /api/v1/orders
   * Tạo đơn hàng mới cho khóa học trả phí (chỉ cần courseId, giá do server snapshot).
   * @param {number|string} courseId
   */
  async createOrder(courseId) {
    return api.post('/v1/orders', { courseId })
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
  async getOrderById(orderId) {
    return api.get(`/v1/orders/${orderId}`)
  }
}
