package com.japaneselearning;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlashcardDueCardIndexMigrationH2IT {

    @Test
    @DisplayName("Flyway V4 + V13 + V21: Verify flashcard performance indexes and due-card query plan on H2/MySQL")
    void flashcardPerformanceIndexesAndDueCardQueryPlan_AreVerified() throws Exception {
        DataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:flashcard_due_index_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );

        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/migration/V1__init_schema.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/migration/V4__create_flashcard_tables.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/migration/V5__seed_flashcard_data.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/migration/V13__add_flashcard_performance_indexes.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/migration/V21__scope_flashcard_review_idempotency_to_user.sql"));

            // 1. Verify expected indexes and column ordering in INFORMATION_SCHEMA
            assertThat(getIndexColumns(conn, "flashcards", "idx_flashcards_deck_id"))
                    .containsExactly("deck_id");
            assertThat(getIndexColumns(conn, "flashcards", "idx_flashcards_deck_id_asc"))
                    .containsExactly("deck_id", "id");

            assertThat(getIndexColumns(conn, "flashcard_progress", "idx_flashcard_progress_user_deck"))
                    .containsExactly("user_id", "deck_id");
            assertThat(getIndexColumns(conn, "flashcard_progress", "idx_flashcard_progress_due"))
                    .containsExactly("user_id", "deck_id", "next_review_time");
            assertThat(getIndexColumns(conn, "flashcard_progress", "idx_flashcard_progress_user_card_due"))
                    .containsExactly("user_id", "flashcard_id", "next_review_time");
            assertThat(getUniqueConstraintColumns(conn, "flashcard_progress", "uk_user_flashcard"))
                    .containsExactly("user_id", "flashcard_id");

            // 2. Verify EXPLAIN plan for the database-side due-card query uses our indexes
            try (PreparedStatement explainStmt = conn.prepareStatement(
                    "EXPLAIN SELECT f.id, f.deck_id, f.front_text, f.front_reading, f.back_meaning, " +
                    "f.back_example, f.back_example_meaning " +
                    "FROM flashcards f " +
                    "INNER JOIN flashcard_decks d ON d.id = f.deck_id " +
                    "LEFT JOIN flashcard_progress fp ON fp.flashcard_id = f.id AND fp.user_id = ? " +
                    "WHERE f.deck_id = ? AND (fp.id IS NULL OR fp.next_review_time <= ?) " +
                    "ORDER BY f.id ASC LIMIT 20"
            )) {
                explainStmt.setLong(1, 1L);
                explainStmt.setLong(2, 1L);
                explainStmt.setTimestamp(3, Timestamp.from(Instant.parse("2026-11-28T13:00:00Z")));

                try (ResultSet rs = explainStmt.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    String plan = rs.getString(1).toLowerCase();
                    assertThat(plan).contains("flashcards");
                    assertThat(plan).contains("flashcard_progress");
                    assertThat(plan).doesNotContain("tablescan");
                    assertThat(plan).containsAnyOf("idx_flashcards_deck_id_asc", "idx_flashcards_deck_id", "fk_flashcards_deck_index");
                    assertThat(plan).containsAnyOf("idx_flashcard_progress_user_card_due", "uk_user_flashcard");
                }
            }
        }
    }

    private List<String> getIndexColumns(Connection conn, String tableName, String indexName) throws Exception {
        List<String> columns = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT LOWER(column_name) AS col_name " +
                "FROM information_schema.index_columns " +
                "WHERE LOWER(table_name) = ? AND LOWER(index_name) = ? " +
                "ORDER BY ordinal_position ASC"
        )) {
            stmt.setString(1, tableName.toLowerCase());
            stmt.setString(2, indexName.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    columns.add(rs.getString("col_name"));
                }
            }
        }
        return columns;
    }

    private List<String> getUniqueConstraintColumns(Connection conn, String tableName, String constraintName) throws Exception {
        List<String> columns = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT LOWER(column_name) AS col_name " +
                "FROM information_schema.key_column_usage " +
                "WHERE LOWER(table_name) = ? AND LOWER(constraint_name) = ? " +
                "ORDER BY ordinal_position ASC"
        )) {
            stmt.setString(1, tableName.toLowerCase());
            stmt.setString(2, constraintName.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    columns.add(rs.getString("col_name"));
                }
            }
        }
        return columns;
    }
}
