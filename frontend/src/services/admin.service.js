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
  }
}
