import api from './api'

/**
 * Payment Service
 * Quản lý khởi tạo thanh toán qua cổng chính thức (VNPay) và giả lập thanh toán (Demo / Local simulation).
 */
export const PaymentService = {
  /**
   * POST /api/v1/payments/create
   * Khởi tạo giao dịch thanh toán (VD: VNPay, Momo).
   * Trả về redirect URL sang cổng thanh toán.
   * @param {Object} payload { orderId, paymentMethod, returnUrl }
   */
  createPayment(payload) {
    return api.post('/v1/payments/create', payload)
  },

  /**
   * POST /api/v1/payments/simulate/:orderId
   * Giả lập thanh toán thành công cho đơn hàng PENDING của học viên.
   * Lưu ý: Tính năng giả lập chỉ dùng cho môi trường test/demo và có thể bị tắt trên production.
   * @param {number|string} orderId
   */
  simulatePayment(orderId) {
    return api.post(`/v1/payments/simulate/${orderId}`)
  }
}

export default PaymentService
