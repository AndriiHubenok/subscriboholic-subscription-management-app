package com.anhub.subscriboholic.service;

import com.anhub.subscriboholic.mapper.UserMapper;
import com.anhub.subscriboholic.model.dto.CreateUserRequest;
import com.anhub.subscriboholic.model.dto.UserDTO;
import com.anhub.subscriboholic.model.entity.Subscription;
import com.anhub.subscriboholic.model.entity.User;
import com.anhub.subscriboholic.model.enumerated.UserRole;
import com.anhub.subscriboholic.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUser() {
        CreateUserRequest request = new CreateUserRequest();

        request.setUsername("Harrier Du Bois");
        request.setPassword("Dora123!");
        request.setEmail("superstarcop@gmail.com");

        User mockUser = new User();
        mockUser.setUsername("Harrier Du Bois");
        mockUser.setEmail("superstarcop@gmail.com");
        mockUser.setRole(UserRole.USER);

        UserDTO mockDTO = new UserDTO();
        mockDTO.setId(1);
        mockDTO.setUsername("Harrier Du Bois");
        mockDTO.setEmail("superstarcop@gmail.com");
        mockDTO.setRole(UserRole.USER);
        mockDTO.setCreatedAt(LocalDateTime.now());
        mockDTO.setUpdatedAt(LocalDateTime.now());

        Mockito.when(userMapper.toEntity(request))
                .thenReturn(mockUser);

        Mockito.when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("Dora123!");

        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(mockUser);

        Mockito.when(userMapper.toDTO(mockUser))
                .thenReturn(mockDTO);

        UserDTO retrievedUser = userService.createUser(request);

        assertEquals("Harrier Du Bois", retrievedUser.getUsername());
        assertEquals("superstarcop@gmail.com", retrievedUser.getEmail());
        assertEquals(UserRole.USER, retrievedUser.getRole());
        assertNotNull(retrievedUser.getId());
        assertNotNull(retrievedUser.getCreatedAt());
        assertNotNull(retrievedUser.getUpdatedAt());
    }

    @Test
    void shouldGetUser() {

        Integer id = 1;
        User mockUser = new User();
        mockUser.setId(id);
        mockUser.setUsername("Harrier Du Bois");

        UserDTO mockDTO = new UserDTO();
        mockDTO.setId(id);
        mockDTO.setUsername("Harrier Du Bois");
        mockDTO.setEmail("superstarcop@gmail.com");
        mockDTO.setRole(UserRole.USER);
        mockDTO.setCreatedAt(LocalDateTime.now());
        mockDTO.setUpdatedAt(LocalDateTime.now());

        Mockito.when(userRepository.findById(id))
                .thenReturn(Optional.of(mockUser));

        Mockito.when(userMapper.toDTO(mockUser))
                .thenReturn(mockDTO);

        UserDTO retrievedUser = userService.getUserById(id);

        assertEquals(id, retrievedUser.getId());
        assertEquals("Harrier Du Bois", retrievedUser.getUsername());
        assertEquals("superstarcop@gmail.com", retrievedUser.getEmail());
        assertEquals(UserRole.USER, retrievedUser.getRole());
        assertNotNull(retrievedUser.getCreatedAt());
        assertNotNull(retrievedUser.getUpdatedAt());
    }

    @Test
    void shouldDeleteUser() {

        Integer id = 1;
        User mockUser = new User();
        mockUser.setId(id);
        mockUser.setUsername("Harrier Du Bois");
        mockUser.setEmail("superstarcop@gmail.com");
        mockUser.setRole(UserRole.USER);

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(mockUser));

        boolean isDeleted = userService.deleteUserById(id);

        assertTrue(isDeleted);

        Mockito.verify(userRepository, Mockito.times(1)).delete(mockUser);
    }
}
