package com.japaneselearning.module_flashcard.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardDeckRes {
    private Long id;
    private String name;
    private String level;
    private String icon;
    private Long totalCards;
    private Long dueCards;
}
