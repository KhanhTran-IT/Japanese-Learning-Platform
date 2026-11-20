import { describe, it, expect, vi, beforeEach } from 'vitest'
import { LeaderboardService } from './leaderboard.service'
import api from './api'

vi.mock('./api', () => ({
  default: {
    get: vi.fn()
  }
}))

describe('LeaderboardService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getLeaderboard calls GET /v1/leaderboards with default params and returns Axios response', async () => {
    const mockRes = {
      data: {
        code: 1000,
        result: {
          rankings: [{ rank: 1, name: 'Alice', xp: 5000 }],
          currentUserRanking: { rank: 1, name: 'Alice', xp: 5000 }
        }
      }
    }
    api.get.mockResolvedValue(mockRes)

    const res = await LeaderboardService.getLeaderboard()

    expect(api.get).toHaveBeenCalledWith('/v1/leaderboards', {
      params: { timeframe: 'ALL_TIME', page: 0, size: 50 }
    })
    expect(res).toEqual(mockRes)
  })

  it('getLeaderboard calls GET /v1/leaderboards with custom timeframe and pagination', async () => {
    const mockRes = { data: { code: 1000, result: { rankings: [], currentUserRanking: null } } }
    api.get.mockResolvedValue(mockRes)

    const res = await LeaderboardService.getLeaderboard('WEEKLY', 1, 20)

    expect(api.get).toHaveBeenCalledWith('/v1/leaderboards', {
      params: { timeframe: 'WEEKLY', page: 1, size: 20 }
    })
    expect(res).toEqual(mockRes)
  })

  it('getLeaderboard bounds invalid timeframe, negative page, and oversized size', async () => {
    const mockRes = { data: { code: 1000, result: { rankings: [] } } }
    api.get.mockResolvedValue(mockRes)

    await LeaderboardService.getLeaderboard('INVALID_TF', -5, 500)

    expect(api.get).toHaveBeenCalledWith('/v1/leaderboards', {
      params: { timeframe: 'ALL_TIME', page: 0, size: 100 }
    })
  })

  it('getMyActivities calls GET /v1/leaderboards/activities and adapts PageResponse result', async () => {
    const rawPage = {
      currentPage: 0,
      pageSize: 10,
      totalPages: 1,
      totalElements: 2,
      data: [
        { id: 2, activityType: 'QUIZ_SUBMIT', xpEarned: 100 },
        { id: 1, activityType: 'LESSON_COMPLETE', xpEarned: 50 }
      ]
    }
    const mockRes = { data: { code: 1000, result: rawPage } }
    api.get.mockResolvedValue(mockRes)

    const res = await LeaderboardService.getMyActivities()

    expect(api.get).toHaveBeenCalledWith('/v1/leaderboards/activities', {
      params: { page: 0, size: 10 }
    })
    expect(Array.isArray(res.data.result)).toBe(true)
    expect(res.data.result.length).toBe(2)
    expect(res.data.result.currentPage).toBe(0)
    expect(res.data.result.pageSize).toBe(10)
    expect(res.data.result.totalElements).toBe(2)
  })

  it('getMyActivities bounds invalid negative page and oversized size', async () => {
    const rawPage = {
      currentPage: 0,
      pageSize: 100,
      totalPages: 1,
      totalElements: 0,
      data: []
    }
    const mockRes = { data: { code: 1000, result: rawPage } }
    api.get.mockResolvedValue(mockRes)

    await LeaderboardService.getMyActivities(-1, 500)

    expect(api.get).toHaveBeenCalledWith('/v1/leaderboards/activities', {
      params: { page: 0, size: 100 }
    })
  })
})
