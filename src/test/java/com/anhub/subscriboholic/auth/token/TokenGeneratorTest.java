package com.anhub.subscriboholic.auth.token;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TokenGeneratorTest {

    @Test
    @DisplayName("Should generate non-null and non-blank verification token")
    void generateVerificationToken_shouldReturnNonNullAndNonBlankToken() {
        // Act
        String token = TokenGenerator.generateVerificationToken();

        // Assert
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("Should generate URL-safe Base64 token with 43 characters (32 random bytes without padding)")
    void generateVerificationToken_shouldReturnUrlSafeBase64TokenWithExpectedLength() {
        // Act
        String token = TokenGenerator.generateVerificationToken();

        // Assert
        // 32 bytes encoded in Base64 without padding produces ceil(32 * 4 / 3) = 43 characters
        assertEquals(43, token.length());
        assertTrue(token.matches("^[A-Za-z0-9_-]+$"));
        assertFalse(token.contains("="));
        assertFalse(token.contains("+"));
        assertFalse(token.contains("/"));
    }

    @Test
    @DisplayName("Should generate unique tokens across multiple consecutive invocations")
    void generateVerificationToken_shouldGenerateUniqueTokensOnConsecutiveCalls() {
        // Arrange
        int sampleSize = 100;
        Set<String> generatedTokens = new HashSet<>();

        // Act
        for (int i = 0; i < sampleSize; i++) {
            generatedTokens.add(TokenGenerator.generateVerificationToken());
        }

        // Assert
        assertEquals(sampleSize, generatedTokens.size());
    }
}
