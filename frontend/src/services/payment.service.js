import api from './api'

/**
 * Payment Service
 * Quản lý xử lý và giả lập thanh toán (Demo / Local simulation).
 */
export const PaymentService = {
  /**
   * POST /api/v1/payments/simulate/:orderId
   * Giả lập thanh toán thành công cho đơn hàng PENDING của học viên.
   * Lưu ý: Tính năng giả lập chỉ dùng cho môi trường test/demo và có thể bị tắt trên production.
   * @param {number|string} orderId
   */
  async simulatePayment(orderId) {
    return api.post(`/v1/payments/simulate/${orderId}`)
  }
}
