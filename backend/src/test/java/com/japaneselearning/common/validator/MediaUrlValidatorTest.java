package com.japaneselearning.common.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaUrlValidatorTest {

    private MediaUrlValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MediaUrlValidator();
        // Default: no trusted domains configured
        validator.initialize(null);
    }

    @Test
    void isValid_ShouldAllowNullOrEmpty() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
        assertTrue(validator.isValid("   ", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://example.com/image.jpg",
            "https://s3.amazonaws.com/bucket/video.mp4",
            "https://localhost:8080/media/123",
            "https://my-domain.co.uk/path?query=1"
    })
    void isValid_ShouldAllowValidHttpUrls(String url) {
        assertTrue(validator.isValid(url, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "javascript:alert(1)",
            "file:///etc/passwd",
            "ftp://server/file.txt",
            "data:text/html,<script>alert(1)</script>",
            "not_a_url",
            "http//missing-colon.com"
    })
    void isValid_ShouldRejectInvalidOrUnsafeUrls(String url) {
        assertFalse(validator.isValid(url, null));
    }

    @Test
    void isValid_WithTrustedDomains_ShouldAllowAllowedDomains() {
        validator.setTrustedDomainsConfig("s3.amazonaws.com, youtube.com , .mycdn.net");
        validator.initialize(null);

        // Exact match
        assertTrue(validator.isValid("https://s3.amazonaws.com/bucket/img.png", null));
        assertTrue(validator.isValid("https://youtube.com/watch?v=123", null));

        // Subdomain match
        assertTrue(validator.isValid("https://bucket.s3.amazonaws.com/img.png", null));
        assertTrue(validator.isValid("https://assets.mycdn.net/file", null));
    }

    @Test
    void isValid_WithTrustedDomains_ShouldRejectUntrustedDomains() {
        validator.setTrustedDomainsConfig("s3.amazonaws.com, youtube.com");
        validator.initialize(null);

        assertFalse(validator.isValid("https://evil.com/malware.exe", null));
        assertFalse(validator.isValid("https://youtube.com.evil.com/video", null));
        assertFalse(validator.isValid("https://s3.amazonaws.com.hacker.net/file", null));
        assertFalse(validator.isValid("https://vimeo.com/123", null));
    }
}
