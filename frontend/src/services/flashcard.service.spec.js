import { describe, it, expect, vi, beforeEach } from 'vitest'
import { FlashcardService } from './flashcard.service'
import api from './api'

vi.mock('./api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  }
}))

describe('FlashcardService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getUserDecks calls GET /v1/flashcards/decks and returns Axios response', async () => {
    const mockRes = { data: { code: 1000, result: [{ id: 1, name: 'Deck N5' }] } }
    api.get.mockResolvedValue(mockRes)

    const res = await FlashcardService.getUserDecks()

    expect(api.get).toHaveBeenCalledWith('/v1/flashcards/decks')
    expect(res).toEqual(mockRes)
  })

  it('getDueCards calls GET /v1/flashcards/decks/:deckId/study and returns Axios response', async () => {
    const mockRes = { data: { code: 1000, result: [{ id: 101, frontText: '食べる' }] } }
    api.get.mockResolvedValue(mockRes)

    const res = await FlashcardService.getDueCards(1)

    expect(api.get).toHaveBeenCalledWith('/v1/flashcards/decks/1/study')
    expect(res).toEqual(mockRes)
  })

  it('reviewCard calls POST /v1/flashcards/reviews with payload and returns Axios response', async () => {
    const payload = { flashcardId: 101, difficulty: 'EASY', idempotencyKey: 'key_1', timezone: 'UTC' }
    const mockRes = { data: { code: 1000, result: null } }
    api.post.mockResolvedValue(mockRes)

    const res = await FlashcardService.reviewCard(payload)

    expect(api.post).toHaveBeenCalledWith('/v1/flashcards/reviews', payload)
    expect(res).toEqual(mockRes)
  })
})
