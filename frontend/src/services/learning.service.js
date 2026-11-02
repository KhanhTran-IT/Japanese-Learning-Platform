import api from './api'

/**
 * Learning Service
 * Quản lý bài học, tài nguyên học tập và tiến độ của học viên trong khóa học.
 */
export const LearningService = {
  /**
   * Lấy chi tiết bài học để học
   * GET /api/v1/lessons/{id}
   */
  getLessonDetail(id) {
    return api.get(`/v1/lessons/${id}`)
  },

  /**
   * Cập nhật tiến độ bài học
   * POST /api/v1/lessons/{id}/progress
   * @param {number|string} id
   * @param {Object} req - { watchedPercent: Number, isCompleted: Boolean }
   */
  updateProgress(id, req) {
    return api.post(`/v1/lessons/${id}/progress`, req)
  },

  /**
   * Đánh dấu hoàn thành bài học
   * POST /api/v1/lessons/{id}/complete
   * @param {number|string} id
   */
  completeLesson(id) {
    return api.post(`/v1/lessons/${id}/complete`)
  },

  /**
   * Lấy danh sách tài liệu đính kèm bài học
   * GET /api/v1/lessons/{lessonId}/resources
   * @param {number|string} lessonId
   */
  getLessonResources(lessonId) {
    return api.get(`/v1/lessons/${lessonId}/resources`)
  },

  /**
   * Lấy cây chương trình học để hiển thị curriculum sidebar
   * GET /api/v1/lessons/{lessonId}/curriculum
   * @param {number|string} lessonId
   */
  getLessonCurriculum(lessonId) {
    return api.get(`/v1/lessons/${lessonId}/curriculum`)
  }
}

export default LearningService
