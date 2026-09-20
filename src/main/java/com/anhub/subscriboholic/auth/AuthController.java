package com.anhub.subscriboholic.auth;

import com.anhub.subscriboholic.user.dto.CreateUserRequest;
import com.anhub.subscriboholic.auth.dto.LoginRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody CreateUserRequest createUserRequest) {
        return authService.signup(createUserRequest) ?
                ResponseEntity.ok().body("Check your email.") : ResponseEntity.badRequest().build();
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        return authService.verifyEmail(token) ?
                ResponseEntity.ok().body("Email verified successfully.") :
                ResponseEntity.badRequest().body("Invalid or expired token.");
    }
}