package com.japaneselearning.common.config;

import com.japaneselearning.common.security.CustomAccessDeniedHandler;
import com.japaneselearning.common.security.JwtAuthenticationEntryPoint;
import com.japaneselearning.common.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler)
            )
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(SWAGGER_WHITELIST).permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/health").permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/courses", "/api/v1/courses/**").permitAll()
                    // Admin-only system endpoints (dashboard, user management)
                    .requestMatchers("/api/v1/admin/dashboard/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/api/v1/admin/users/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                    // Course content management (TEACHER allowed with ownership checks in service layer)
                    .requestMatchers("/api/v1/admin/courses/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "TEACHER")
                    .requestMatchers("/api/v1/admin/sections/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "TEACHER")
                    .requestMatchers("/api/v1/admin/lessons/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "TEACHER")
                    .requestMatchers("/api/student/**").hasAnyRole("STUDENT", "ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/api/users/me", "/api/users/me/**").authenticated()
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
