package com.japaneselearning.common.config;

import com.japaneselearning.module_auth.util.CookieUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "app.cors.allowed-origins=https://myproduction.com,http://localhost:5173"
})
public class CorsAndCookieConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnCorsHeadersForAllowedOrigin() throws Exception {
        mockMvc.perform(options("/api/courses")
                .header("Origin", "https://myproduction.com")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://myproduction.com"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void shouldRejectCorsForDisallowedOrigin() throws Exception {
        mockMvc.perform(options("/api/courses")
                .header("Origin", "https://hacker.com")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cookieUtilShouldThrowExceptionWhenSameSiteNoneAndNotSecure() {
        CookieUtil util = new CookieUtil();
        ReflectionTestUtils.setField(util, "sameSite", "None");
        ReflectionTestUtils.setField(util, "secure", false);

        assertThrows(IllegalStateException.class, util::validateConfig);
    }

    @Test
    void cookieUtilShouldPassWhenSameSiteNoneAndSecure() {
        CookieUtil util = new CookieUtil();
        ReflectionTestUtils.setField(util, "sameSite", "None");
        ReflectionTestUtils.setField(util, "secure", true);

        // Should not throw
        util.validateConfig();
    }

    @Test
    void cookieUtilShouldCreateValidRefreshTokenCookie() {
        CookieUtil util = new CookieUtil();
        ReflectionTestUtils.setField(util, "sameSite", "Lax");
        ReflectionTestUtils.setField(util, "secure", true);
        ReflectionTestUtils.setField(util, "refreshExpirationMs", 604800000L);

        org.springframework.http.ResponseCookie cookie = util.createRefreshTokenCookie("my-test-refresh-token");

        org.junit.jupiter.api.Assertions.assertEquals(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, cookie.getName());
        org.junit.jupiter.api.Assertions.assertEquals("my-test-refresh-token", cookie.getValue());
        org.junit.jupiter.api.Assertions.assertTrue(cookie.isHttpOnly());
        org.junit.jupiter.api.Assertions.assertTrue(cookie.isSecure());
        org.junit.jupiter.api.Assertions.assertEquals("Lax", cookie.getSameSite());
        org.junit.jupiter.api.Assertions.assertEquals("/api/auth", cookie.getPath());
        org.junit.jupiter.api.Assertions.assertEquals(604800L, cookie.getMaxAge().getSeconds());
    }

    @Test
    void cookieUtilShouldCreateValidClearRefreshTokenCookie() {
        CookieUtil util = new CookieUtil();
        ReflectionTestUtils.setField(util, "sameSite", "Strict");
        ReflectionTestUtils.setField(util, "secure", true);

        org.springframework.http.ResponseCookie cookie = util.createClearRefreshTokenCookie();

        org.junit.jupiter.api.Assertions.assertEquals(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, cookie.getName());
        org.junit.jupiter.api.Assertions.assertEquals("", cookie.getValue());
        org.junit.jupiter.api.Assertions.assertTrue(cookie.isHttpOnly());
        org.junit.jupiter.api.Assertions.assertTrue(cookie.isSecure());
        org.junit.jupiter.api.Assertions.assertEquals("Strict", cookie.getSameSite());
        org.junit.jupiter.api.Assertions.assertEquals("/api/auth", cookie.getPath());
        org.junit.jupiter.api.Assertions.assertEquals(0L, cookie.getMaxAge().getSeconds());
    }
}
