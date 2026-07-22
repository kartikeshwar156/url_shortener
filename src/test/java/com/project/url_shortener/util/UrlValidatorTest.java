package com.project.url_shortener.util;

import com.project.url_shortener.exception.InvalidUrlException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UrlValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "https://example.com",
            "http://example.com/path",
            "https://sub.domain.example.co.uk/page?q=1"
    })
    void acceptsValidHttpUrls(String url) {
        assertDoesNotThrow(() -> UrlValidator.validate(url));
    }

    @Test
    void rejectsBlankUrl() {
        assertThrows(InvalidUrlException.class, () -> UrlValidator.validate("   "));
    }

    @Test
    void rejectsNullUrl() {
        assertThrows(InvalidUrlException.class, () -> UrlValidator.validate(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "javascript:alert(1)",
            "file:///etc/passwd",
            "ftp://example.com"
    })
    void rejectsNonHttpSchemes(String url) {
        assertThrows(InvalidUrlException.class, () -> UrlValidator.validate(url));
    }

    @Test
    void rejectsMalformedUrl() {
        assertThrows(InvalidUrlException.class, () -> UrlValidator.validate("http://"));
    }
}
