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
}
