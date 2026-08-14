package com.anhub.subscriboholic.integration;

import com.anhub.subscriboholic.model.dto.CreateSubscriptionRequest;
import com.anhub.subscriboholic.model.dto.CreateUserRequest;
import com.anhub.subscriboholic.model.entity.User;
import com.anhub.subscriboholic.model.enumerated.BillingCycleType;
import com.anhub.subscriboholic.model.enumerated.SubscriptionStatus;
import com.anhub.subscriboholic.model.enumerated.UserRole;
import com.anhub.subscriboholic.repository.SubscriptionRepository;
import com.anhub.subscriboholic.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.TimeZone;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class UserControllerIT {
    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    private Integer testUserId;

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

    @BeforeEach
    void setUp() {
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setUsername("Booker DeWitt");
        user.setPassword("Columbia123!");
        user.setEmail("booker@gmail.com");
        user.setRole(UserRole.ADMIN);

        User createdUser = userRepository.save(user);
        testUserId = createdUser.getId();
    }

    @Test
    @WithMockUser(username = "Booker DeWitt", roles = {"ADMIN"})
    void shouldCreateUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest();

        request.setUsername("Elizabeth Comstock");
        request.setPassword("RaptureIsTheBest69!");
        request.setEmail("portal_maker@gmail.com");

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())

                .andExpect(header().string("Location", "http://localhost/users/2"))

                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("Elizabeth Comstock"))
                .andExpect(jsonPath("$.email").value("portal_maker@gmail.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser(username = "Booker DeWitt", roles = {"ADMIN"})
    void shouldGetUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest();

        request.setUsername("Elizabeth Comstock");
        request.setPassword("RaptureIsTheBest69!");
        request.setEmail("portal_maker@gmail.com");

        MvcResult mvcResult = mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("Elizabeth Comstock"))
                .andExpect(jsonPath("$.email").value("portal_maker@gmail.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();

        Integer savedId = objectMapper.readTree(responseContent).get("id").asInt();

        mockMvc.perform(get("/users/{id}", savedId)
                        .with(csrf()))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(savedId))
                .andExpect(jsonPath("$.username").value("Elizabeth Comstock"))
                .andExpect(jsonPath("$.email").value("portal_maker@gmail.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.created_at").exists());
    }

    @Test
    @WithMockUser(username = "Booker DeWitt", roles = {"ADMIN"})
    void shouldDeleteUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest();

        request.setUsername("Elizabeth Comstock");
        request.setPassword("RaptureIsTheBest69!");
        request.setEmail("portal_maker@gmail.com");

        MvcResult mvcResult = mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("Elizabeth Comstock"))
                .andExpect(jsonPath("$.email").value("portal_maker@gmail.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();

        Integer savedId = objectMapper.readTree(responseContent).get("id").asInt();

        mockMvc.perform(delete("/users/{id}", savedId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
