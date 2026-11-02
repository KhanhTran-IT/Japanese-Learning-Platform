import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QuizService } from './quiz.service'
import api from './api'

vi.mock('./api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  }
}))

describe('QuizService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getQuiz calls GET /v1/quizzes/:id and returns Axios response', async () => {
    const mockRes = { data: { code: 1000, result: { id: 1, title: 'Quiz 1' } } }
    api.get.mockResolvedValue(mockRes)

    const res = await QuizService.getQuiz(1)

    expect(api.get).toHaveBeenCalledWith('/v1/quizzes/1')
    expect(res).toEqual(mockRes)
  })

  it('startQuiz calls POST /v1/quizzes/:id/start and returns Axios response', async () => {
    const mockRes = { data: { code: 1000, result: { attemptId: 10 } } }
    api.post.mockResolvedValue(mockRes)

    const res = await QuizService.startQuiz(1)

    expect(api.post).toHaveBeenCalledWith('/v1/quizzes/1/start')
    expect(res).toEqual(mockRes)
  })

  it('submitQuiz calls POST /v1/quizzes/:id/submit with payload and returns Axios response', async () => {
    const payload = { attemptId: 10, answers: [] }
    const mockRes = { data: { code: 1000, result: { score: 100 } } }
    api.post.mockResolvedValue(mockRes)

    const res = await QuizService.submitQuiz(1, payload)

    expect(api.post).toHaveBeenCalledWith('/v1/quizzes/1/submit', payload)
    expect(res).toEqual(mockRes)
  })

  it('getQuizResult calls GET /v1/quizzes/:id/result/:attemptId and returns Axios response', async () => {
    const mockRes = { data: { code: 1000, result: { score: 80 } } }
    api.get.mockResolvedValue(mockRes)

    const res = await QuizService.getQuizResult(1, 10)

    expect(api.get).toHaveBeenCalledWith('/v1/quizzes/1/result/10')
    expect(res).toEqual(mockRes)
  })

  it('getLessonQuizzes calls GET /v1/lessons/:lessonId/quizzes and returns Axios response', async () => {
    const mockRes = { data: { code: 1000, result: [] } }
    api.get.mockResolvedValue(mockRes)

    const res = await QuizService.getLessonQuizzes(5)

    expect(api.get).toHaveBeenCalledWith('/v1/lessons/5/quizzes')
    expect(res).toEqual(mockRes)
  })

  it('getMyQuizAttempts calls GET /users/me/quiz-attempts and returns Axios response', async () => {
    const mockRes = { data: { code: 1000, result: [] } }
    api.get.mockResolvedValue(mockRes)

    const res = await QuizService.getMyQuizAttempts()

    expect(api.get).toHaveBeenCalledWith('/users/me/quiz-attempts')
    expect(res).toEqual(mockRes)
  })
})
