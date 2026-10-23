package com.japaneselearning;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenMigrationH2IT {

    @Test
    @DisplayName("Flyway V10: Migration V10__add_refresh_token_lifecycle_indexes.sql chạy thành công trên H2 và tương thích chuẩn MariaDB")
    void v10RefreshTokenMigration_ExecutesSuccessfullyOnH2() throws Exception {
        DataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:v10_refresh_token_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE users (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "full_name VARCHAR(150) NOT NULL, " +
                        "email VARCHAR(150) NOT NULL UNIQUE, " +
                        "password_hash VARCHAR(255) NOT NULL, " +
                        "status VARCHAR(20) DEFAULT 'ACTIVE', " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")");

                stmt.execute("CREATE TABLE refresh_tokens (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "user_id BIGINT NOT NULL, " +
                        "token VARCHAR(500) NULL, " +
                        "token_hash VARCHAR(64) NOT NULL UNIQUE, " +
                        "expired_at TIMESTAMP NOT NULL, " +
                        "revoked BOOLEAN DEFAULT FALSE, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users(id)" +
                        ")");
            }

            // Run V10 migration script
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/migration/V10__add_refresh_token_lifecycle_indexes.sql"));

            // Verify indexes are functional by inserting test records and querying through index filters
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO users (id, full_name, email, password_hash) VALUES (1, 'User 1', 'u1@example.com', 'hash1')");
                stmt.execute("INSERT INTO refresh_tokens (user_id, token_hash, expired_at, revoked) " +
                        "VALUES (1, 'hash-active', DATEADD('DAY', 7, CURRENT_TIMESTAMP), FALSE)");
                stmt.execute("INSERT INTO refresh_tokens (user_id, token_hash, expired_at, revoked) " +
                        "VALUES (1, 'hash-revoked', DATEADD('DAY', 7, CURRENT_TIMESTAMP), TRUE)");
                stmt.execute("INSERT INTO refresh_tokens (user_id, token_hash, expired_at, revoked) " +
                        "VALUES (1, 'hash-expired', DATEADD('DAY', -1, CURRENT_TIMESTAMP), FALSE)");

                // Query with (user_id, revoked) filter
                ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) FROM refresh_tokens WHERE user_id = 1 AND revoked = FALSE");
                assertThat(rs1.next()).isTrue();
                assertThat(rs1.getInt(1)).isEqualTo(2);

                // Query with expired_at filter
                ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) FROM refresh_tokens WHERE expired_at < CURRENT_TIMESTAMP");
                assertThat(rs2.next()).isTrue();
                assertThat(rs2.getInt(1)).isEqualTo(1);

                // Query with revoked filter
                ResultSet rs3 = stmt.executeQuery("SELECT COUNT(*) FROM refresh_tokens WHERE revoked = TRUE");
                assertThat(rs3.next()).isTrue();
                assertThat(rs3.getInt(1)).isEqualTo(1);
            }
        }
    }
}
