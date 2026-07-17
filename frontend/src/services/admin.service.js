import api from './api'

export const AdminService = {
  /**
   * GET /api/v1/admin/dashboard
   * Trả về thống kê tổng quan
   */
  async getDashboardStats() {
    try {
      const response = await api.get('/v1/admin/dashboard')
      return response
    } catch (error) {
      // Mock data in case the backend API is not yet implemented (returns 404)
      if (error.response && error.response.status === 404) {
        console.warn('Backend API /api/v1/admin/dashboard not found. Using mock data for UI demonstration.')
        return {
          data: {
            code: 1000,
            message: 'Success',
            result: {
              totalUsers: 156,
              totalCourses: 24,
              totalLessons: 142,
              totalEnrollments: 850,
              recentUsers: [
                { id: 1, fullName: 'Nguyễn Văn A', email: 'nva@test.com', role: 'STUDENT', createdAt: '2026-07-17T10:00:00Z' },
                { id: 2, fullName: 'Trần Thị B', email: 'ttb@test.com', role: 'TEACHER', createdAt: '2026-07-16T14:30:00Z' }
              ],
              recentCourses: [
                { id: 1, title: 'Luyện thi JLPT N3', teacherName: 'Sensei C', isPublished: true, createdAt: '2026-07-15T09:00:00Z' },
                { id: 2, title: 'Kaiwa cơ bản', teacherName: 'Sensei D', isPublished: false, createdAt: '2026-07-14T11:20:00Z' }
              ]
            }
          }
        }
      }
      throw error
    }
  }
}
