import api from './api'

/**
 * Public Course Service
 * Gọi các API public cho danh sách và chi tiết khóa học.
 */
export const CourseService = {
  /**
   * GET /api/v1/courses
   * Lấy danh sách khóa học PUBLISHED với filter & pagination.
   * @param {Object} params - { keyword, level, courseType, page, size }
   */
  async getCourses(params = {}) {
    return api.get('/v1/courses', { params })
  },

  /**
   * GET /api/v1/courses/:slug
   * Lấy chi tiết khóa học theo slug.
   */
  async getCourseBySlug(slug) {
    return api.get(`/v1/courses/${slug}`)
  },

  /**
   * POST /api/v1/courses/:courseId/enroll
   * Ghi danh vào khóa học miễn phí.
   */
  async enrollFreeCourse(courseId) {
    return api.post(`/v1/courses/${courseId}/enroll`)
  }
}
