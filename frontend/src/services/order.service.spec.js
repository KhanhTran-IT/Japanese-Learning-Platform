import { describe, it, expect, vi, beforeEach } from 'vitest'
import { OrderService } from './order.service'
import api from './api'

vi.mock('./api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  }
}))

describe('OrderService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('createOrder sends POST to /v1/orders with courseId payload only', async () => {
    const mockResponse = { data: { code: 1000, result: { id: 101, orderCode: 'ORD-101' } } }
    api.post.mockResolvedValue(mockResponse)

    const res = await OrderService.createOrder(42)

    expect(api.post).toHaveBeenCalledTimes(1)
    expect(api.post).toHaveBeenCalledWith('/v1/orders', { courseId: 42 })
    expect(res).toEqual(mockResponse)
  })

  it('getMyOrders sends GET to /v1/orders/me', async () => {
    const mockResponse = { data: { code: 1000, result: [{ id: 101, status: 'PENDING' }] } }
    api.get.mockResolvedValue(mockResponse)

    const res = await OrderService.getMyOrders()

    expect(api.get).toHaveBeenCalledTimes(1)
    expect(api.get).toHaveBeenCalledWith('/v1/orders/me')
    expect(res).toEqual(mockResponse)
  })

  it('getOrderById sends GET to /v1/orders/:id', async () => {
    const mockResponse = { data: { code: 1000, result: { id: 101, orderCode: 'ORD-101' } } }
    api.get.mockResolvedValue(mockResponse)

    const res = await OrderService.getOrderById(101)

    expect(api.get).toHaveBeenCalledTimes(1)
    expect(api.get).toHaveBeenCalledWith('/v1/orders/101')
    expect(res).toEqual(mockResponse)
  })
})
