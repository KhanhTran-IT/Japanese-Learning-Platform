import api from './api'

export const AdminService = {
  /**
   * GET /api/v1/admin/dashboard
   * Trả về thống kê tổng quan
   */
  async getDashboardStats() {
    return api.get('/v1/admin/dashboard')
  }
}
