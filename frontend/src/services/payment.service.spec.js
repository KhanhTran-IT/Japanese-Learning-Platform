import { describe, it, expect, vi, beforeEach } from 'vitest'
import { PaymentService } from './payment.service'
import api from './api'

vi.mock('./api', () => ({
  default: {
    post: vi.fn()
  }
}))

describe('PaymentService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('createPayment sends POST to /v1/payments/create', async () => {
    const mockResponse = {
      data: {
        code: 1000,
        result: {
          orderId: 101,
          paymentUrl: 'https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=50000000',
          transactionCode: 'VNP-ORD-101',
          paymentMethod: 'VNPAY'
        }
      }
    }
    api.post.mockResolvedValue(mockResponse)

    const payload = {
      orderId: 101,
      paymentMethod: 'VNPAY',
      returnUrl: 'http://localhost:5173/student/checkout/vnpay-return'
    }
    const res = await PaymentService.createPayment(payload)

    expect(api.post).toHaveBeenCalledTimes(1)
    expect(api.post).toHaveBeenCalledWith('/v1/payments/create', payload)
    expect(res).toEqual(mockResponse)
  })

  it('simulatePayment sends POST to /v1/payments/simulate/:orderId', async () => {
    const mockResponse = {
      data: {
        code: 1000,
        result: {
          id: 55,
          orderId: 101,
          status: 'SUCCESS',
          transactionCode: 'TXN-101'
        }
      }
    }
    api.post.mockResolvedValue(mockResponse)

    const res = await PaymentService.simulatePayment(101)

    expect(api.post).toHaveBeenCalledTimes(1)
    expect(api.post).toHaveBeenCalledWith('/v1/payments/simulate/101')
    expect(res).toEqual(mockResponse)
  })
})
