package com.anhub.subscriboholic.auth;

import com.anhub.subscriboholic.auth.dto.LoginRequest;
import com.anhub.subscriboholic.security.JwtService;
import com.anhub.subscriboholic.user.dto.CreateUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    // ==========================================
    // POST /auth/signup tests
    // ==========================================

    @Test
    @DisplayName("Should return 200 OK with 'Check your email.' when signup succeeds")
    void signup_shouldReturn200WithCheckEmailMessage_whenSignupSucceeds() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Adam Jensen");
        request.setPassword("NeverAskedForThis123!");
        request.setEmail("adam.jensen@sarif.com");

        when(authService.signup(any(CreateUserRequest.class))).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Check your email."));

        verify(authService).signup(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when signup service returns false")
    void signup_shouldReturn400BadRequest_whenSignupReturnsFalse() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Adam Jensen");
        request.setPassword("WeakPass");
        request.setEmail("adam@sarif.com");

        when(authService.signup(any(CreateUserRequest.class))).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));

        verify(authService).signup(any(CreateUserRequest.class));
    }

    // ==========================================
    // POST /auth/login tests
    // ==========================================

    @Test
    @DisplayName("Should return 200 OK with JWT token when login credentials are valid")
    void login_shouldReturnJwtToken_whenCredentialsAreValid() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("Adam Jensen", "NeverAskedForThis123!");
        String expectedJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mockToken";

        when(authService.login(any(LoginRequest.class))).thenReturn(expectedJwt);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedJwt));

        verify(authService).login(any(LoginRequest.class));
    }

    // ==========================================
    // GET /auth/verify tests
    // ==========================================

    @Test
    @DisplayName("Should return 200 OK with success message when verification token is valid")
    void verifyEmail_shouldReturn200WithSuccessMessage_whenTokenIsValid() throws Exception {
        // Arrange
        String token = "valid-token-123";
        when(authService.verifyEmail(token)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/auth/verify")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string("Email verified successfully."));

        verify(authService).verifyEmail(token);
    }

    @Test
    @DisplayName("Should return 400 Bad Request with error message when token verification fails")
    void verifyEmail_shouldReturn400WithErrorMessage_whenTokenVerificationFails() throws Exception {
        // Arrange
        String token = "invalid-token-456";
        when(authService.verifyEmail(token)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/auth/verify")
                        .param("token", token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid or expired token."));

        verify(authService).verifyEmail(token);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when required token request parameter is missing")
    void verifyEmail_shouldReturn400BadRequest_whenTokenParameterIsMissing() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/auth/verify"))
                .andExpect(status().isBadRequest());
    }
}
