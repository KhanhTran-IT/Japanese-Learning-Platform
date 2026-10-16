import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import MyCourseCard from './MyCourseCard.vue'

describe('MyCourseCard.vue', () => {
  const sampleCourse = {
    courseId: 10,
    courseName: 'Tiếng Nhật N5 Toàn Diện',
    level: 'N5',
    thumbnailUrl: null,
    lastLessonName: 'Bài 1: Bảng chữ cái Hiragana',
    totalLessons: 20,
    completedLessons: 5,
    progressPercent: 25
  }

  it('renders course information and progress properly', () => {
    const wrapper = mount(MyCourseCard, {
      props: { course: sampleCourse }
    })

    expect(wrapper.text()).toContain('Tiếng Nhật N5 Toàn Diện')
    expect(wrapper.text()).toContain('N5')
    expect(wrapper.text()).toContain('Bài tiếp theo: Bài 1: Bảng chữ cái Hiragana')
    expect(wrapper.text()).toContain('25% hoàn thành')
    expect(wrapper.text()).toContain('5/20 bài')
  })

  it('emits continue event on click and keyboard activation without nested buttons', async () => {
    const wrapper = mount(MyCourseCard, {
      props: { course: sampleCourse }
    })

    // Semantics check: outer is a focusable button role, no nested interactive buttons inside
    expect(wrapper.attributes('role')).toBe('button')
    expect(wrapper.attributes('tabindex')).toBe('0')
    expect(wrapper.findAll('button').length).toBe(0)

    // Click triggers continue
    await wrapper.trigger('click')
    expect(wrapper.emitted('continue')).toHaveLength(1)
    expect(wrapper.emitted('continue')[0]).toEqual([sampleCourse])

    // Keydown Enter triggers continue
    await wrapper.trigger('keydown.enter')
    expect(wrapper.emitted('continue')).toHaveLength(2)

    // Keydown Space triggers continue
    await wrapper.trigger('keydown.space')
    expect(wrapper.emitted('continue')).toHaveLength(3)
  })
})
