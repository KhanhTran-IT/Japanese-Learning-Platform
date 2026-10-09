import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import FlashcardPage from './FlashcardPage.vue';
import FlashcardService from '@/services/flashcard.service';

vi.mock('@/services/flashcard.service');

describe('FlashcardPage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('loads and displays decks on mount', async () => {
    const mockDecks = [
      { id: 1, name: 'Deck 1', level: 'N5', totalCards: 100, dueCards: 5 },
      { id: 2, name: 'Deck 2', level: 'N4', totalCards: 50, dueCards: 0 }
    ];
    FlashcardService.getUserDecks.mockResolvedValue({ data: mockDecks });

    const wrapper = mount(FlashcardPage);
    await flushPromises();

    expect(FlashcardService.getUserDecks).toHaveBeenCalled();
    expect(wrapper.text()).toContain('Deck 1');
    expect(wrapper.text()).toContain('5 thẻ cần ôn hôm nay');
    expect(wrapper.text()).toContain('Deck 2');
  });

  it('starts studying when clicking on a deck with due cards', async () => {
    const mockDecks = [
      { id: 1, name: 'Deck 1', level: 'N5', totalCards: 100, dueCards: 2 }
    ];
    FlashcardService.getUserDecks.mockResolvedValue({ data: mockDecks });
    
    const mockCards = [
      { id: 101, deckId: 1, frontText: '食べる', backMeaning: 'ăn', level: 'N5' },
      { id: 102, deckId: 1, frontText: '飲む', backMeaning: 'uống', level: 'N5' }
    ];
    FlashcardService.getDueCards.mockResolvedValue({ data: mockCards });

    const wrapper = mount(FlashcardPage);
    await flushPromises();

    // Click start study button
    const studyBtn = wrapper.find('button.bg-primary');
    expect(studyBtn.exists()).toBe(true);
    await studyBtn.trigger('click');
    await flushPromises();

    expect(FlashcardService.getDueCards).toHaveBeenCalledWith(1);
    
    // Check if first card is rendered
    expect(wrapper.text()).toContain('食べる');
    // Should not show answer buttons initially
    const answerButtons = wrapper.findAll('button').filter(b => ['Khó', 'Bình thường', 'Dễ'].includes(b.text()));
    expect(answerButtons.length).toBe(0);
  });

  it('submits review and moves to next card', async () => {
    const mockDecks = [
      { id: 1, name: 'Deck 1', level: 'N5', totalCards: 100, dueCards: 2 }
    ];
    FlashcardService.getUserDecks.mockResolvedValue({ data: mockDecks });
    
    const mockCards = [
      { id: 101, deckId: 1, frontText: '食べる', backMeaning: 'ăn', level: 'N5' },
      { id: 102, deckId: 1, frontText: '飲む', backMeaning: 'uống', level: 'N5' }
    ];
    FlashcardService.getDueCards.mockResolvedValue({ data: mockCards });
    FlashcardService.reviewCard.mockResolvedValue({ data: null });

    const wrapper = mount(FlashcardPage);
    await flushPromises();

    // Enter study mode
    await wrapper.find('button.bg-primary').trigger('click');
    await flushPromises();

    // Flip card
    await wrapper.find('.cursor-pointer').trigger('click');
    await flushPromises();

    // Answer EASY
    const easyBtn = wrapper.findAll('button').filter(b => b.text() === 'Dễ')[0];
    await easyBtn.trigger('click');
    await flushPromises();

    expect(FlashcardService.reviewCard).toHaveBeenCalledWith(expect.objectContaining({
      flashcardId: 101,
      difficulty: 'EASY'
    }));

    // Should move to next card
    expect(wrapper.text()).toContain('飲む');
  });
});
