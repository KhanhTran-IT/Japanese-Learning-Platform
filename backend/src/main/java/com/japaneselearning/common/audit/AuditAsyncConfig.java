package com.japaneselearning.common.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Configuration for asynchronous audit logging thread pool.
 * <p>
 * Uses {@link ThreadPoolExecutor.CallerRunsPolicy} so if the queue is full,
 * the calling thread persists the audit record rather than dropping it.
 */
@Configuration
@EnableAsync
public class AuditAsyncConfig {

    public static final String AUDIT_EXECUTOR_BEAN = "auditExecutor";

    @Bean(name = AUDIT_EXECUTOR_BEAN)
    public Executor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("audit-exec-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
