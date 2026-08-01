package com.anhub.subscriboholic.service;

import com.anhub.subscriboholic.mapper.SubscriptionMapper;
import com.anhub.subscriboholic.mapper.UserMapper;
import com.anhub.subscriboholic.model.dto.CreateUserRequest;
import com.anhub.subscriboholic.model.dto.UserDTO;
import com.anhub.subscriboholic.model.entity.User;
import com.anhub.subscriboholic.model.enumerated.UserRole;
import com.anhub.subscriboholic.repository.SubscriptionRepository;
import com.anhub.subscriboholic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserServiceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    static PostgreSQLContainer<?> postgres =  new PostgreSQLContainer<>("postgres:17");

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

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUser() {
        CreateUserRequest request = new CreateUserRequest();

        request.setUsername("Harrier Du Bois");
        request.setPassword("Dora123!");
        request.setEmail("superstarcop@gmail.com");

        UserDTO createdUser = userService.createUser(request);

        assertEquals("Harrier Du Bois", createdUser.getUsername());
        assertEquals("superstarcop@gmail.com", createdUser.getEmail());
        assertEquals(UserRole.USER, createdUser.getRole());
        assertNotNull(createdUser.getId());
        assertNotNull(createdUser.getCreatedAt());
        assertNotNull(createdUser.getUpdatedAt());
    }

    @Test
    void shouldGetUser() {
        CreateUserRequest request = new CreateUserRequest();

        request.setUsername("Harrier Du Bois");
        request.setPassword("Dora123!");
        request.setEmail("superstarcop@gmail.com");

        Integer id = userService.createUser(request).getId();
        UserDTO retrievedUser = userService.getUserById(id);

        assertEquals("Harrier Du Bois", retrievedUser.getUsername());
        assertEquals("superstarcop@gmail.com", retrievedUser.getEmail());
        assertEquals(UserRole.USER, retrievedUser.getRole());
        assertNotNull(retrievedUser.getId());
        assertNotNull(retrievedUser.getCreatedAt());
        assertNotNull(retrievedUser.getUpdatedAt());
    }

    @Test
    void shouldDeleteUser() {
        CreateUserRequest request = new CreateUserRequest();

        request.setUsername("Harrier Du Bois");
        request.setPassword("Dora123!");
        request.setEmail("superstarcop@gmail.com");

        UserDTO createdUser = userService.createUser(request);

        assertEquals("Harrier Du Bois", createdUser.getUsername());
        assertEquals("superstarcop@gmail.com", createdUser.getEmail());
        assertEquals(UserRole.USER, createdUser.getRole());
        assertNotNull(createdUser.getId());
        assertNotNull(createdUser.getCreatedAt());
        assertNotNull(createdUser.getUpdatedAt());

        userService.deleteUserById(createdUser.getId());

        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(createdUser.getId()));
    }
}
