import { describe, it, expect, vi, beforeEach } from 'vitest'
import { LearningService } from './learning.service'
import api from './api'

vi.mock('./api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  }
}))

describe('LearningService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getLessonDetail calls GET /v1/lessons/:id and returns Axios response', async () => {
    const mockRes = { data: { code: 1000, result: { id: 10, title: 'Bài 1' } } }
    api.get.mockResolvedValue(mockRes)

    const res = await LearningService.getLessonDetail(10)

    expect(api.get).toHaveBeenCalledWith('/v1/lessons/10')
    expect(res).toEqual(mockRes)
  })

  it('updateProgress calls POST /v1/lessons/:id/progress and returns Axios response', async () => {
    const payload = { watchedPercent: 80, isCompleted: false }
    const mockRes = { data: { code: 1000, result: null } }
    api.post.mockResolvedValue(mockRes)

    const res = await LearningService.updateProgress(10, payload)

    expect(api.post).toHaveBeenCalledWith('/v1/lessons/10/progress', payload)
    expect(res).toEqual(mockRes)
  })

  it('completeLesson calls POST /v1/lessons/:id/complete and returns Axios response', async () => {
    const mockRes = { data: { code: 1000, result: null } }
    api.post.mockResolvedValue(mockRes)

    const res = await LearningService.completeLesson(10)

    expect(api.post).toHaveBeenCalledWith('/v1/lessons/10/complete')
    expect(res).toEqual(mockRes)
  })

  it('getLessonResources calls GET /v1/lessons/:id/resources and returns Axios response', async () => {
    const mockRes = { data: { code: 1000, result: [] } }
    api.get.mockResolvedValue(mockRes)

    const res = await LearningService.getLessonResources(10)

    expect(api.get).toHaveBeenCalledWith('/v1/lessons/10/resources')
    expect(res).toEqual(mockRes)
  })

  it('getLessonCurriculum calls GET /v1/lessons/:id/curriculum and returns Axios response', async () => {
    const mockRes = { data: { code: 1000, result: { sections: [] } } }
    api.get.mockResolvedValue(mockRes)

    const res = await LearningService.getLessonCurriculum(10)

    expect(api.get).toHaveBeenCalledWith('/v1/lessons/10/curriculum')
    expect(res).toEqual(mockRes)
  })
})
