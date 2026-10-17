package com.japaneselearning.module_auth.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TokenHashUtilTest {

    @Test
    void hashToken_ValidToken_Returns64CharHex() {
        String token = "sample-refresh-token-xyz-123456";
        String hash = TokenHashUtil.hashToken(token);

        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertTrue(hash.matches("^[0-9a-f]{64}$"));
    }

    @Test
    void hashToken_Deterministic() {
        String token = "another-token-to-verify";
        String hash1 = TokenHashUtil.hashToken(token);
        String hash2 = TokenHashUtil.hashToken(token);

        assertEquals(hash1, hash2);
    }

    @Test
    void hashToken_NullOrBlank_ReturnsNull() {
        assertNull(TokenHashUtil.hashToken(null));
        assertNull(TokenHashUtil.hashToken(""));
        assertNull(TokenHashUtil.hashToken("   "));
    }

    @Test
    void hashToken_KnownSha256Vector() {
        // SHA-256("test") = 9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08
        String hash = TokenHashUtil.hashToken("test");
        assertEquals("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", hash);
    }
}
