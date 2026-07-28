package com.anhub.subscriboholic.service;

import com.anhub.subscriboholic.mapper.UserMapper;
import com.anhub.subscriboholic.model.dto.CreateUserRequest;
import com.anhub.subscriboholic.model.dto.LoginRequest;
import com.anhub.subscriboholic.model.entity.User;
import com.anhub.subscriboholic.model.enumerated.UserRole;
import com.anhub.subscriboholic.repository.UserRepository;
import com.anhub.subscriboholic.security.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder encoder;
    private final UserMapper userMapper;

    public String signup(CreateUserRequest createUserRequest) {
        createUserRequest.setPassword(encoder.encode(createUserRequest.getPassword()));

        User user = userMapper.toEntity(createUserRequest);
        user.setRole(UserRole.USER);
        User createdUser = userRepository.save(user);

        return jwtService.generateToken(createdUser);
    }

    public String login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        return jwtService.generateToken(user);
    }
}
