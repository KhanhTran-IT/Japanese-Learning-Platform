import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import LessonLearningPage from './LessonLearningPage.vue'
import { LearningService } from '@/services/learning.service'

// Mock vue-router
vi.mock('vue-router', () => ({
  useRoute: () => ({
    params: { id: '1' }
  }),
  useRouter: () => ({
    push: vi.fn()
  })
}))

// Mock LearningService
vi.mock('@/services/learning.service', () => ({
  LearningService: {
    getLessonDetail: vi.fn(),
    updateProgress: vi.fn(),
    completeLesson: vi.fn(),
    getLessonResources: vi.fn(),
    getLessonCurriculum: vi.fn()
  }
}))

describe('LessonLearningPage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    
    LearningService.getLessonDetail.mockResolvedValue({
      data: {
        code: 1000,
        result: {
          id: 1,
          title: 'Test Lesson',
          content: 'Test content',
          durationMinutes: 10,
          isCompleted: false,
          watchedPercent: 0
        }
      }
    })
    
    LearningService.completeLesson.mockResolvedValue({
      data: {
        code: 1000,
        result: null
      }
    })
    
    LearningService.getLessonResources.mockResolvedValue({
      data: {
        code: 1000,
        result: []
      }
    })

    LearningService.getLessonCurriculum.mockResolvedValue({
      data: {
        code: 1000,
        result: {
          courseId: 1,
          courseTitle: 'Test Course',
          sections: [],
          previousLessonId: null,
          nextLessonId: null
        }
      }
    })
  })

  it('renders lesson detail correctly', async () => {
    const wrapper = mount(LessonLearningPage, {
      global: {
        stubs: ['router-link']
      }
    })

    // Wait for initial fetch
    await new Promise(resolve => setTimeout(resolve, 0))

    expect(wrapper.find('.lesson-title').text()).toBe('Test Lesson')
    expect(wrapper.find('.safe-content').text()).toBe('Test content')
    expect(wrapper.find('.progress-text').text()).toBe('0%')
  })

  it('updates state when lesson is marked as completed', async () => {
    const wrapper = mount(LessonLearningPage, {
      global: {
        stubs: ['router-link']
      }
    })

    await new Promise(resolve => setTimeout(resolve, 0))

    // Click complete button
    await wrapper.find('.btn-complete').trigger('click')
    
    // Wait for API call and state update
    await new Promise(resolve => setTimeout(resolve, 0))

    expect(LearningService.completeLesson).toHaveBeenCalledWith(1)
    
    // Check if state is updated
    expect(wrapper.find('.progress-text').text()).toBe('100%')
    const completeBtn = wrapper.find('.btn-complete')
    expect(completeBtn.text()).toContain('Đã hoàn thành')
    expect(completeBtn.attributes('disabled')).toBeDefined()
  })

  it('displays error if lesson detail fetch fails', async () => {
    LearningService.getLessonDetail.mockRejectedValue({
      response: {
        data: { message: 'Bạn chưa ghi danh khóa học này' },
        status: 403
      }
    })

    const wrapper = mount(LessonLearningPage, {
      global: {
        stubs: ['router-link']
      }
    })

    await new Promise(resolve => setTimeout(resolve, 0))

    const errorState = wrapper.find('.error-state')
    expect(errorState.exists()).toBe(true)
    expect(errorState.text()).toContain('Bạn chưa ghi danh khóa học này')
  })
})
