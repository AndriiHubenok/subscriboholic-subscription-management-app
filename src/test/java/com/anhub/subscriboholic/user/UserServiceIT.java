package com.anhub.subscriboholic.user;

import com.anhub.subscriboholic.user.dto.CreateUserRequest;
import com.anhub.subscriboholic.user.dto.UserDTO;
import com.anhub.subscriboholic.user.enumerated.UserRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserServiceIT {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    static void beforeAll() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM subscriptions");
        jdbcTemplate.execute("DELETE FROM users");
    }

    // ==========================================
    // createUser integration tests
    // ==========================================

    @Test
    @DisplayName("Should persist user into database with BCrypt-hashed password, USER role, and verified email")
    void createUser_shouldPersistUserAndReturnDTO_whenValidRequestProvided() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Kim Kitsuragi");
        request.setPassword("Aerostatic123!");
        request.setEmail("kim.kitsuragi@police.revachol");

        // Act
        UserDTO createdUser = userService.createUser(request);

        // Assert response DTO
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("Kim Kitsuragi", createdUser.getUsername());
        assertEquals("kim.kitsuragi@police.revachol", createdUser.getEmail());
        assertEquals(UserRole.USER, createdUser.getRole());

        // Assert database persistence
        Optional<User> userInDbOpt = userRepository.findById(createdUser.getId());
        assertTrue(userInDbOpt.isPresent());

        User userInDb = userInDbOpt.get();
        assertEquals("Kim Kitsuragi", userInDb.getUsername());
        assertEquals("kim.kitsuragi@police.revachol", userInDb.getEmail());
        assertEquals(UserRole.USER, userInDb.getRole());
        assertTrue(userInDb.isEmailVerified());
        assertNotNull(userInDb.getCreatedAt());

        // Assert password was encoded and raw password is not stored in plaintext
        assertNotEquals("Aerostatic123!", userInDb.getPassword());
        assertTrue(passwordEncoder.matches("Aerostatic123!", userInDb.getPassword()));
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException and rollback when creating user with duplicate username")
    void createUser_shouldRollbackAndThrowException_whenUsernameAlreadyExists() {
        // Arrange
        CreateUserRequest firstRequest = new CreateUserRequest();
        firstRequest.setUsername("Kim Kitsuragi");
        firstRequest.setPassword("Aerostatic123!");
        firstRequest.setEmail("kim.kitsuragi@police.revachol");
        userService.createUser(firstRequest);

        CreateUserRequest duplicateUsernameRequest = new CreateUserRequest();
        duplicateUsernameRequest.setUsername("Kim Kitsuragi");
        duplicateUsernameRequest.setPassword("DifferentPassword456!");
        duplicateUsernameRequest.setEmail("different.email@police.revachol");

        // Act & Assert
        assertThrows(
                DataIntegrityViolationException.class,
                () -> userService.createUser(duplicateUsernameRequest)
        );

        // Verify only 1 user exists in DB
        assertEquals(1, userRepository.count());
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException and rollback when creating user with duplicate email")
    void createUser_shouldRollbackAndThrowException_whenEmailAlreadyExists() {
        // Arrange
        CreateUserRequest firstRequest = new CreateUserRequest();
        firstRequest.setUsername("Kim Kitsuragi");
        firstRequest.setPassword("Aerostatic123!");
        firstRequest.setEmail("kim.kitsuragi@police.revachol");
        userService.createUser(firstRequest);

        CreateUserRequest duplicateEmailRequest = new CreateUserRequest();
        duplicateEmailRequest.setUsername("Lieutenant Kitsuragi");
        duplicateEmailRequest.setPassword("DifferentPassword456!");
        duplicateEmailRequest.setEmail("kim.kitsuragi@police.revachol");

        // Act & Assert
        assertThrows(
                DataIntegrityViolationException.class,
                () -> userService.createUser(duplicateEmailRequest)
        );

        // Verify only 1 user exists in DB
        assertEquals(1, userRepository.count());
    }

    // ==========================================
    // getUserById integration tests
    // ==========================================

    @Test
    @DisplayName("Should return user DTO when user exists in database")
    void getUserById_shouldReturnUserDTO_whenUserExistsInDatabase() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Jean Vicquemare");
        request.setPassword("Judicial123!");
        request.setEmail("jean.vicquemare@police.revachol");
        UserDTO savedUser = userService.createUser(request);

        // Act
        UserDTO retrievedUser = userService.getUserById(savedUser.getId());

        // Assert
        assertNotNull(retrievedUser);
        assertEquals(savedUser.getId(), retrievedUser.getId());
        assertEquals("Jean Vicquemare", retrievedUser.getUsername());
        assertEquals("jean.vicquemare@police.revachol", retrievedUser.getEmail());
        assertEquals(UserRole.USER, retrievedUser.getRole());
        assertNotNull(retrievedUser.getCreatedAt());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when getting user by non-existent ID in database")
    void getUserById_shouldThrowIllegalArgumentException_whenUserNotFoundInDatabase() {
        // Arrange
        Integer nonExistentId = 999999;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(nonExistentId)
        );

        assertEquals("User not found", exception.getMessage());
        assertThat(exception.getMessage()).isEqualTo("User not found");
    }

    // ==========================================
    // deleteUserById integration tests
    // ==========================================

    @Test
    @DisplayName("Should delete user from database and return true when user exists")
    void deleteUserById_shouldRemoveUserFromDatabaseAndReturnTrue_whenUserExists() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Judit Minot");
        request.setPassword("PatrolOfficer123!");
        request.setEmail("judit.minot@police.revachol");
        UserDTO savedUser = userService.createUser(request);

        // Act
        boolean isDeleted = userService.deleteUserById(savedUser.getId());

        // Assert
        assertTrue(isDeleted);
        assertTrue(userRepository.findById(savedUser.getId()).isEmpty());
        assertEquals(0, userRepository.count());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when deleting user with non-existent ID in database")
    void deleteUserById_shouldThrowIllegalArgumentException_whenUserNotFoundInDatabase() {
        // Arrange
        Integer nonExistentId = 999999;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.deleteUserById(nonExistentId)
        );

        assertEquals("User not found", exception.getMessage());
        assertThat(exception.getMessage()).isEqualTo("User not found");
    }
}
