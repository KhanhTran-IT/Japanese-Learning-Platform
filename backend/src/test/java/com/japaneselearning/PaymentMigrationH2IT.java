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

class PaymentMigrationH2IT {

    @Test
    @DisplayName("Flyway V9: Migration V9__create_payment_tables.sql chạy thành công trên H2 và tương thích chuẩn MariaDB")
    void v9PaymentMigration_ExecutesSuccessfullyOnH2() throws Exception {
        DataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:v9_payment_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
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
                             "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                             ")");

                stmt.execute("CREATE TABLE courses (" +
                             "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                             "title VARCHAR(255) NOT NULL, " +
                             "slug VARCHAR(255) NOT NULL UNIQUE" +
                             ")");
            }

            // Run V7 (orders and order_items)
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/migration/V7__create_order_tables.sql"));

            // Run V9 (payments)
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/migration/V9__create_payment_tables.sql"));

            // Verify payments table exists and columns are functional
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO users (id, full_name, email, password_hash) VALUES (1, 'Test User', 'test@example.com', 'hash')");
                stmt.execute("INSERT INTO orders (id, user_id, order_code, total_amount, final_amount, status) VALUES (1, 1, 'ORD-001', 500000.00, 500000.00, 'PENDING')");
                stmt.execute("INSERT INTO payments (order_id, payment_method, payment_provider, transaction_code, amount, status) " +
                             "VALUES (1, 'SIMULATION', 'LOCAL_SIMULATION', 'TXN-001', 500000.00, 'SUCCESS')");

                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM payments WHERE order_id = 1 AND status = 'SUCCESS'");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isEqualTo(1);
            }
        }
    }
}
