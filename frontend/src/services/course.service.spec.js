import { describe, it, expect, vi, beforeEach } from 'vitest'
import { CourseService } from './course.service'
import api from './api'

vi.mock('./api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  }
}))

describe('CourseService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getCourses calls GET /v1/courses with params and returns Axios response', async () => {
    const mockRes = { data: { code: 1000, result: { content: [] } } }
    api.get.mockResolvedValue(mockRes)

    const res = await CourseService.getCourses({ page: 0, size: 10, level: 'N5' })

    expect(api.get).toHaveBeenCalledWith('/v1/courses', { params: { page: 0, size: 10, level: 'N5' } })
    expect(res).toEqual(mockRes)
  })

  it('getCourseBySlug calls GET /v1/courses/:slug and returns Axios response', async () => {
    const mockRes = { data: { code: 1000, result: { id: 1, title: 'N5' } } }
    api.get.mockResolvedValue(mockRes)

    const res = await CourseService.getCourseBySlug('khoa-hoc-n5')

    expect(api.get).toHaveBeenCalledWith('/v1/courses/khoa-hoc-n5')
    expect(res).toEqual(mockRes)
  })

  it('enrollFreeCourse calls POST /v1/courses/:id/enroll and returns Axios response', async () => {
    const mockRes = { data: { code: 1000, result: null } }
    api.post.mockResolvedValue(mockRes)

    const res = await CourseService.enrollFreeCourse(1)

    expect(api.post).toHaveBeenCalledWith('/v1/courses/1/enroll')
    expect(res).toEqual(mockRes)
  })
})
