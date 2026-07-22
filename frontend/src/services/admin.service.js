import api from './api'

export const AdminService = {
  /**
   * GET /api/v1/admin/dashboard
   * Trả về thống kê tổng quan
   */
  async getDashboardStats() {
    return api.get('/v1/admin/dashboard')
  },

  /**
   * GET /api/v1/admin/users
   * Lấy danh sách người dùng có phân trang và lọc
   */
  async getUsers(params) {
    return api.get('/v1/admin/users', { params })
  },

  /**
   * PUT /api/v1/admin/users/:id/lock
   * Khóa tài khoản người dùng
   */
  async lockUser(id) {
    return api.put(`/v1/admin/users/${id}/lock`)
  },

  /**
   * PUT /api/v1/admin/users/:id/unlock
   * Mở khóa tài khoản người dùng
   */
  async unlockUser(id) {
    return api.put(`/v1/admin/users/${id}/unlock`)
  },

  // ==========================================
  // COURSE MANAGEMENT
  // ==========================================
  
  /**
   * GET /api/v1/admin/courses
   * Lấy danh sách khóa học (phân trang)
   */
  async getCourses(params) {
    return api.get('/v1/admin/courses', { params })
  },

  /**
   * GET /api/v1/admin/courses/:id
   * Xem chi tiết 1 khóa học
   */
  async getCourseDetail(id) {
    return api.get(`/v1/admin/courses/${id}`)
  },

  /**
   * POST /api/v1/admin/courses
   * Tạo khóa học mới (trạng thái mặc định DRAFT)
   */
  async createCourse(payload) {
    return api.post('/v1/admin/courses', payload)
  },

  /**
   * PUT /api/v1/admin/courses/:id
   * Cập nhật thông tin khóa học
   */
  async updateCourse(id, payload) {
    return api.put(`/v1/admin/courses/${id}`, payload)
  },

  /**
   * DELETE /api/v1/admin/courses/:id
   * Xóa mềm khóa học (chuyển sang ARCHIVED)
   */
  async deleteCourse(id) {
    return api.delete(`/v1/admin/courses/${id}`)
  },

  /**
   * PUT /api/v1/admin/courses/:id/publish
   * Xuất bản khóa học (Yêu cầu phải có bài học)
   */
  async publishCourse(id) {
    return api.put(`/v1/admin/courses/${id}/publish`)
  },

  /**
   * PUT /api/v1/admin/courses/:id/hide
   * Ẩn khóa học
   */
  async hideCourse(id) {
    return api.put(`/v1/admin/courses/${id}/hide`)
  }
}
