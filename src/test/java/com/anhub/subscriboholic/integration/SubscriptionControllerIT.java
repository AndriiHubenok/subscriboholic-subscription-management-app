package com.anhub.subscriboholic.integration;

import com.anhub.subscriboholic.exception.SubscriptionNotFoundException;
import com.anhub.subscriboholic.model.dto.CreateSubscriptionRequest;
import com.anhub.subscriboholic.model.dto.SubscriptionDTO;
import com.anhub.subscriboholic.model.dto.UserDTO;
import com.anhub.subscriboholic.model.entity.User;
import com.anhub.subscriboholic.model.enumerated.BillingCycleType;
import com.anhub.subscriboholic.model.enumerated.SubscriptionStatus;
import com.anhub.subscriboholic.model.enumerated.UserRole;
import com.anhub.subscriboholic.repository.SubscriptionRepository;
import com.anhub.subscriboholic.repository.UserRepository;
import com.anhub.subscriboholic.service.SubscriptionService;
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
public class SubscriptionControllerIT {

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
        user.setUsername("Courier 6");
        user.setPassword("Benny_loh36");
        user.setEmail("nuclearwinter_enjoyer@gmail.com");
        user.setRole(UserRole.USER);

        User createdUser = userRepository.save(user);
        testUserId = createdUser.getId();
    }

    @Test
    @WithMockUser(username = "Courier 6")
    void shouldCreateSubscription() throws Exception {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();

        request.setName("Netflix Pro");
        request.setDescription("Netflix and chill");
        request.setPrice(BigDecimal.valueOf(15.99));
        request.setCurrency("USD");
        request.setBillingCycle(BillingCycleType.MONTHLY);
        request.setNextPaymentDate(LocalDate.now().plusDays(30));
        request.setStatus(SubscriptionStatus.ACTIVE);

        mockMvc.perform(post("/subscriptions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())

                .andExpect(header().string("Location", "http://localhost/subscriptions/4"))

                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Netflix Pro"))
                .andExpect(jsonPath("$.description").value("Netflix and chill"))
                .andExpect(jsonPath("$.price").value(15.99))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.billing_cycle").value("MONTHLY"))
                .andExpect(jsonPath("$.next_payment_date").value(LocalDate.now().plusDays(30).toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.user_id").value(testUserId));
    }

    @Test
    @WithMockUser(username = "Courier 6")
    void shouldGetSubscription() throws Exception {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();

        request.setName("Netflix Pro");
        request.setDescription("Netflix and chill");
        request.setPrice(BigDecimal.valueOf(15.99));
        request.setCurrency("USD");
        request.setBillingCycle(BillingCycleType.MONTHLY);
        request.setNextPaymentDate(LocalDate.now().plusDays(30));
        request.setStatus(SubscriptionStatus.ACTIVE);

        MvcResult mvcResult = mockMvc.perform(post("/subscriptions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Netflix Pro"))
                .andExpect(jsonPath("$.user_id").value(testUserId))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();

        Integer savedId = objectMapper.readTree(responseContent).get("id").asInt();

        mockMvc.perform(get("/subscriptions/{id}", savedId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedId))
                .andExpect(jsonPath("$.name").value("Netflix Pro"))
                .andExpect(jsonPath("$.description").value("Netflix and chill"))
                .andExpect(jsonPath("$.price").value(15.99))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.billing_cycle").value("MONTHLY"))
                .andExpect(jsonPath("$.next_payment_date").value(LocalDate.now().plusDays(30).toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.user_id").value(testUserId))
                .andExpect(jsonPath("$.created_at").exists());
    }

    @Test
    @WithMockUser(username = "Courier 6")
    void shouldUpdateSubscription() throws Exception {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();

        request.setName("Netflix Pro");
        request.setDescription("Netflix and chill");
        request.setPrice(BigDecimal.valueOf(15.99));
        request.setCurrency("USD");
        request.setBillingCycle(BillingCycleType.MONTHLY);
        request.setNextPaymentDate(LocalDate.now().plusDays(30));
        request.setStatus(SubscriptionStatus.ACTIVE);

        MvcResult mvcResult = mockMvc.perform(post("/subscriptions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Netflix Pro"))
                .andExpect(jsonPath("$.user_id").value(testUserId))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();

        Integer savedId = objectMapper.readTree(responseContent).get("id").asInt();

        CreateSubscriptionRequest updateRequest = new CreateSubscriptionRequest();

        updateRequest.setName("Netflix Pro");
        updateRequest.setDescription("Netflix and chill");
        updateRequest.setPrice(BigDecimal.valueOf(150.99));
        updateRequest.setCurrency("Tögrög");
        updateRequest.setBillingCycle(BillingCycleType.YEARLY);
        updateRequest.setNextPaymentDate(LocalDate.now().plusYears(1));
        updateRequest.setStatus(SubscriptionStatus.ACTIVE);

        mockMvc.perform(put("/subscriptions/{id}", savedId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedId))
                .andExpect(jsonPath("$.name").value("Netflix Pro"))
                .andExpect(jsonPath("$.description").value("Netflix and chill"))
                .andExpect(jsonPath("$.price").value(150.99))
                .andExpect(jsonPath("$.currency").value("Tögrög"))
                .andExpect(jsonPath("$.billing_cycle").value("YEARLY"))
                .andExpect(jsonPath("$.next_payment_date").value(LocalDate.now().plusYears(1).toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.user_id").value(testUserId))
                .andExpect(jsonPath("$.created_at").exists());
    }

    @Test
    @WithMockUser(username = "Courier 6")
    void shouldDeleteSubscription() throws Exception {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();

        request.setName("Netflix Pro");
        request.setDescription("Netflix and chill");
        request.setPrice(BigDecimal.valueOf(15.99));
        request.setCurrency("USD");
        request.setBillingCycle(BillingCycleType.MONTHLY);
        request.setNextPaymentDate(LocalDate.now().plusDays(30));
        request.setStatus(SubscriptionStatus.ACTIVE);

        MvcResult mvcResult = mockMvc.perform(post("/subscriptions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Netflix Pro"))
                .andExpect(jsonPath("$.user_id").value(testUserId))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();

        Integer savedId = objectMapper.readTree(responseContent).get("id").asInt();

        mockMvc.perform(delete("/subscriptions/{id}", savedId)
                        .with(csrf()))

                .andExpect(status().isNoContent());
    }
}
