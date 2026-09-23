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
import com.anhub.subscriboholic.user.UserMapper;
import com.anhub.subscriboholic.user.UserRepository;
import com.anhub.subscriboholic.user.dto.CreateUserRequest;
import com.anhub.subscriboholic.user.enumerated.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private NotificationEventProducer notificationEventProducer;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==========================================
    // signup tests
    // ==========================================

    @Test
    @DisplayName("Should create unverified user, persist verification token, and produce registration notification event when request is valid")
    void signup_shouldCreateUnverifiedUserAndGenerateTokenAndSendNotification_whenRequestIsValid() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Adam Jensen");
        request.setPassword("NeverAskedForThis123!");
        request.setEmail("adam.jensen@sarif.com");

        User entityFromMapper = new User();
        entityFromMapper.setUsername("Adam Jensen");
        entityFromMapper.setEmail("adam.jensen@sarif.com");

        User savedUser = new User();
        savedUser.setId(1);
        savedUser.setUsername("Adam Jensen");
        savedUser.setEmail("adam.jensen@sarif.com");
        savedUser.setPassword("encoded_hash_123");
        savedUser.setRole(UserRole.USER);
        savedUser.setEmailVerified(false);

        UserRegisteredEvent mockEvent = new UserRegisteredEvent();
        mockEvent.setUserId(1);
        mockEvent.setUsername("Adam Jensen");
        mockEvent.setUserEmail("adam.jensen@sarif.com");

        when(encoder.encode("NeverAskedForThis123!")).thenReturn("encoded_hash_123");
        when(userMapper.toEntity(request)).thenReturn(entityFromMapper);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toUserEmailVerificationEvent(savedUser)).thenReturn(mockEvent);

        // Act
        boolean result = authService.signup(request);

        // Assert
        assertTrue(result);

        // Verify user persistence details
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals("Adam Jensen", capturedUser.getUsername());
        assertEquals("adam.jensen@sarif.com", capturedUser.getEmail());
        assertEquals(UserRole.USER, capturedUser.getRole());
        assertFalse(capturedUser.isEmailVerified());

        // Verify verification token persistence
        ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(tokenRepository, times(1)).save(tokenCaptor.capture());
        VerificationToken capturedToken = tokenCaptor.getValue();
        assertNotNull(capturedToken.getToken());
        assertEquals(savedUser, capturedToken.getUser());
        assertNotNull(capturedToken.getExpiresAt());

        // Verify registration notification event produced
        ArgumentCaptor<UserRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(notificationEventProducer, times(1)).sendRegistrationVerification(eventCaptor.capture());
        UserRegisteredEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent.getEventId());
        assertEquals(capturedToken.getToken(), capturedEvent.getVerificationToken());

        verify(encoder, times(1)).encode("NeverAskedForThis123!");
        verify(userMapper, times(1)).toEntity(request);
        verify(userMapper, times(1)).toUserEmailVerificationEvent(savedUser);
        verifyNoMoreInteractions(userRepository, tokenRepository, notificationEventProducer);
    }

    @Test
    @DisplayName("Should propagate exception and halt process when password encoder fails")
    void signup_shouldPropagateException_whenPasswordEncoderFails() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Adam Jensen");
        request.setPassword("RawPassword!");
        request.setEmail("adam@sarif.com");

        when(encoder.encode(anyString())).thenThrow(new IllegalArgumentException("Encoding failure"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(request)
        );

        assertEquals("Encoding failure", exception.getMessage());
        verify(userMapper, never()).toEntity(any());
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
        verify(notificationEventProducer, never()).sendRegistrationVerification(any());
    }

    @Test
    @DisplayName("Should propagate exception and not generate token when user repository save fails")
    void signup_shouldPropagateException_whenUserRepositorySaveFails() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Adam Jensen");
        request.setPassword("RawPassword!");
        request.setEmail("adam@sarif.com");

        User entity = new User();
        when(encoder.encode("RawPassword!")).thenReturn("encoded_pass");
        when(userMapper.toEntity(request)).thenReturn(entity);
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error saving user"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.signup(request)
        );

        assertEquals("Database error saving user", exception.getMessage());
        verify(tokenRepository, never()).save(any());
        verify(notificationEventProducer, never()).sendRegistrationVerification(any());
    }

    @Test
    @DisplayName("Should propagate exception when token repository save fails")
    void signup_shouldPropagateException_whenTokenRepositorySaveFails() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Adam Jensen");
        request.setPassword("RawPassword!");
        request.setEmail("adam@sarif.com");

        User entity = new User();
        User savedUser = new User();
        savedUser.setId(1);

        when(encoder.encode("RawPassword!")).thenReturn("encoded_pass");
        when(userMapper.toEntity(request)).thenReturn(entity);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenRepository.save(any(VerificationToken.class))).thenThrow(new RuntimeException("Token save failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.signup(request)
        );

        assertEquals("Token save failed", exception.getMessage());
        verify(notificationEventProducer, never()).sendRegistrationVerification(any());
    }

    @Test
    @DisplayName("Should propagate exception when notification event producer fails")
    void signup_shouldPropagateException_whenNotificationProducerFails() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Adam Jensen");
        request.setPassword("RawPassword!");
        request.setEmail("adam@sarif.com");

        User entity = new User();
        User savedUser = new User();
        savedUser.setId(1);

        when(encoder.encode("RawPassword!")).thenReturn("encoded_pass");
        when(userMapper.toEntity(request)).thenReturn(entity);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toUserEmailVerificationEvent(savedUser)).thenReturn(new UserRegisteredEvent());
        doThrow(new RuntimeException("RabbitMQ connection down"))
                .when(notificationEventProducer).sendRegistrationVerification(any(UserRegisteredEvent.class));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.signup(request)
        );

        assertEquals("RabbitMQ connection down", exception.getMessage());
        verify(notificationEventProducer, times(1)).sendRegistrationVerification(any());
    }

    // ==========================================
    // verifyEmail tests
    // ==========================================

    @Test
    @DisplayName("Should verify user email, save user, delete token, and return true when token is valid and unexpired")
    void verifyEmail_shouldSetEmailVerifiedTrueAndDeleteToken_whenTokenIsValidAndNotExpired() {
        // Arrange
        String tokenValue = "valid-token-123";
        User user = new User();
        user.setId(1);
        user.setEmailVerified(false);

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setId(10L);
        verificationToken.setToken(tokenValue);
        verificationToken.setUser(user);
        verificationToken.setExpiresAt(Instant.now().plusSeconds(3600));

        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(verificationToken));

        // Act
        boolean result = authService.verifyEmail(tokenValue);

        // Assert
        assertTrue(result);
        assertTrue(user.isEmailVerified());

        verify(tokenRepository, times(1)).findByToken(tokenValue);
        verify(userRepository, times(1)).save(user);
        verify(tokenRepository, times(1)).delete(verificationToken);
        verifyNoMoreInteractions(tokenRepository, userRepository);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException when verification token is not found")
    void verifyEmail_shouldThrowInvalidTokenException_whenTokenNotFound() {
        // Arrange
        String tokenValue = "unknown-token-404";
        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.empty());

        // Act & Assert
        InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> authService.verifyEmail(tokenValue)
        );

        assertEquals("Invalid verification token: unknown-token-404", exception.getMessage());
        assertThat(exception.getMessage()).contains(tokenValue);

        verify(tokenRepository, times(1)).findByToken(tokenValue);
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(any());
        verifyNoMoreInteractions(tokenRepository, userRepository);
    }

    @Test
    @DisplayName("Should delete expired token and throw TokenExpiredException when token is expired")
    void verifyEmail_shouldDeleteTokenAndThrowTokenExpiredException_whenTokenIsExpired() {
        // Arrange
        String tokenValue = "expired-token-xyz";
        User user = new User();
        user.setId(1);
        user.setEmailVerified(false);

        VerificationToken expiredToken = new VerificationToken();
        expiredToken.setId(20L);
        expiredToken.setToken(tokenValue);
        expiredToken.setUser(user);
        expiredToken.setExpiresAt(Instant.now().minusSeconds(120)); // Expired 2 minutes ago

        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(expiredToken));

        // Act & Assert
        TokenExpiredException exception = assertThrows(
                TokenExpiredException.class,
                () -> authService.verifyEmail(tokenValue)
        );

        assertEquals("Verification token is expired: expired-token-xyz", exception.getMessage());
        assertThat(exception.getMessage()).contains(tokenValue);
        assertFalse(user.isEmailVerified());

        verify(tokenRepository, times(1)).findByToken(tokenValue);
        verify(tokenRepository, times(1)).delete(expiredToken);
        verify(userRepository, never()).save(any());
        verifyNoMoreInteractions(tokenRepository, userRepository);
    }

    // ==========================================
    // login tests
    // ==========================================

    @Test
    @DisplayName("Should authenticate credentials, find user, and return JWT token when credentials are valid")
    void login_shouldAuthenticateAndReturnJwtToken_whenCredentialsAreValid() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("Adam Jensen", "NeverAskedForThis123!");

        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("Adam Jensen");

        String expectedJwt = "mocked.jwt.token.value";

        when(userRepository.findByUsername("Adam Jensen")).thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken(mockUser)).thenReturn(expectedJwt);

        // Act
        String token = authService.login(loginRequest);

        // Assert
        assertEquals(expectedJwt, token);
        assertThat(token).isEqualTo(expectedJwt);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager, times(1)).authenticate(authCaptor.capture());
        UsernamePasswordAuthenticationToken capturedAuth = authCaptor.getValue();
        assertEquals("Adam Jensen", capturedAuth.getPrincipal());
        assertEquals("NeverAskedForThis123!", capturedAuth.getCredentials());

        verify(userRepository, times(1)).findByUsername("Adam Jensen");
        verify(jwtService, times(1)).generateToken(mockUser);
        verifyNoMoreInteractions(authenticationManager, userRepository, jwtService);
    }

    @Test
    @DisplayName("Should propagate BadCredentialsException when authentication manager fails")
    void login_shouldPropagateBadCredentialsException_whenAuthenticationFails() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("Adam Jensen", "WrongPassword!");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Bad credentials", exception.getMessage());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when authenticated user cannot be found in repository")
    void login_shouldThrowNoSuchElementException_whenUserNotFoundInRepository() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("GhostUser", "Password123!");

        when(userRepository.findByUsername("GhostUser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                NoSuchElementException.class,
                () -> authService.login(loginRequest)
        );

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByUsername("GhostUser");
        verify(jwtService, never()).generateToken(any());
    }

    // ==========================================
    // getCurrentUserUsername tests
    // ==========================================

    @Test
    @DisplayName("Should return current username when security context contains authenticated user")
    void getCurrentUserUsername_shouldReturnUsername_whenSecurityContextHasAuthentication() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("Adam Jensen");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act
        String username = authService.getCurrentUserUsername();

        // Assert
        assertEquals("Adam Jensen", username);
    }

    @Test
    @DisplayName("Should throw ResponseStatusException UNAUTHORIZED when authentication in security context is null")
    void getCurrentUserUsername_shouldThrowResponseStatusExceptionUnauthorized_whenUnauthenticated() {
        // Arrange
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.getCurrentUserUsername()
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("User is not authenticated.", exception.getReason());
    }

    // ==========================================
    // getCurrentUserId tests
    // ==========================================

    @Test
    @DisplayName("Should return current user ID when authenticated user exists in repository")
    void getCurrentUserId_shouldReturnUserId_whenUserExists() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("Adam Jensen");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        User mockUser = new User();
        mockUser.setId(42);
        mockUser.setUsername("Adam Jensen");

        when(userRepository.findByUsername("Adam Jensen")).thenReturn(Optional.of(mockUser));

        // Act
        Integer userId = authService.getCurrentUserId();

        // Assert
        assertEquals(42, userId);
        verify(userRepository, times(1)).findByUsername("Adam Jensen");
    }

    @Test
    @DisplayName("Should throw ResponseStatusException UNAUTHORIZED when getCurrentUserId called without authentication")
    void getCurrentUserId_shouldThrowResponseStatusExceptionUnauthorized_whenUnauthenticated() {
        // Arrange
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.getCurrentUserId()
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when authenticated username is not found in repository")
    void getCurrentUserId_shouldThrowNoSuchElementException_whenUserNotFoundInRepository() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("NonExistentUser");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("NonExistentUser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                NoSuchElementException.class,
                () -> authService.getCurrentUserId()
        );

        verify(userRepository, times(1)).findByUsername("NonExistentUser");
    }
}
