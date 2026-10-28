package com.japaneselearning;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real MariaDB Integration Tests executed in CI and against running containerized MariaDB.
 * Enabled conditionally via environment variable `RUN_MARIADB_IT=true` to ensure
 * local unit test execution without MariaDB remains fast and deterministic.
 */
@SpringBootTest
@ActiveProfiles("mariadb-it")
@EnabledIfEnvironmentVariable(named = "RUN_MARIADB_IT", matches = "true")
public class RealMariaDbIntegrationIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("MariaDB Flyway Schema History: Toàn bộ 10 migration V1-V10 thực thi thành công trên MariaDB")
    void flywayMigrations_AreAllSuccessfullyAppliedOnMariaDb() {
        List<Map<String, Object>> migrations = jdbcTemplate.queryForList(
                "SELECT version, description, type, script, success FROM flyway_schema_history ORDER BY installed_rank ASC"
        );

        assertThat(migrations).isNotEmpty();
        assertThat(migrations.size()).isGreaterThanOrEqualTo(10);

        for (Map<String, Object> migration : migrations) {
            Object success = migration.get("success");
            boolean isSuccess = (success instanceof Boolean && (Boolean) success)
                    || (success instanceof Number && ((Number) success).intValue() == 1);
            assertThat(isSuccess)
                    .as("Migration %s (%s) must be successful", migration.get("version"), migration.get("script"))
                    .isTrue();
        }

        List<String> versions = migrations.stream()
                .map(m -> String.valueOf(m.get("version")))
                .toList();

        assertThat(versions).contains("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
    }

    @Test
    @DisplayName("MariaDB Charset & Collation: Lưu trữ và truy vấn chuỗi tiếng Nhật Kanji, Hiragana, Katakana UTF8MB4 chính xác")
    void utf8mb4JapaneseCharacters_AreStoredAndRetrievedCorrectly() {
        String testEmail = "test_kanji_" + System.currentTimeMillis() + "@example.com";
        String japaneseName = "山田 太郎 (やまだ たろう)";
        String japaneseCourseTitle = "日本語能力試験N5・文法と語彙コース (カタカナ・ひらがな・漢字)";

        jdbcTemplate.update(
                "INSERT INTO users (full_name, email, password_hash, status) VALUES (?, ?, ?, 'ACTIVE')",
                japaneseName, testEmail, "$2a$10$abcdefghijklmnopqrstuv"
        );

        Long teacherId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?",
                Long.class,
                testEmail
        );

        String retrievedName = jdbcTemplate.queryForObject(
                "SELECT full_name FROM users WHERE email = ?",
                String.class,
                testEmail
        );
        assertThat(retrievedName).isEqualTo(japaneseName);

        String slug = "n5-kanji-course-" + System.currentTimeMillis();
        jdbcTemplate.update(
                "INSERT INTO courses (teacher_id, title, slug, course_type, level, status) VALUES (?, ?, ?, 'FREE', 'N5', 'DRAFT')",
                teacherId, japaneseCourseTitle, slug
        );

        String retrievedTitle = jdbcTemplate.queryForObject(
                "SELECT title FROM courses WHERE slug = ?",
                String.class,
                slug
        );
        assertThat(retrievedTitle).isEqualTo(japaneseCourseTitle);

        // Cleanup test data in reverse FK order
        jdbcTemplate.update("DELETE FROM courses WHERE slug = ?", slug);
        jdbcTemplate.update("DELETE FROM users WHERE email = ?", testEmail);
    }

    @Test
    @DisplayName("MariaDB Schema Integrity: Kiểm tra các bảng cốt lõi và index vòng đời refresh_token tồn tại")
    void coreTablesAndIndexes_ExistOnMariaDb() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()",
                String.class
        );

        assertThat(tables).contains(
                "users", "roles", "user_roles",
                "courses", "course_sections", "lessons",
                "quizzes", "questions", "answers",
                "orders", "order_items", "payments",
                "refresh_tokens", "flyway_schema_history"
        );

        List<String> indexes = jdbcTemplate.queryForList(
                "SELECT index_name FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'refresh_tokens'",
                String.class
        );

        assertThat(indexes).contains(
                "uq_refresh_tokens_token_hash",
                "idx_refresh_tokens_user_revoked",
                "idx_refresh_tokens_expired_at",
                "idx_refresh_tokens_revoked"
        );
    }
}
