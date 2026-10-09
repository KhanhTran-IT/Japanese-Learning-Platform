import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import QuizTakingPage from './QuizTakingPage.vue'
import { QuizService } from '@/services/quiz.service'

const mockPush = vi.fn()
let onBeforeRouteLeaveCallback = null

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { quizId: '1' } }),
  useRouter: () => ({ push: mockPush }),
  onBeforeRouteLeave: (cb) => { onBeforeRouteLeaveCallback = cb }
}))

vi.mock('@/services/quiz.service', () => ({
  QuizService: {
    getQuiz: vi.fn(),
    startQuiz: vi.fn(),
    submitQuiz: vi.fn()
  }
}))

describe('QuizTakingPage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    window.confirm = vi.fn(() => true)
    
    QuizService.getQuiz.mockResolvedValue({
      data: {
        code: 1000,
        result: {
          id: 1,
          title: 'Test Quiz',
          description: 'A mock quiz',
          timeLimitMinutes: 10,
          passingScore: 80,
          maxAttempts: 3,
          questions: [
            { id: 101, content: 'Q1', questionType: 'SINGLE_CHOICE', points: 10, answers: [{id: 1, content: 'A1'}, {id: 2, content: 'A2'}] },
            { id: 102, content: 'Q2', questionType: 'FILL_BLANK', points: 10, answers: [] }
          ]
        }
      }
    })
  })

  it('renders intro phase correctly', async () => {
    const wrapper = mount(QuizTakingPage, { global: { stubs: ['router-link'] } })
    await flushPromises()
    
    expect(wrapper.text()).toContain('Test Quiz')
    expect(wrapper.text()).toContain('2 câu hỏi')
    expect(wrapper.text()).toContain('10 phút')
    expect(wrapper.text()).toContain('Đạt: 80 điểm')
  })

  it('starts quiz and shows first question', async () => {
    QuizService.startQuiz.mockResolvedValue({
      data: { code: 1000, result: { attemptId: 999, startedAt: new Date().toISOString() } }
    })
    
    const wrapper = mount(QuizTakingPage, { global: { stubs: ['router-link'] } })
    await flushPromises()
    
    const startBtn = wrapper.findAll('button').find(b => b.text().includes('Bắt đầu làm bài'))
    await startBtn.trigger('click')
    await flushPromises()
    
    expect(QuizService.startQuiz).toHaveBeenCalledWith('1')
    
    // Quiz phase
    expect(wrapper.text()).toContain('Câu 1 (10 điểm)')
    expect(wrapper.text()).toContain('Q1')
    expect(wrapper.text()).toContain('A1')
  })

  it('submits quiz correctly', async () => {
    QuizService.startQuiz.mockResolvedValue({
      data: { code: 1000, result: { attemptId: 999, startedAt: new Date().toISOString() } }
    })
    QuizService.submitQuiz.mockResolvedValue({
      data: { code: 1000 }
    })
    
    const wrapper = mount(QuizTakingPage, { global: { stubs: ['router-link'] } })
    await flushPromises()
    
    await wrapper.findAll('button').find(b => b.text().includes('Bắt đầu làm bài')).trigger('click')
    await flushPromises()
    
    // Select answer for Q1
    const allButtons = wrapper.findAll('button')
    const a1Button = allButtons.find(b => b.text().includes('A1') || b.html().includes('A1'))
    await a1Button.trigger('click')
    
    // Next question
    await allButtons.find(b => b.text().includes('Câu tiếp theo')).trigger('click')
    
    // Q2
    expect(wrapper.text()).toContain('Q2')
    await wrapper.find('textarea').setValue('My answer')
    
    // Submit
    await wrapper.findAll('button').find(b => b.text().includes('Nộp bài')).trigger('click')
    await flushPromises()
    
    expect(QuizService.submitQuiz).toHaveBeenCalledWith('1', {
      attemptId: 999,
      answers: [
        { questionId: 101, answerId: 1 },
        { questionId: 102, userAnswerText: 'My answer' }
      ]
    })
    expect(mockPush).toHaveBeenCalledWith('/student/quizzes/1/result/999')
  })
})
