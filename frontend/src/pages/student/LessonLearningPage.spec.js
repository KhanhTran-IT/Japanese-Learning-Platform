import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
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

// Mock Sidebar component because it's rendering too many things that we don't need to test here
const LearningCurriculumSidebarStub = {
  template: '<div class="sidebar-stub"></div>'
}

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
        stubs: {
          'router-link': true,
          'LearningCurriculumSidebar': LearningCurriculumSidebarStub
        }
      }
    })

    await flushPromises()

    // Title is rendered
    expect(wrapper.find('h1').text()).toContain('Test Lesson')
    
    // Content tab is active by default
    const contentTab = wrapper.findAll('button').find(b => b.text().includes('Nội dung'))
    expect(contentTab.classes()).toContain('border-stitch-accent')
    
    // Content is rendered
    expect(wrapper.text()).toContain('Test content')
  })

  it('updates state when lesson is marked as completed', async () => {
    const wrapper = mount(LessonLearningPage, {
      global: {
        stubs: {
          'router-link': true,
          'LearningCurriculumSidebar': LearningCurriculumSidebarStub
        }
      }
    })

    await flushPromises()

    // Switch to progress tab
    const progressTabBtn = wrapper.findAll('button').find(b => b.text().includes('Tiến độ'))
    await progressTabBtn.trigger('click')

    // Find and click complete button
    const buttons = wrapper.findAll('button')
    const completeBtn = buttons.find(b => b.text().includes('Đánh dấu xong bài học'))
    await completeBtn.trigger('click')
    
    await flushPromises()

    expect(LearningService.completeLesson).toHaveBeenCalledWith(1)
    
    // Check if state is updated
    const completedBtnAfter = wrapper.findAll('button').find(b => b.text().includes('Đã hoàn thành'))
    expect(completedBtnAfter.exists()).toBe(true)
    expect(completedBtnAfter.attributes('disabled')).toBeDefined()
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
        stubs: {
          'router-link': true,
          'LearningCurriculumSidebar': LearningCurriculumSidebarStub
        }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('Không thể truy cập')
    expect(wrapper.text()).toContain('Bạn chưa ghi danh khóa học này')
  })
})
