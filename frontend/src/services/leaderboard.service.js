import api from './api'

/**
 * Leaderboard Service
 * Lấy bảng xếp hạng học viên theo thời gian (WEEKLY, MONTHLY, ALL_TIME).
 */
export const LeaderboardService = {
  /**
   * GET /api/v1/leaderboards
   * @param {string} [timeframe='ALL_TIME'] - 'WEEKLY' | 'MONTHLY' | 'ALL_TIME'
   * @param {number} [page=0]
   * @param {number} [size=50]
   */
  getLeaderboard(timeframe = 'ALL_TIME', page = 0, size = 50) {
    return api.get('/v1/leaderboards', {
      params: { timeframe, page, size }
    })
  }
}

export default LeaderboardService
