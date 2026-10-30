import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import Navbar from './Navbar.vue'
import { createTestingPinia } from '@pinia/testing'
import { useAuthStore } from '@/stores/auth.store'
import { createRouter, createWebHistory } from 'vue-router'

vi.mock('@/services/auth.service', () => ({
  AuthService: {
    logout: vi.fn().mockResolvedValue({})
  }
}))

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div>Home</div>' } },
    { path: '/courses', component: { template: '<div>Courses</div>' } },
    { path: '/login', component: { template: '<div>Login</div>' } },
    { path: '/register', component: { template: '<div>Register</div>' } },
    { path: '/student/dashboard', component: { template: '<div>Student Dashboard</div>' } },
    { path: '/student/my-courses', component: { template: '<div>My Courses</div>' } },
    { path: '/student/flashcards', component: { template: '<div>Flashcards</div>' } },
    { path: '/student/leaderboard', component: { template: '<div>Leaderboard</div>' } },
    { path: '/student/profile', component: { template: '<div>Profile</div>' } },
    { path: '/student/orders', component: { template: '<div>Orders</div>' } },
    { path: '/admin/dashboard', component: { template: '<div>Admin Dashboard</div>' } }
  ]
})

describe('Navbar.vue', () => {
  beforeEach(async () => {
    vi.clearAllMocks()
    await router.push('/')
  })

  const mountNavbar = (authState = {}) => {
    const pinia = createTestingPinia({
      createSpy: vi.fn,
      initialState: {
        auth: {
          accessToken: null,
          user: null,
          initialized: true,
          ...authState
        }
      }
    })

    return mount(Navbar, {
      global: {
        plugins: [pinia, router]
      }
    })
  }

  it('renders public navigation (Trang chủ and Khóa học) for guests on desktop and mobile', async () => {
    const wrapper = mountNavbar({ isAuthenticated: false, user: null })

    // Desktop nav
    const desktopLinks = wrapper.findAll('nav a')
    const desktopTexts = desktopLinks.map(l => l.text())
    expect(desktopTexts).toContain('Trang chủ')
    expect(desktopTexts).toContain('Khóa học')
    expect(desktopTexts).not.toContain('Admin')

    // Open mobile menu
    const toggleBtn = wrapper.find('button[aria-label="Toggle navigation menu"]')
    expect(toggleBtn.exists()).toBe(true)
    await toggleBtn.trigger('click')

    // Mobile nav
    const mobileMenu = wrapper.find('.md\\:hidden.py-4')
    expect(mobileMenu.exists()).toBe(true)
    const mobileLinks = mobileMenu.findAll('a').map(l => l.text())
    expect(mobileLinks).toContain('Trang chủ')
    expect(mobileLinks).toContain('Khóa học')
    expect(mobileLinks).toContain('Đăng nhập')
    expect(mobileLinks).toContain('Đăng ký miễn phí')
  })

  it('displays student-specific links and hides admin actions for student users', async () => {
    const wrapper = mountNavbar({
      accessToken: 'mock-token',
      user: { id: 1, fullName: 'Nguyen Van A', roles: ['STUDENT'] }
    })

    const text = wrapper.text()
    expect(text).toContain('Khóa học của tôi')
    expect(text).toContain('Flashcards')
    expect(text).toContain('Bảng xếp hạng')
    expect(text).toContain('Đơn hàng')
    expect(text).not.toContain('Admin')

    // Open mobile menu
    await wrapper.find('button[aria-label="Toggle navigation menu"]').trigger('click')
    const mobileMenu = wrapper.find('.md\\:hidden.py-4')
    const mobileLinks = mobileMenu.findAll('a').map(l => l.text())
    expect(mobileLinks).toContain('Trang chủ')
    expect(mobileLinks).toContain('Khóa học')
    expect(mobileLinks).toContain('Dashboard')
    expect(mobileLinks).toContain('Khóa học của tôi')
    expect(mobileLinks).toContain('Flashcards')
    expect(mobileLinks).toContain('Bảng xếp hạng')
    expect(mobileLinks).toContain('Đơn hàng')
    expect(mobileLinks).toContain('Profile')
    expect(mobileLinks).not.toContain('Bảng quản trị')
  })

  it('hides student-only links (Flashcards, Leaderboard, My Courses, Profile) for admin users', async () => {
    const wrapper = mountNavbar({
      accessToken: 'mock-token',
      user: { id: 2, fullName: 'Admin User', roles: ['ADMIN'] }
    })

    // Desktop navigation checks
    const desktopNavText = wrapper.find('nav').text()
    expect(desktopNavText).toContain('Trang chủ')
    expect(desktopNavText).toContain('Khóa học')
    expect(desktopNavText).toContain('Admin')

    // Desktop auth container
    const desktopAuth = wrapper.find('.hidden.md\\:flex.items-center.gap-3')
    const desktopAuthText = desktopAuth.text()
    expect(desktopAuthText).toContain('Dashboard')
    expect(desktopAuthText).not.toContain('Khóa học của tôi')
    expect(desktopAuthText).not.toContain('Flashcards')
    expect(desktopAuthText).not.toContain('Bảng xếp hạng')
    expect(desktopAuthText).not.toContain('Đơn hàng')

    // Open mobile menu
    await wrapper.find('button[aria-label="Toggle navigation menu"]').trigger('click')
    const mobileMenu = wrapper.find('.md\\:hidden.py-4')
    const mobileLinks = mobileMenu.findAll('a').map(l => l.text())
    expect(mobileLinks).toContain('Trang chủ')
    expect(mobileLinks).toContain('Khóa học')
    expect(mobileLinks).toContain('Bảng quản trị')
    expect(mobileLinks).not.toContain('Khóa học của tôi')
    expect(mobileLinks).not.toContain('Flashcards')
    expect(mobileLinks).not.toContain('Bảng xếp hạng')
    expect(mobileLinks).not.toContain('Đơn hàng')
    expect(mobileLinks).not.toContain('Profile')
  })
})
