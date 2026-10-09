package com.japaneselearning.module_flashcard.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.module_flashcard.dto.FlashcardDeckRes;
import com.japaneselearning.module_flashcard.dto.FlashcardRes;
import com.japaneselearning.module_flashcard.dto.ReviewFlashcardReq;
import com.japaneselearning.module_flashcard.service.FlashcardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flashcards")
@RequiredArgsConstructor
@Tag(name = "Flashcard", description = "APIs for flashcard system (SRS)")
public class FlashcardController {

    private final FlashcardService flashcardService;

    @GetMapping("/decks")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get user flashcard decks", description = "Returns a list of decks with due card counts for the current user.")
    public ApiResponse<List<FlashcardDeckRes>> getUserDecks() {
        return ApiResponse.success("Lấy danh sách bộ thẻ thành công", flashcardService.getUserDecks());
    }

    @GetMapping("/decks/{deckId}/study")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get cards due for study", description = "Returns a batch of cards that are due for review or new.")
    public ApiResponse<List<FlashcardRes>> getDueCards(@PathVariable Long deckId) {
        return ApiResponse.success("Lấy danh sách thẻ thành công", flashcardService.getDueCards(deckId));
    }

    @PostMapping("/reviews")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Submit a flashcard review", description = "Submits review result (EASY/MEDIUM/HARD) and calculates next review interval.")
    public ApiResponse<Void> reviewCard(@Valid @RequestBody ReviewFlashcardReq req) {
        flashcardService.reviewCard(req);
        return ApiResponse.success("Lưu kết quả thành công", null);
    }
}
