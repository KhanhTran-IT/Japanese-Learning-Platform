package com.japaneselearning.module_flashcard.service;

import com.japaneselearning.module_flashcard.dto.FlashcardDeckRes;
import com.japaneselearning.module_flashcard.dto.FlashcardRes;
import com.japaneselearning.module_flashcard.dto.ReviewFlashcardReq;

import java.util.List;

public interface FlashcardService {
    List<FlashcardDeckRes> getUserDecks();
    List<FlashcardRes> getDueCards(Long deckId);
    void reviewCard(ReviewFlashcardReq request);
}
