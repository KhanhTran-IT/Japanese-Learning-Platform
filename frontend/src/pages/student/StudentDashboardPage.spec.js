import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import StudentDashboardPage from './StudentDashboardPage.vue'
import { StudentService } from '@/services/student.service'

// Mock vue-router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

// Mock components
vi.mock('@/components/student/ProgressOverviewCard.vue', () => ({
  default: {
    name: 'ProgressOverviewCard',
    template: '<div class="mock-overview-card"></div>',
    props: ['label', 'value', 'icon', 'color', 'isPercent']
  }
}))

vi.mock('@/components/student/MyCourseCard.vue', () => ({
  default: {
    name: 'MyCourseCard',
    template: '<div class="mock-course-card"><button @click="$emit(\'continue\', course)">Continue</button></div>',
    props: ['course']
  }
}))

// Mock StudentService
vi.mock('@/services/student.service', () => ({
  StudentService: {
    getDashboardProgress: vi.fn(),
    getMyCourses: vi.fn()
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
          { courseId: 1, title: 'Course 1', lastLessonId: 5 },
          { courseId: 2, title: 'Course 2', slug: 'course-2' }
        ]
      }
    })
  })

  it('renders correctly and fetches data', async () => {
    const wrapper = mount(StudentDashboardPage, {
      global: {
        stubs: ['router-link']
      }
    })

    // Initially loading
    expect(wrapper.find('.loading-container').exists()).toBe(true)

    // Wait for data fetch
    await new Promise(resolve => setTimeout(resolve, 0))

    expect(wrapper.find('.loading-container').exists()).toBe(false)
    expect(StudentService.getDashboardProgress).toHaveBeenCalled()
    expect(StudentService.getMyCourses).toHaveBeenCalled()
    
    // Check if mocked cards are rendered
    const courseCards = wrapper.findAll('.mock-course-card')
    expect(courseCards.length).toBe(2)
  })

  it('navigates to last lesson when continue is clicked', async () => {
    const wrapper = mount(StudentDashboardPage, {
      global: {
        stubs: ['router-link']
      }
    })

    await new Promise(resolve => setTimeout(resolve, 0))

    const courseCards = wrapper.findAll('.mock-course-card')
    // Click continue on the first course (has lastLessonId: 5)
    await courseCards[0].find('button').trigger('click')

    expect(mockPush).toHaveBeenCalledWith('/student/lessons/5')
  })

  it('navigates to course detail when continue is clicked but no last lesson', async () => {
    const wrapper = mount(StudentDashboardPage, {
      global: {
        stubs: ['router-link']
      }
    })

    await new Promise(resolve => setTimeout(resolve, 0))

    const courseCards = wrapper.findAll('.mock-course-card')
    // Click continue on the second course (no lastLessonId, has slug: 'course-2')
    await courseCards[1].find('button').trigger('click')

    expect(mockPush).toHaveBeenCalledWith('/courses/course-2')
  })
})
