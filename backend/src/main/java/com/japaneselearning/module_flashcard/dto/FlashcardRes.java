package com.japaneselearning.module_flashcard.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardRes {
    private Long id;
    private Long deckId;
    private String frontText;
    private String frontReading;
    private String backMeaning;
    private String backExample;
    private String backExampleMeaning;
    private String level;
}
