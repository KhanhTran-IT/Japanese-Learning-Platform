import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import Footer from './Footer.vue'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div>Home</div>' } },
    { path: '/courses', component: { template: '<div>Courses</div>' } },
    { path: '/student/my-courses', component: { template: '<div>My Courses</div>' } },
    { path: '/student/flashcards', component: { template: '<div>Flashcards</div>' } },
    { path: '/student/leaderboard', component: { template: '<div>Leaderboard</div>' } }
  ]
})

describe('Footer.vue', () => {
  beforeEach(async () => {
    await router.push('/')
  })

  it('renders JLPT level links with correct query parameters', () => {
    const wrapper = mount(Footer, {
      global: {
        plugins: [router]
      }
    })

    const jlptLevels = ['N5', 'N4', 'N3', 'N2', 'N1']
    jlptLevels.forEach(level => {
      const link = wrapper.findAll('a').find(a => a.text() === `Khóa học ${level}`)
      expect(link).toBeDefined()
      expect(link.attributes('href')).toContain(`/courses?level=${level}`)
    })
  })

  it('renders practice links to student features', () => {
    const wrapper = mount(Footer, {
      global: {
        plugins: [router]
      }
    })

    const myCoursesLink = wrapper.findAll('a').find(a => a.text() === 'Khóa học của tôi')
    expect(myCoursesLink).toBeDefined()
    expect(myCoursesLink.attributes('href')).toContain('/student/my-courses')

    const flashcardsLink = wrapper.findAll('a').find(a => a.text() === 'Flashcards ôn tập')
    expect(flashcardsLink).toBeDefined()
    expect(flashcardsLink.attributes('href')).toContain('/student/flashcards')

    const leaderboardLink = wrapper.findAll('a').find(a => a.text() === 'Bảng xếp hạng')
    expect(leaderboardLink).toBeDefined()
    expect(leaderboardLink.attributes('href')).toContain('/student/leaderboard')
  })

  it('has disabled states for unlinked social buttons and contains no dead href="#" links', () => {
    const wrapper = mount(Footer, {
      global: {
        plugins: [router]
      }
    })

    // Social buttons are disabled
    const socialButtons = wrapper.findAll('button')
    expect(socialButtons.length).toBe(4)
    socialButtons.forEach(btn => {
      expect(btn.attributes('disabled')).toBeDefined()
      expect(btn.attributes('aria-disabled')).toBe('true')
    })

    // Check no href="#" anywhere in Footer
    const deadLinks = wrapper.findAll('a[href="#"]')
    expect(deadLinks.length).toBe(0)
  })
})
