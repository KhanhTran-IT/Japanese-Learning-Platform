import { describe, it, expect, vi, beforeEach } from 'vitest'
import { StudentService } from './student.service'
import api from './api'

vi.mock('./api', () => ({
  default: {
    get: vi.fn()
  }
}))

describe('StudentService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getDashboardProgress calls GET /users/me/progress and returns Axios response', async () => {
    const mockRes = { data: { code: 1000, result: { totalEnrolledCourses: 2 } } }
    api.get.mockResolvedValue(mockRes)

    const res = await StudentService.getDashboardProgress()

    expect(api.get).toHaveBeenCalledWith('/users/me/progress')
    expect(res).toEqual(mockRes)
  })

  it('getMyCourses calls GET /users/me/courses and returns Axios response', async () => {
    const mockRes = { data: { code: 1000, result: [{ courseId: 1, title: 'N5' }] } }
    api.get.mockResolvedValue(mockRes)

    const res = await StudentService.getMyCourses()

    expect(api.get).toHaveBeenCalledWith('/users/me/courses')
    expect(res).toEqual(mockRes)
  })

  it('getMyActivities calls GET /users/me/activities and adapts PageResponse result', async () => {
    const rawPageResponse = {
      currentPage: 0,
      pageSize: 10,
      totalPages: 1,
      totalElements: 1,
      data: [{ id: 101, activityType: 'LESSON_COMPLETE', xpEarned: 50, description: 'Completed Lesson 1' }]
    }
    const mockRes = { data: { code: 1000, result: rawPageResponse } }
    api.get.mockResolvedValue(mockRes)

    const res = await StudentService.getMyActivities()

    expect(api.get).toHaveBeenCalledWith('/users/me/activities')
    expect(Array.isArray(res.data.result)).toBe(true)
    expect(res.data.result.length).toBe(1)
    expect(res.data.result.currentPage).toBe(0)
    expect(res.data.result.pageSize).toBe(10)
    expect(res.data.result.totalElements).toBe(1)
    expect(res.data.result.data[0].activityType).toBe('LESSON_COMPLETE')
  })

  it('getMyActivities passes bounded pagination parameters', async () => {
    const rawPageResponse = {
      currentPage: 2,
      pageSize: 100,
      totalPages: 3,
      totalElements: 250,
      data: []
    }
    const mockRes = { data: { code: 1000, result: rawPageResponse } }
    api.get.mockResolvedValue(mockRes)

    await StudentService.getMyActivities({ page: -5, size: 999 })

    expect(api.get).toHaveBeenCalledWith('/users/me/activities', {
      params: { page: 0, size: 100 }
    })
  })
})
