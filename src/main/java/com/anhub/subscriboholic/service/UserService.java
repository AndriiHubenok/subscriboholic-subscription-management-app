package com.anhub.subscriboholic.service;

import com.anhub.subscriboholic.mapper.UserMapper;
import com.anhub.subscriboholic.model.dto.CreateUserRequest;
import com.anhub.subscriboholic.model.dto.UserDTO;
import com.anhub.subscriboholic.model.entity.User;
import com.anhub.subscriboholic.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDTO createUser(CreateUserRequest createUserRequest) {
        User user = userRepository.save(userMapper.toEntity(createUserRequest));
        return userMapper.toDTO(user);
    }

    public UserDTO getUserById(Integer id) {
        return userMapper.toDTO(userRepository.findById(id).orElse(null));
    }
}
