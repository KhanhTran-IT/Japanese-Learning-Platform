package com.japaneselearning.module_flashcard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewFlashcardReq {
    
    @NotNull(message = "flashcardId không được để trống")
    private Long flashcardId;
    
    @NotBlank(message = "difficulty không được để trống")
    @Size(max = 20, message = "difficulty không được vượt quá 20 ký tự")
    @Pattern(regexp = "^(?i)(EASY|MEDIUM|HARD)$", message = "Độ khó phải là EASY, MEDIUM hoặc HARD")
    private String difficulty; // "EASY", "MEDIUM", "HARD"
    
    @NotBlank(message = "idempotencyKey không được để trống")
    @Size(max = 255, message = "idempotencyKey không được vượt quá 255 ký tự")
    private String idempotencyKey;
    
    // ISO-8601 string, e.g., "2026-10-09T12:00:00Z"
    // Or just client timezone string like "Asia/Ho_Chi_Minh"
    @NotBlank(message = "timezone không được để trống")
    @Size(max = 64, message = "timezone không được vượt quá 64 ký tự")
    private String timezone; 
}
