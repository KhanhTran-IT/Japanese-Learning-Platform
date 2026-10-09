package com.japaneselearning.module_flashcard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewFlashcardReq {
    
    @NotNull
    private Long flashcardId;
    
    @NotBlank
    private String difficulty; // "EASY", "MEDIUM", "HARD"
    
    @NotBlank
    private String idempotencyKey;
    
    // ISO-8601 string, e.g., "2026-10-09T12:00:00Z"
    // Or just client timezone string like "Asia/Ho_Chi_Minh"
    @NotBlank
    private String timezone; 
}
