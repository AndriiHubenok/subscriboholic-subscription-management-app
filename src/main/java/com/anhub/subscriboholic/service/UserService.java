package com.anhub.subscriboholic.service;

import com.anhub.subscriboholic.mapper.UserMapper;
import com.anhub.subscriboholic.model.dto.CreateUserRequest;
import com.anhub.subscriboholic.model.dto.UserDTO;
import com.anhub.subscriboholic.model.entity.Subscription;
import com.anhub.subscriboholic.model.entity.User;
import com.anhub.subscriboholic.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;

    public UserDTO createUser(CreateUserRequest request) {
        request.setPassword(encoder.encode(request.getPassword()));
        User user = userRepository.save(userMapper.toEntity(request));
        return userMapper.toDTO(user);
    }

    public UserDTO getUserById(Integer id) {
        return userMapper.toDTO(userRepository.findById(id).orElse(null));
    }

    public boolean deleteUserById(Integer id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            userRepository.delete(user);
            return true;
        }
        return false;
    }
}
