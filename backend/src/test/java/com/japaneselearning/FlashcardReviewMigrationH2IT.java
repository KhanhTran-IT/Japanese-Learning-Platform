package com.japaneselearning;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlashcardReviewMigrationH2IT {

    @Test
    @DisplayName("Flyway V21: Migration scopes flashcard_review_logs.idempotency_key uniqueness to (user_id, idempotency_key) on H2/MySQL")
    void v21FlashcardReviewIdempotencyMigration_ExecutesSuccessfullyOnH2() throws Exception {
        DataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:v21_flashcard_review_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );

        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/migration/V1__init_schema.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/migration/V4__create_flashcard_tables.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/migration/V5__seed_flashcard_data.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/migration/V13__add_flashcard_performance_indexes.sql"));

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO users (id, email, password_hash, full_name, status) " +
                             "VALUES (101, 'student1@example.com', 'hash1', 'Student One', 'ACTIVE')");
                stmt.execute("INSERT INTO users (id, email, password_hash, full_name, status) " +
                             "VALUES (102, 'student2@example.com', 'hash2', 'Student Two', 'ACTIVE')");

                // Seed pre-migration review log row to verify data preservation during V21
                stmt.execute("INSERT INTO flashcard_review_logs (user_id, flashcard_id, difficulty, idempotency_key) " +
                             "VALUES (101, 1, 'EASY', 'shared-key-001')");
            }

            // Execute V21 migration
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/migration/V21__scope_flashcard_review_idempotency_to_user.sql"));

            try (Statement stmt = conn.createStatement()) {
                // 1. Existing row preserved
                ResultSet rsExisting = stmt.executeQuery(
                        "SELECT idempotency_key FROM flashcard_review_logs WHERE user_id = 101 AND flashcard_id = 1");
                assertThat(rsExisting.next()).isTrue();
                assertThat(rsExisting.getString("idempotency_key")).isEqualTo("shared-key-001");

                // 2. Different user CAN use the same idempotency_key
                stmt.execute("INSERT INTO flashcard_review_logs (user_id, flashcard_id, difficulty, idempotency_key) " +
                             "VALUES (102, 1, 'MEDIUM', 'shared-key-001')");

                ResultSet rsCount = stmt.executeQuery(
                        "SELECT COUNT(*) FROM flashcard_review_logs WHERE idempotency_key = 'shared-key-001'");
                assertThat(rsCount.next()).isTrue();
                assertThat(rsCount.getInt(1)).isEqualTo(2);

                // 3. Same user CANNOT insert a duplicate (user_id, idempotency_key)
                assertThatThrownBy(() -> stmt.execute(
                        "INSERT INTO flashcard_review_logs (user_id, flashcard_id, difficulty, idempotency_key) " +
                        "VALUES (101, 1, 'HARD', 'shared-key-001')"))
                        .isInstanceOf(SQLException.class);
            }
        }
    }
}
