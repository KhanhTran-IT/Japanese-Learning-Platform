import api from './api';

class LeaderboardService {
  async getLeaderboard(timeframe = 'ALL_TIME', page = 0, size = 50) {
    const response = await api.get('/leaderboards', {
      params: { timeframe, page, size }
    });
    return response.data;
  }
}
// Fix
export default new LeaderboardService();
