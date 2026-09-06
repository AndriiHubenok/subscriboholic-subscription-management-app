package com.anhub.subscriboholic.auth;

import com.anhub.subscriboholic.user.dto.CreateUserRequest;
import com.anhub.subscriboholic.auth.dto.LoginRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public String signup(@RequestBody CreateUserRequest createUserRequest) {
        return authService.signup(createUserRequest);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}