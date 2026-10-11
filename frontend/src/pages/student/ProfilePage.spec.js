import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import ProfilePage from './ProfilePage.vue'
import { AuthService } from '@/services/auth.service'

const mockPush = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

vi.mock('@/services/auth.service', () => ({
  AuthService: {
    updateCurrentUser: vi.fn(),
    changePassword: vi.fn(),
    logout: vi.fn()
  }
}))

describe('ProfilePage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    AuthService.updateCurrentUser.mockResolvedValue({
      data: {
        code: 1000,
        result: {
          id: 1,
          fullName: 'Test User',
          email: 'test@example.com',
          phone: '0900000000',
          avatarUrl: '',
          country: '🇯🇵',
          isPrivateLeaderboard: true,
          level: 4,
          currentStreak: 8,
          roles: ['STUDENT']
        }
      }
    })
  })

  const mountPage = () => mount(ProfilePage, {
    global: {
      plugins: [
        createTestingPinia({
          initialState: {
            auth: {
              user: {
                id: 1,
                fullName: 'Test User',
                email: 'test@example.com',
                phone: '0900000000',
                avatarUrl: '',
                country: '🇻🇳',
                isPrivateLeaderboard: false,
                level: 3,
                currentStreak: 7
              }
            }
          }
        })
      ]
    }
  })

  it('renders leaderboard profile fields from current user', () => {
    const wrapper = mountPage()

    expect(wrapper.text()).toContain('Quốc gia hiển thị')
    expect(wrapper.text()).toContain('Ẩn khỏi bảng xếp hạng')
    expect(wrapper.text()).toContain('Lv.3')
    expect(wrapper.text()).toContain('🔥 7 ngày')
    expect(wrapper.find('select').element.value).toBe('🇻🇳')
    expect(wrapper.find('input[type="checkbox"]').element.checked).toBe(false)
  })

  it('submits country and leaderboard privacy settings with profile update', async () => {
    const wrapper = mountPage()

    await wrapper.find('select').setValue('🇯🇵')
    await wrapper.find('input[type="checkbox"]').setValue(true)
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(AuthService.updateCurrentUser).toHaveBeenCalledWith(expect.objectContaining({
      fullName: 'Test User',
      phone: '0900000000',
      country: '🇯🇵',
      isPrivateLeaderboard: true
    }))
  })
})
