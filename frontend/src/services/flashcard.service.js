import api from './api'

/**
 * Flashcard Service
 * Quản lý bộ thẻ flashcard, học giãn cách (SRS) và ôn tập từ vựng.
 */
export const FlashcardService = {
  /**
   * GET /api/v1/flashcards/decks
   * Lấy danh sách bộ thẻ flashcard của học viên kèm số thẻ cần ôn.
   */
  getUserDecks() {
    return api.get('/v1/flashcards/decks')
  },

  /**
   * GET /api/v1/flashcards/decks/:deckId/study
   * Lấy danh sách các thẻ đến hạn cần ôn tập theo thuật toán SRS.
   * @param {number|string} deckId
   */
  getDueCards(deckId) {
    return api.get(`/v1/flashcards/decks/${deckId}/study`)
  },

  /**
   * POST /api/v1/flashcards/reviews
   * Lưu kết quả đánh giá thẻ (EASY/MEDIUM/HARD) và cập nhật khoảng cách ôn tập tiếp theo.
   * @param {Object} data - { flashcardId, difficulty, idempotencyKey, timezone }
   */
  reviewCard(data) {
    return api.post('/v1/flashcards/reviews', data)
  }
}

export default FlashcardService
