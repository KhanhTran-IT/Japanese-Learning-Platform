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

class AuditMigrationH2IT {

    @Test
    @DisplayName("Flyway V16: Migration V16__create_audit_logs_table.sql executes successfully on H2 (MySQL Mode)")
    void v16AuditMigration_ExecutesSuccessfullyOnH2() throws Exception {
        DataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:v16_audit_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );

        try (Connection conn = dataSource.getConnection()) {
            // Run V16 (audit_logs table and indexes)
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/migration/V16__create_audit_logs_table.sql"));

            // Verify audit_logs table exists and columns are functional
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO audit_logs (actor_id, actor_email, actor_role, action, target_type, target_id, outcome, correlation_id, metadata) " +
                             "VALUES (10, 'admin@example.com', 'ADMIN', 'USER_LOCK', 'USER', '20', 'SUCCESS', 'corr-abc-123', '{\"reason\":\"security\"}')");

                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM audit_logs WHERE action = 'USER_LOCK' AND outcome = 'SUCCESS'");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isEqualTo(1);

                ResultSet rsRecord = stmt.executeQuery("SELECT actor_email, correlation_id, metadata FROM audit_logs WHERE id = 1");
                assertThat(rsRecord.next()).isTrue();
                assertThat(rsRecord.getString("actor_email")).isEqualTo("admin@example.com");
                assertThat(rsRecord.getString("correlation_id")).isEqualTo("corr-abc-123");
                assertThat(rsRecord.getString("metadata")).contains("security");
            }
        }
    }
}
