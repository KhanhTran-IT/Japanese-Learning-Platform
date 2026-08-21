package com.japaneselearning.module_health.controller;

import com.japaneselearning.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Health check endpoint.
 * Used to verify the API is running and responsive.
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "API health check endpoints")
public class HealthCheckController {

    @GetMapping
    @Operation(summary = "Check API health", description = "Returns API status and server time")
    public ApiResponse<Map<String, Object>> healthCheck() {
        Map<String, Object> healthData = Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now().toString(),
                "service", "japanese-learning-api",
                "version", "1.0.0"
        );

        return ApiResponse.success("API is running", healthData);
    }
}
