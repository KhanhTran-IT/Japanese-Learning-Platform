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
})
