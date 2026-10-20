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
   * Lấy danh sách các đơn hàng của học viên đang đăng nhập.
   */
  async getMyOrders() {
    return api.get('/v1/orders/me')
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
