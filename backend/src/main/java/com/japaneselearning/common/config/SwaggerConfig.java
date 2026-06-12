package com.japaneselearning.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 / Swagger configuration.
 * Access Swagger UI at: /swagger-ui.html
 * Access API docs at: /api-docs
 */
@Configuration
public class SwaggerConfig {

    @Value("${app.backend-url}")
    private String backendUrl;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Japanese Learning Platform API")
                        .version("1.0.0")
                        .description("REST API for Japanese Learning Platform - Backend")
                        .contact(new Contact()
                                .name("Japanese Learning Team")
                                .email("contact@japaneselearning.com")))
                .servers(List.of(
                        new Server()
                                .url(backendUrl)
                                .description("Local development server")));
    }
}
