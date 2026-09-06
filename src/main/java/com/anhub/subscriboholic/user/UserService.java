package com.anhub.subscriboholic.user;

import com.anhub.subscriboholic.user.dto.CreateUserRequest;
import com.anhub.subscriboholic.user.dto.UserDTO;
import com.anhub.subscriboholic.user.enumerated.UserRole;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;

    public UserDTO createUser(CreateUserRequest request) {
        User user = userMapper.toEntity(request);

        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);

        return userMapper.toDTO(userRepository.save(user));
    }

    public UserDTO getUserById(Integer id) {
        return userMapper.toDTO(userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found")));
    }

    public boolean deleteUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userRepository.delete(user);
        return true;
    }
}
