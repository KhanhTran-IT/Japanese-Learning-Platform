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

  it('getMyOrders sends GET to /v1/orders/me with params', async () => {
    const mockResponse = { data: { code: 1000, result: [{ id: 101, status: 'PENDING' }] } }
    api.get.mockResolvedValue(mockResponse)

    const res = await OrderService.getMyOrders({ page: 1, size: 5 })

    expect(api.get).toHaveBeenCalledTimes(1)
    expect(api.get).toHaveBeenCalledWith('/v1/orders/me', { params: { page: 1, size: 5 } })
    expect(res).toEqual(mockResponse)
  })

  it('getMyOrders adapts PageResponse result into array with pagination metadata', async () => {
    const pageResponse = {
      currentPage: 0,
      pageSize: 10,
      totalPages: 1,
      totalElements: 1,
      data: [{ id: 101, status: 'PENDING' }]
    }
    const mockResponse = { data: { code: 1000, result: pageResponse } }
    api.get.mockResolvedValue(mockResponse)

    const res = await OrderService.getMyOrders()

    expect(api.get).toHaveBeenCalledWith('/v1/orders/me', { params: undefined })
    expect(Array.isArray(res.data.result)).toBe(true)
    expect(res.data.result).toHaveLength(1)
    expect(res.data.result[0].id).toBe(101)
    expect(res.data.result.currentPage).toBe(0)
    expect(res.data.result.pageSize).toBe(10)
    expect(res.data.result.totalPages).toBe(1)
    expect(res.data.result.totalElements).toBe(1)
    expect(res.data.result.data).toEqual(pageResponse.data)
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
