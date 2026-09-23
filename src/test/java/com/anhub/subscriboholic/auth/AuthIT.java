package com.anhub.subscriboholic.auth;

import com.anhub.subscriboholic.auth.dto.LoginRequest;
import com.anhub.subscriboholic.auth.token.TokenRepository;
import com.anhub.subscriboholic.auth.token.VerificationToken;
import com.anhub.subscriboholic.auth.token.exception.InvalidTokenException;
import com.anhub.subscriboholic.auth.token.exception.TokenExpiredException;
import com.anhub.subscriboholic.notification.dto.user.UserRegisteredEvent;
import com.anhub.subscriboholic.notification.producer.NotificationEventProducer;
import com.anhub.subscriboholic.security.JwtService;
import com.anhub.subscriboholic.user.User;
import com.anhub.subscriboholic.user.UserRepository;
import com.anhub.subscriboholic.user.dto.CreateUserRequest;
import com.anhub.subscriboholic.user.enumerated.UserRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Optional;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthIT {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    static void beforeAll() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private NotificationEventProducer notificationEventProducer;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM email_verification_tokens");
        jdbcTemplate.execute("DELETE FROM subscriptions");
        jdbcTemplate.execute("DELETE FROM users");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==========================================
    // signup integration tests
    // ==========================================

    @Test
    @DisplayName("Should persist unverified user and verification token in database and trigger notification event")
    void signup_shouldPersistUserAndTokenAndTriggerNotificationInDatabase() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Adam Jensen");
        request.setPassword("NeverAskedForThis123!");
        request.setEmail("adam.jensen@sarif.com");

        // Act
        boolean signupResult = authService.signup(request);

        // Assert
        assertTrue(signupResult);

        // Verify user in DB
        Optional<User> userOpt = userRepository.findByUsername("Adam Jensen");
        assertTrue(userOpt.isPresent());

        User user = userOpt.get();
        assertEquals("Adam Jensen", user.getUsername());
        assertEquals("adam.jensen@sarif.com", user.getEmail());
        assertEquals(UserRole.USER, user.getRole());
        assertFalse(user.isEmailVerified());
        assertNotEquals("NeverAskedForThis123!", user.getPassword());
        assertTrue(passwordEncoder.matches("NeverAskedForThis123!", user.getPassword()));

        // Verify verification token in DB
        assertEquals(1, tokenRepository.count());
        VerificationToken token = tokenRepository.findAll().get(0);
        assertNotNull(token.getToken());
        assertEquals(user.getId(), token.getUser().getId());
        assertFalse(token.isExpired());

        // Verify notification event dispatched
        verify(notificationEventProducer).sendRegistrationVerification(any(UserRegisteredEvent.class));
    }

    // ==========================================
    // verifyEmail integration tests
    // ==========================================

    @Test
    @DisplayName("Should mark user email verified and remove token from database upon successful verification")
    void verifyEmail_shouldMarkUserAsVerifiedAndDeleteTokenInDatabase() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Adam Jensen");
        request.setPassword("NeverAskedForThis123!");
        request.setEmail("adam.jensen@sarif.com");
        authService.signup(request);

        VerificationToken token = tokenRepository.findAll().get(0);
        String tokenValue = token.getToken();

        // Act
        boolean verifyResult = authService.verifyEmail(tokenValue);

        // Assert
        assertTrue(verifyResult);

        // Verify user in DB now has emailVerified = true
        User user = userRepository.findByUsername("Adam Jensen").orElseThrow();
        assertTrue(user.isEmailVerified());

        // Verify token deleted from DB
        assertTrue(tokenRepository.findByToken(tokenValue).isEmpty());
        assertEquals(0, tokenRepository.count());
    }

    @Test
    @DisplayName("Should throw InvalidTokenException when verifying with non-existent token")
    void verifyEmail_shouldThrowInvalidTokenException_whenTokenDoesNotExistInDatabase() {
        // Act & Assert
        InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> authService.verifyEmail("completely-random-token")
        );

        assertEquals("Invalid verification token: completely-random-token", exception.getMessage());
    }

    @Test
    @DisplayName("Should delete token and throw TokenExpiredException when token is expired in database")
    void verifyEmail_shouldThrowTokenExpiredExceptionAndDeleteToken_whenTokenIsExpiredInDatabase() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Adam Jensen");
        request.setPassword("NeverAskedForThis123!");
        request.setEmail("adam.jensen@sarif.com");
        authService.signup(request);

        VerificationToken token = tokenRepository.findAll().get(0);
        token.setExpiresAt(Instant.now().minusSeconds(3600)); // 1 hour ago
        tokenRepository.save(token);

        // Act & Assert
        TokenExpiredException exception = assertThrows(
                TokenExpiredException.class,
                () -> authService.verifyEmail(token.getToken())
        );

        assertThat(exception.getMessage()).contains(token.getToken());

        // Verify user remains unverified
        User user = userRepository.findByUsername("Adam Jensen").orElseThrow();
        assertFalse(user.isEmailVerified());
    }

    // ==========================================
    // login integration tests
    // ==========================================

    @Test
    @DisplayName("Should authenticate user and return valid JWT containing username when credentials match")
    void login_shouldAuthenticateUserAndReturnValidJwt_whenCredentialsAreValid() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Adam Jensen");
        request.setPassword("NeverAskedForThis123!");
        request.setEmail("adam.jensen@sarif.com");
        authService.signup(request);

        // Verify email so user is active
        VerificationToken token = tokenRepository.findAll().get(0);
        authService.verifyEmail(token.getToken());

        LoginRequest loginRequest = new LoginRequest("Adam Jensen", "NeverAskedForThis123!");

        // Act
        String jwtToken = authService.login(loginRequest);

        // Assert
        assertNotNull(jwtToken);
        assertFalse(jwtToken.isBlank());

        String extractedUsername = jwtService.extractUsername(jwtToken);
        assertEquals("Adam Jensen", extractedUsername);
    }

    @Test
    @DisplayName("Should throw LockedException when user email is not yet verified")
    void login_shouldThrowLockedException_whenUserEmailIsNotVerified() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("UnverifiedUser");
        request.setPassword("Password123!");
        request.setEmail("unverified@sarif.com");
        authService.signup(request);

        LoginRequest loginRequest = new LoginRequest("UnverifiedUser", "Password123!");

        // Act & Assert - unverified users have isAccountNonLocked() = false
        assertThrows(
                org.springframework.security.authentication.LockedException.class,
                () -> authService.login(loginRequest)
        );
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when password does not match for verified user")
    void login_shouldFailWithBadCredentials_whenPasswordIsWrong() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Adam Jensen");
        request.setPassword("NeverAskedForThis123!");
        request.setEmail("adam.jensen@sarif.com");
        authService.signup(request);

        // Verify email so user account is unlocked
        VerificationToken token = tokenRepository.findAll().get(0);
        authService.verifyEmail(token.getToken());

        LoginRequest wrongPasswordRequest = new LoginRequest("Adam Jensen", "IncorrectPassword!");

        // Act & Assert
        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(wrongPasswordRequest)
        );
    }

    // ==========================================
    // getCurrentUserUsername & getCurrentUserId integration tests
    // ==========================================

    @Test
    @DisplayName("Should return authenticated user username and ID from database")
    void getCurrentUserUsernameAndId_shouldReturnAuthenticatedUserInfo() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("David Sarif");
        request.setPassword("SarifIndustries123!");
        request.setEmail("david.sarif@sarif.com");
        authService.signup(request);

        User savedUser = userRepository.findByUsername("David Sarif").orElseThrow();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("David Sarif", null)
        );

        // Act
        String currentUsername = authService.getCurrentUserUsername();
        Integer currentUserId = authService.getCurrentUserId();

        // Assert
        assertEquals("David Sarif", currentUsername);
        assertEquals(savedUser.getId(), currentUserId);
    }
}
