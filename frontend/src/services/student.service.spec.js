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
})
