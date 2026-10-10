import { mount } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import LeaderboardPage from './LeaderboardPage.vue';
import leaderboardService from '@/services/leaderboard.service';

vi.mock('@/services/leaderboard.service', () => ({
  default: {
    getLeaderboard: vi.fn()
  }
}));

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn()
  })
}));

describe('LeaderboardPage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders top 3 podium correctly and user list', async () => {
    leaderboardService.getLeaderboard.mockResolvedValue({
      data: {
        rankings: [
          { rank: 1, name: 'Alice', avatar: 'A', xp: 5000, level: 10, streak: 5, badge: '🥇', isMe: false },
          { rank: 2, name: 'Bob', avatar: 'B', xp: 4000, level: 8, streak: 3, badge: '🥈', isMe: false },
          { rank: 3, name: 'Charlie', avatar: 'C', xp: 3000, level: 6, streak: 2, badge: '🥉', isMe: true }
        ],
        currentUserRanking: { rank: 3, name: 'Charlie', avatar: 'C', xp: 3000, level: 6, streak: 2, badge: '🥉', isMe: true }
      }
    });

    const wrapper = mount(LeaderboardPage);
    
    // Initial state is loading
    expect(wrapper.find('.animate-spin').exists()).toBe(true);

    // Wait for data
    await new Promise(resolve => setTimeout(resolve, 0));
    
    // Check top 3 rendering
    expect(wrapper.text()).toContain('Alice');
    expect(wrapper.text()).toContain('Bob');
    expect(wrapper.text()).toContain('Charlie');
    
    // Check my ranking card
    expect(wrapper.text()).toContain('Charlie — Hạng #3 của bạn');
  });

  it('handles empty leaderboard gracefully', async () => {
    leaderboardService.getLeaderboard.mockResolvedValue({
      data: {
        rankings: [],
        currentUserRanking: null
      }
    });

    const wrapper = mount(LeaderboardPage);
    await new Promise(resolve => setTimeout(resolve, 0));
    
    expect(wrapper.text()).toContain('Chưa có dữ liệu bảng xếp hạng cho thời gian này.');
  });
});
