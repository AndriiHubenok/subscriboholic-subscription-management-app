package com.anhub.subscriboholic.auth.token;

import com.anhub.subscriboholic.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class VerificationTokenTest {

    @Test
    @DisplayName("Should initialize createdAt to now and expiresAt to 24 hours later in default constructor")
    void defaultConstructor_shouldInitializeCreatedAtAndSetExpiresAtTo24HoursLater() {
        // Arrange
        Instant before = Instant.now().minusSeconds(1);

        // Act
        VerificationToken token = new VerificationToken();
        Instant after = Instant.now().plusSeconds(1);

        // Assert
        assertNotNull(token.getCreatedAt());
        assertNotNull(token.getExpiresAt());

        assertTrue(token.getCreatedAt().isAfter(before) && token.getCreatedAt().isBefore(after));

        // expiresAt should be exactly 86400 seconds (24 hours) after createdAt
        Duration duration = Duration.between(token.getCreatedAt(), token.getExpiresAt());
        assertEquals(86400, duration.getSeconds());
        assertEquals(24, duration.toHours());
    }

    @Test
    @DisplayName("Should return false from isExpired when expiresAt is in the future")
    void isExpired_shouldReturnFalse_whenExpiresAtIsInTheFuture() {
        // Arrange
        VerificationToken token = new VerificationToken();
        token.setExpiresAt(Instant.now().plusSeconds(3600)); // 1 hour into future

        // Act & Assert
        assertFalse(token.isExpired());
    }

    @Test
    @DisplayName("Should return true from isExpired when expiresAt is in the past")
    void isExpired_shouldReturnTrue_whenExpiresAtIsInThePast() {
        // Arrange
        VerificationToken token = new VerificationToken();
        token.setExpiresAt(Instant.now().minusSeconds(10)); // 10 seconds in the past

        // Act & Assert
        assertTrue(token.isExpired());
    }

    @Test
    @DisplayName("Should properly set and get all fields")
    void settersAndGetters_shouldWorkCorrectly() {
        // Arrange
        VerificationToken token = new VerificationToken();
        User user = new User();
        user.setId(5);
        user.setUsername("TestUser");

        Instant created = Instant.parse("2026-01-01T10:00:00Z");
        Instant expires = Instant.parse("2026-01-02T10:00:00Z");

        // Act
        token.setId(100L);
        token.setToken("custom-token-xyz");
        token.setUser(user);
        token.setCreatedAt(created);
        token.setExpiresAt(expires);

        // Assert
        assertEquals(100L, token.getId());
        assertEquals("custom-token-xyz", token.getToken());
        assertEquals(user, token.getUser());
        assertEquals(created, token.getCreatedAt());
        assertEquals(expires, token.getExpiresAt());
        assertThat(token.getToken()).isEqualTo("custom-token-xyz");
    }
}
