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
    const validTimeframes = ['WEEKLY', 'MONTHLY', 'ALL_TIME']
    const safeTimeframe = (typeof timeframe === 'string' && validTimeframes.includes(timeframe.toUpperCase()))
      ? timeframe.toUpperCase()
      : 'ALL_TIME'
    const safePage = (typeof page === 'number' && Number.isInteger(page) && page >= 0) ? page : 0
    const safeSize = (typeof size === 'number' && Number.isInteger(size) && size > 0)
      ? Math.min(size, 100)
      : 50

    return api.get('/v1/leaderboards', {
      params: { timeframe: safeTimeframe, page: safePage, size: safeSize }
    })
  },

  /**
   * GET /api/v1/leaderboards/activities
   * Lấy lịch sử hoạt động học tập có phân trang của người dùng hiện tại
   * @param {number} [page=0]
   * @param {number} [size=10]
   */
  async getMyActivities(page = 0, size = 10) {
    const safePage = (typeof page === 'number' && Number.isInteger(page) && page >= 0) ? page : 0
    const safeSize = (typeof size === 'number' && Number.isInteger(size) && size > 0)
      ? Math.min(size, 100)
      : 10

    const response = await api.get('/v1/leaderboards/activities', {
      params: { page: safePage, size: safeSize }
    })

    if (response?.data?.result && !Array.isArray(response.data.result) && Array.isArray(response.data.result.data)) {
      const pageData = response.data.result
      const items = [...pageData.data]
      items.currentPage = pageData.currentPage
      items.pageSize = pageData.pageSize
      items.totalPages = pageData.totalPages
      items.totalElements = pageData.totalElements
      items.data = pageData.data
      response.data.result = items
    }
    return response
  }
}

export default LeaderboardService
