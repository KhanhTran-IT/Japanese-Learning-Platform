import api from './api'

/**
 * Quiz Service
 * Quản lý làm bài kiểm tra, nộp bài, tính điểm và lịch sử quiz của học viên.
 */
export const QuizService = {
  /**
   * Lấy chi tiết quiz cho student (questions + answers, không có isCorrect)
   * GET /api/v1/quizzes/{id}
   */
  getQuiz(id) {
    return api.get(`/v1/quizzes/${id}`)
  },

  /**
   * Bắt đầu làm bài - tạo attempt mới
   * POST /api/v1/quizzes/{id}/start
   * @param {number|string} id
   * @returns {{ attemptId, quizId, startedAt, status, maxAttempts, remainingAttempts }}
   */
  startQuiz(id) {
    return api.post(`/v1/quizzes/${id}/start`)
  },

  /**
   * Nộp bài làm quiz
   * POST /api/v1/quizzes/{id}/submit
   * @param {number|string} id - Quiz ID
   * @param {Object} payload - { attemptId: number, answers: [{ questionId, answerId?, userAnswerText? }] }
   */
  submitQuiz(id, payload) {
    return api.post(`/v1/quizzes/${id}/submit`, payload)
  },

  /**
   * Lấy kết quả chi tiết của một attempt
   * GET /api/v1/quizzes/{id}/result/{attemptId}
   * @param {number|string} id
   * @param {number|string} attemptId
   */
  getQuizResult(id, attemptId) {
    return api.get(`/v1/quizzes/${id}/result/${attemptId}`)
  },

  /**
   * Lấy danh sách quiz của một bài học
   * GET /api/v1/lessons/{lessonId}/quizzes
   * @param {number|string} lessonId
   */
  getLessonQuizzes(lessonId) {
    return api.get(`/v1/lessons/${lessonId}/quizzes`)
  },

  /**
   * Lấy lịch sử quiz attempt của student hiện tại (hỗ trợ phân trang và tương thích ngược)
   * GET /api/users/me/quiz-attempts
   * @param {Object} [params] - { page, size }
   */
  async getMyQuizAttempts(params) {
    const response = params !== undefined
      ? await api.get('/users/me/quiz-attempts', { params })
      : await api.get('/users/me/quiz-attempts')

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

export default QuizService
