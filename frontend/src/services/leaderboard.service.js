import api from './api';

class LeaderboardService {
  async getLeaderboard(timeframe = 'ALL_TIME', page = 0, size = 50) {
    const response = await api.get('/v1/leaderboards', {
      params: { timeframe, page, size }
    });
    return response.data?.result || { rankings: [], currentUserRanking: null };
  }
}

export default new LeaderboardService();
