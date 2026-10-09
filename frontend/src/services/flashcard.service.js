import api from './api';

const FlashcardService = {
  getUserDecks() {
    return api.get('/v1/flashcards/decks').then(res => res.data);
  },
  
  getDueCards(deckId) {
    return api.get(`/v1/flashcards/decks/${deckId}/study`).then(res => res.data);
  },
  
  reviewCard(data) {
    return api.post('/v1/flashcards/reviews', data).then(res => res.data);
  }
};

export default FlashcardService;
