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
