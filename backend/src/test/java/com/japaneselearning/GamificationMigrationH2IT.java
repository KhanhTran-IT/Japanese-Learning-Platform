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

class GamificationMigrationH2IT {

    @Test
    @DisplayName("Flyway V17: Unique constraint uk_user_activity_reference enforces reference idempotency and permits nullable references on H2")
    void v17GamificationMigration_ExecutesSuccessfullyOnH2() throws Exception {
        DataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:v17_gamification_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );

        try (Connection conn = dataSource.getConnection()) {
            // Run baseline schema V1 + V6 + V17
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/migration/V1__init_schema.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/migration/V6__create_leaderboard_tables.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/migration/V17__add_learning_activity_unique_constraint.sql"));

            try (Statement stmt = conn.createStatement()) {
                // Insert a test user
                stmt.execute("INSERT INTO users (id, email, password_hash, full_name, status, level, current_streak) " +
                             "VALUES (100, 'learner@example.com', 'hash', 'Learner Test', 'ACTIVE', 1, 0)");

                // 1. Reference-based activity: First insert succeeds
                stmt.execute("INSERT INTO learning_activities (user_id, activity_type, xp_amount, reference_id, description) " +
                             "VALUES (100, 'LESSON_COMPLETED', 100, 501, 'Completed Lesson 501')");

                ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) FROM learning_activities WHERE user_id = 100 AND activity_type = 'LESSON_COMPLETED' AND reference_id = 501");
                assertThat(rs1.next()).isTrue();
                assertThat(rs1.getInt(1)).isEqualTo(1);

                // 2. Reference-based activity: Duplicate insert fails with unique constraint violation
                assertThatThrownBy(() -> stmt.execute("INSERT INTO learning_activities (user_id, activity_type, xp_amount, reference_id, description) " +
                                                      "VALUES (100, 'LESSON_COMPLETED', 100, 501, 'Duplicate Lesson 501')"))
                        .isInstanceOf(SQLException.class);

                // 3. Nullable reference activities: Multiple inserts with reference_id = NULL must succeed (Standard SQL NULL != NULL)
                stmt.execute("INSERT INTO learning_activities (user_id, activity_type, xp_amount, reference_id, description) " +
                             "VALUES (100, 'FLASHCARD_REVIEWED', 10, NULL, 'Review 1')");
                stmt.execute("INSERT INTO learning_activities (user_id, activity_type, xp_amount, reference_id, description) " +
                             "VALUES (100, 'FLASHCARD_REVIEWED', 10, NULL, 'Review 2')");

                ResultSet rsNull = stmt.executeQuery("SELECT COUNT(*) FROM learning_activities WHERE user_id = 100 AND activity_type = 'FLASHCARD_REVIEWED' AND reference_id IS NULL");
                assertThat(rsNull.next()).isTrue();
                assertThat(rsNull.getInt(1)).isEqualTo(2);
            }
        }
    }
}
