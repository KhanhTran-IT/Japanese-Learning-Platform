import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import StudentDashboardPage from './StudentDashboardPage.vue'
import { StudentService } from '@/services/student.service'
import { QuizService } from '@/services/quiz.service'

// Mock vue-router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

// Mock StudentService & QuizService
vi.mock('@/services/student.service', () => ({
  StudentService: {
    getDashboardProgress: vi.fn(),
    getMyCourses: vi.fn()
  }
}))

vi.mock('@/services/quiz.service', () => ({
  QuizService: {
    getMyQuizAttempts: vi.fn()
  }
}))

describe('StudentDashboardPage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    
    StudentService.getDashboardProgress.mockResolvedValue({
      data: {
        code: 1000,
        result: {
          totalEnrolledCourses: 2,
          totalCompletedLessons: 10,
          overallProgressPercent: 50
        }
      }
    })
    
    StudentService.getMyCourses.mockResolvedValue({
      data: {
        code: 1000,
        result: [
          { courseId: 1, courseName: 'Course 1', lastLessonId: 5, progressPercent: 20 },
          { courseId: 2, courseName: 'Course 2', slug: 'course-2', progressPercent: 0 }
        ]
      }
    })

    QuizService.getMyQuizAttempts.mockResolvedValue({
      data: {
        code: 1000,
        result: []
      }
    })
  })

  it('renders correctly and fetches data', async () => {
    const wrapper = mount(StudentDashboardPage, {
      global: {
        stubs: ['router-link'],
        plugins: [
          createTestingPinia({
            initialState: {
              auth: { user: { fullName: 'Test User' } }
            }
          })
        ]
      }
    })

    // Initially loading
    expect(wrapper.text()).toContain('Đang tải dữ liệu học tập...')

    // Wait for data fetch
    await flushPromises()

    expect(wrapper.text()).not.toContain('Đang tải dữ liệu học tập...')
    expect(StudentService.getDashboardProgress).toHaveBeenCalled()
    expect(StudentService.getMyCourses).toHaveBeenCalled()
    
    // Check if courses are rendered
    expect(wrapper.text()).toContain('Course 1')
    expect(wrapper.text()).toContain('Course 2')
  })

  it('navigates to last lesson when course is clicked', async () => {
    const wrapper = mount(StudentDashboardPage, {
      global: {
        stubs: ['router-link'],
        plugins: [
          createTestingPinia({
            initialState: {
              auth: { user: { fullName: 'Test User' } }
            }
          })
        ]
      }
    })

    await flushPromises()

    // Find the first course which has lastLessonId: 5
    const courses = wrapper.findAll('.cursor-pointer')
    // The first .cursor-pointer might be a course item
    await courses[0].trigger('click')

    expect(mockPush).toHaveBeenCalledWith('/student/lessons/5')
  })

  it('navigates to course detail when course is clicked but no last lesson', async () => {
    const wrapper = mount(StudentDashboardPage, {
      global: {
        stubs: ['router-link'],
        plugins: [
          createTestingPinia({
            initialState: {
              auth: { user: { fullName: 'Test User' } }
            }
          })
        ]
      }
    })

    await flushPromises()

    const courses = wrapper.findAll('.cursor-pointer.group')
    // Click continue on the second course (no lastLessonId, has slug: 'course-2')
    await courses[1].trigger('click')

    expect(mockPush).toHaveBeenCalledWith('/courses/course-2')
  })
})
