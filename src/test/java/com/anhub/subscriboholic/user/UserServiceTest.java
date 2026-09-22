package com.anhub.subscriboholic.user;

import com.anhub.subscriboholic.user.dto.CreateUserRequest;
import com.anhub.subscriboholic.user.dto.UserDTO;
import com.anhub.subscriboholic.user.enumerated.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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

    // ==========================================
    // createUser tests
    // ==========================================

    @Test
    @DisplayName("Should successfully create user with encoded password and default USER role when request is valid")
    void createUser_shouldCreateUserAndSetDefaults_whenRequestIsValid() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Harrier Du Bois");
        request.setPassword("Dora123!");
        request.setEmail("superstarcop@gmail.com");

        User entityFromMapper = new User();
        entityFromMapper.setUsername("Harrier Du Bois");
        entityFromMapper.setEmail("superstarcop@gmail.com");

        User savedUser = new User();
        savedUser.setId(1);
        savedUser.setUsername("Harrier Du Bois");
        savedUser.setEmail("superstarcop@gmail.com");
        savedUser.setPassword("encodedPassword123");
        savedUser.setRole(UserRole.USER);
        savedUser.setEmailVerified(true);
        savedUser.setCreatedAt(LocalDateTime.now());
        savedUser.setUpdatedAt(LocalDateTime.now());

        UserDTO mockDTO = new UserDTO();
        mockDTO.setId(1);
        mockDTO.setUsername("Harrier Du Bois");
        mockDTO.setEmail("superstarcop@gmail.com");
        mockDTO.setRole(UserRole.USER);
        mockDTO.setCreatedAt(savedUser.getCreatedAt());
        mockDTO.setUpdatedAt(savedUser.getUpdatedAt());

        when(userMapper.toEntity(request)).thenReturn(entityFromMapper);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDTO(savedUser)).thenReturn(mockDTO);

        // Act
        UserDTO retrievedUser = userService.createUser(request);

        // Assert
        assertNotNull(retrievedUser);
        assertEquals(1, retrievedUser.getId());
        assertEquals("Harrier Du Bois", retrievedUser.getUsername());
        assertEquals("superstarcop@gmail.com", retrievedUser.getEmail());
        assertEquals(UserRole.USER, retrievedUser.getRole());
        assertNotNull(retrievedUser.getCreatedAt());
        assertNotNull(retrievedUser.getUpdatedAt());

        // Verify entity properties passed to repository.save
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals("encodedPassword123", capturedUser.getPassword());
        assertEquals(UserRole.USER, capturedUser.getRole());
        assertTrue(capturedUser.isEmailVerified());

        // Verify interactions
        verify(userMapper, times(1)).toEntity(request);
        verify(passwordEncoder, times(1)).encode("Dora123!");
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toDTO(savedUser);
        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("Should propagate exception and not save user when password encoder fails")
    void createUser_shouldPropagateException_whenPasswordEncoderThrowsException() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Harrier Du Bois");
        request.setPassword("Dora123!");
        request.setEmail("superstarcop@gmail.com");

        User entityFromMapper = new User();
        when(userMapper.toEntity(request)).thenReturn(entityFromMapper);
        when(passwordEncoder.encode(anyString())).thenThrow(new IllegalArgumentException("Encoding failed"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request)
        );

        assertEquals("Encoding failed", exception.getMessage());
        assertThat(exception.getMessage()).isEqualTo("Encoding failed");

        verify(userMapper, times(1)).toEntity(request);
        verify(passwordEncoder, times(1)).encode("Dora123!");
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toDTO(any());
        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("Should propagate exception and not map to DTO when repository save fails")
    void createUser_shouldPropagateException_whenRepositorySaveThrowsException() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Harrier Du Bois");
        request.setPassword("Dora123!");
        request.setEmail("superstarcop@gmail.com");

        User entityFromMapper = new User();
        when(userMapper.toEntity(request)).thenReturn(entityFromMapper);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error during save"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.createUser(request)
        );

        assertEquals("Database error during save", exception.getMessage());

        verify(userMapper, times(1)).toEntity(request);
        verify(passwordEncoder, times(1)).encode("Dora123!");
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, never()).toDTO(any());
        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder);
    }

    // ==========================================
    // getUserById tests
    // ==========================================

    @Test
    @DisplayName("Should return user DTO when user exists by ID")
    void getUserById_shouldReturnUserDTO_whenUserExists() {
        // Arrange
        Integer id = 1;
        User mockUser = new User();
        mockUser.setId(id);
        mockUser.setUsername("Harrier Du Bois");
        mockUser.setEmail("superstarcop@gmail.com");
        mockUser.setRole(UserRole.USER);

        UserDTO mockDTO = new UserDTO();
        mockDTO.setId(id);
        mockDTO.setUsername("Harrier Du Bois");
        mockDTO.setEmail("superstarcop@gmail.com");
        mockDTO.setRole(UserRole.USER);
        mockDTO.setCreatedAt(LocalDateTime.now());
        mockDTO.setUpdatedAt(LocalDateTime.now());

        when(userRepository.findById(id)).thenReturn(Optional.of(mockUser));
        when(userMapper.toDTO(mockUser)).thenReturn(mockDTO);

        // Act
        UserDTO retrievedUser = userService.getUserById(id);

        // Assert
        assertNotNull(retrievedUser);
        assertEquals(id, retrievedUser.getId());
        assertEquals("Harrier Du Bois", retrievedUser.getUsername());
        assertEquals("superstarcop@gmail.com", retrievedUser.getEmail());
        assertEquals(UserRole.USER, retrievedUser.getRole());
        assertNotNull(retrievedUser.getCreatedAt());
        assertNotNull(retrievedUser.getUpdatedAt());

        verify(userRepository, times(1)).findById(id);
        verify(userMapper, times(1)).toDTO(mockUser);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when user does not exist by ID")
    void getUserById_shouldThrowIllegalArgumentException_whenUserNotFound() {
        // Arrange
        Integer id = 999;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(id)
        );

        assertEquals("User not found", exception.getMessage());
        assertThat(exception.getMessage()).isEqualTo("User not found");

        verify(userRepository, times(1)).findById(id);
        verify(userMapper, never()).toDTO(any());
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when ID is null")
    void getUserById_shouldThrowIllegalArgumentException_whenIdIsNull() {
        // Arrange
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(null)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository, times(1)).findById(null);
        verify(userMapper, never()).toDTO(any());
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    // ==========================================
    // deleteUserById tests
    // ==========================================

    @Test
    @DisplayName("Should delete user and return true when user exists by ID")
    void deleteUserById_shouldDeleteUserAndReturnTrue_whenUserExists() {
        // Arrange
        Integer id = 1;
        User mockUser = new User();
        mockUser.setId(id);
        mockUser.setUsername("Harrier Du Bois");
        mockUser.setEmail("superstarcop@gmail.com");
        mockUser.setRole(UserRole.USER);

        when(userRepository.findById(id)).thenReturn(Optional.of(mockUser));

        // Act
        boolean isDeleted = userService.deleteUserById(id);

        // Assert
        assertTrue(isDeleted);

        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(1)).delete(mockUser);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException and not call delete when user does not exist")
    void deleteUserById_shouldThrowIllegalArgumentException_whenUserNotFound() {
        // Arrange
        Integer id = 999;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.deleteUserById(id)
        );

        assertEquals("User not found", exception.getMessage());
        assertThat(exception.getMessage()).isEqualTo("User not found");

        verify(userRepository, times(1)).findById(id);
        verify(userRepository, never()).delete(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when ID is null for delete")
    void deleteUserById_shouldThrowIllegalArgumentException_whenIdIsNull() {
        // Arrange
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.deleteUserById(null)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository, times(1)).findById(null);
        verify(userRepository, never()).delete(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should propagate exception when repository delete throws exception")
    void deleteUserById_shouldPropagateException_whenRepositoryDeleteThrowsException() {
        // Arrange
        Integer id = 1;
        User mockUser = new User();
        mockUser.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(mockUser));
        doThrow(new RuntimeException("Database error during delete")).when(userRepository).delete(mockUser);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.deleteUserById(id)
        );

        assertEquals("Database error during delete", exception.getMessage());

        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(1)).delete(mockUser);
        verifyNoMoreInteractions(userRepository);
    }

    // ==========================================
    // Backward compatibility aliases
    // ==========================================

    @Test
    void shouldCreateUser() {
        createUser_shouldCreateUserAndSetDefaults_whenRequestIsValid();
    }

    @Test
    void shouldGetUser() {
        getUserById_shouldReturnUserDTO_whenUserExists();
    }

    @Test
    void shouldDeleteUser() {
        deleteUserById_shouldDeleteUserAndReturnTrue_whenUserExists();
    }
}
