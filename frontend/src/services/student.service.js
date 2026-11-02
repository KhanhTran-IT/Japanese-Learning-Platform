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
  }
}

export default StudentService
