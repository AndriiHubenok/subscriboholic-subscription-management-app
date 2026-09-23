package com.anhub.subscriboholic.auth;

import com.anhub.subscriboholic.auth.token.TokenGenerator;
import com.anhub.subscriboholic.auth.token.TokenRepository;
import com.anhub.subscriboholic.auth.token.VerificationToken;
import com.anhub.subscriboholic.auth.token.exception.InvalidTokenException;
import com.anhub.subscriboholic.auth.token.exception.TokenExpiredException;
import com.anhub.subscriboholic.notification.dto.user.UserRegisteredEvent;
import com.anhub.subscriboholic.notification.producer.NotificationEventProducer;
import com.anhub.subscriboholic.user.UserMapper;
import com.anhub.subscriboholic.user.dto.CreateUserRequest;
import com.anhub.subscriboholic.auth.dto.LoginRequest;
import com.anhub.subscriboholic.user.User;
import com.anhub.subscriboholic.user.enumerated.UserRole;
import com.anhub.subscriboholic.user.UserRepository;
import com.anhub.subscriboholic.security.JwtService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder encoder;
    private final UserMapper userMapper;
    private final TokenRepository tokenRepository;
    private final NotificationEventProducer notificationEventProducer;

    public boolean signup(CreateUserRequest createUserRequest) {
        createUserRequest.setPassword(encoder.encode(createUserRequest.getPassword()));

        User user = userMapper.toEntity(createUserRequest);
        user.setRole(UserRole.USER);
        user.setEmailVerified(false);
        User createdUser = userRepository.save(user);

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(generateVerificationToken());
        verificationToken.setUser(createdUser);
        tokenRepository.save(verificationToken);

        UserRegisteredEvent event = userMapper.toUserEmailVerificationEvent(createdUser);
        event.setEventId(UUID.randomUUID());
        event.setVerificationToken(verificationToken.getToken());

        notificationEventProducer.sendRegistrationVerification(event);

        return true;
    }

    private String generateVerificationToken() {
        return TokenGenerator.generateVerificationToken();
    }

    public boolean verifyEmail(String tokenValue) {
        VerificationToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidTokenException(tokenValue));

        if (token.isExpired()) {
            tokenRepository.delete(token);
            throw new TokenExpiredException(tokenValue);
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        tokenRepository.delete(token);

        return true;
    }

    @Scheduled(cron = "0 3 22 * * ?")
    public void deleteExpiredUnverifiedUsers() {
        int amountDeletedUsers = userRepository.deleteUnverifiedUsersOlderThan(LocalDateTime.now());
        log.info("Deleted {} unverified users", amountDeletedUsers);
    }

    public String login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        return jwtService.generateToken(user);
    }

    public Integer getCurrentUserId() {
        String username = getCurrentUserUsername();
        User user = userRepository.findByUsername(username).orElseThrow();
        return user.getId();
    }

    public String getCurrentUserUsername() {
        String username;
        try {
            username = SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated.");
        }
        return username;
    }
}
