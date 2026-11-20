import api from './api'

/**
 * Student Service
 * Quản lý thông tin tiến độ học tập và khóa học đã ghi danh của học viên.
 */
export const StudentService = {
  /**
   * GET /api/users/me/progress
   * Trả về tổng quan tiến độ: totalEnrolledCourses, totalCompletedLessons, overallProgressPercent
   */
  getDashboardProgress() {
    return api.get('/users/me/progress')
  },

  /**
   * GET /api/users/me/courses
   * Trả về danh sách khóa học đã ghi danh kèm tiến độ từng khóa
   */
  getMyCourses() {
    return api.get('/users/me/courses')
  },

  /**
   * GET /api/users/me/activities
   * Trả về lịch sử hoạt động học tập có phân trang
   * @param {Object} [params] - { page: 0, size: 10 }
   */
  async getMyActivities(params) {
    const safePage = (params && typeof params.page === 'number' && Number.isInteger(params.page) && params.page >= 0)
      ? params.page
      : 0
    const safeSize = (params && typeof params.size === 'number' && Number.isInteger(params.size) && params.size > 0)
      ? Math.min(params.size, 100)
      : 10

    const queryParams = params !== undefined ? { page: safePage, size: safeSize } : undefined
    const response = queryParams !== undefined
      ? await api.get('/users/me/activities', { params: queryParams })
      : await api.get('/users/me/activities')

    if (response?.data?.result && !Array.isArray(response.data.result) && Array.isArray(response.data.result.data)) {
      const pageData = response.data.result
      const items = [...pageData.data]
      items.currentPage = pageData.currentPage
      items.pageSize = pageData.pageSize
      items.totalPages = pageData.totalPages
      items.totalElements = pageData.totalElements
      items.data = pageData.data
      response.data.result = items
    }
    return response
  }
}

export default StudentService
