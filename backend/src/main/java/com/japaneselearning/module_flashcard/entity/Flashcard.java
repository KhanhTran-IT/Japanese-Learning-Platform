package com.japaneselearning.module_flashcard.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "flashcards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private FlashcardDeck deck;

    @Column(name = "front_text", nullable = false)
    private String frontText;

    @Column(name = "front_reading")
    private String frontReading;

    @Column(name = "back_meaning", nullable = false, length = 1000)
    private String backMeaning;

    @Column(name = "back_example", length = 1000)
    private String backExample;

    @Column(name = "back_example_meaning", length = 1000)
    private String backExampleMeaning;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
