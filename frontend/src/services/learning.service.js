import api from './api'

export const LearningService = {
  /**
   * Lấy chi tiết bài học để học
   * GET /api/v1/lessons/{id}
   */
  getLessonDetail(id) {
    return api.get(`/lessons/${id}`)
  },

  /**
   * Cập nhật tiến độ bài học
   * POST /api/v1/lessons/{id}/progress
   * @param {Object} req - { watchedPercent: Number, isCompleted: Boolean }
   */
  updateProgress(id, req) {
    return api.post(`/lessons/${id}/progress`, req)
  }
}
