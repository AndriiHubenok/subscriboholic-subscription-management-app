package com.anhub.subscriboholic.service;

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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubscriptionServiceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private SubscriptionService subscriptionService;

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
        user.setUsername("Adam Jensen");
        user.setPassword("Icarus228?");
        user.setEmail("neveraskedforthisman@gmail.com");
        user.setRole(UserRole.USER);

        User createdUser = userRepository.save(user);
        testUserId = createdUser.getId();
    }

    @Test
    @WithMockUser(username = "Adam Jensen")
    void shouldCreateSubscription() {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();

        request.setName("Netflix Pro");
        request.setDescription("Netflix and chill");
        request.setPrice(BigDecimal.valueOf(15.99));
        request.setCurrency("USD");
        request.setBillingCycle(BillingCycleType.MONTHLY);
        request.setNextPaymentDate(LocalDate.now().plusDays(30));
        request.setStatus(SubscriptionStatus.ACTIVE);

        SubscriptionDTO createdSubscription = subscriptionService.createSubscription(request);

        assertEquals("Netflix Pro", createdSubscription.getName());
        assertEquals("Netflix and chill", createdSubscription.getDescription());
        assertEquals(BigDecimal.valueOf(15.99), createdSubscription.getPrice());
        assertEquals("USD", createdSubscription.getCurrency());
        assertEquals(BillingCycleType.MONTHLY, createdSubscription.getBillingCycle());
        assertEquals(LocalDate.now().plusDays(30), createdSubscription.getNextPaymentDate());
        assertEquals(SubscriptionStatus.ACTIVE, createdSubscription.getStatus());
        assertEquals(testUserId, createdSubscription.getUserId());
        assertNotNull(createdSubscription.getId());
        assertNotNull(createdSubscription.getCreatedAt());
        assertNotNull(createdSubscription.getUpdatedAt());
    }

    @Test
    @WithMockUser(username = "Adam Jensen")
    void shouldGetSubscription() {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();

        request.setName("Netflix Pro");
        request.setDescription("Netflix and chill");
        request.setPrice(BigDecimal.valueOf(15.99));
        request.setCurrency("USD");
        request.setBillingCycle(BillingCycleType.MONTHLY);
        request.setNextPaymentDate(LocalDate.now().plusDays(30));
        request.setStatus(SubscriptionStatus.ACTIVE);

        Integer id = subscriptionService.createSubscription(request).getId();
        SubscriptionDTO retrievedSubscription = subscriptionService.getSubscriptionById(id);

        assertEquals(id, retrievedSubscription.getId());
        assertEquals("Netflix Pro", retrievedSubscription.getName());
        assertEquals("Netflix and chill", retrievedSubscription.getDescription());
        assertEquals(BigDecimal.valueOf(15.99), retrievedSubscription.getPrice());
        assertEquals("USD", retrievedSubscription.getCurrency());
        assertEquals(BillingCycleType.MONTHLY, retrievedSubscription.getBillingCycle());
        assertEquals(LocalDate.now().plusDays(30), retrievedSubscription.getNextPaymentDate());
        assertEquals(SubscriptionStatus.ACTIVE, retrievedSubscription.getStatus());
        assertEquals(testUserId, retrievedSubscription.getUserId());
        assertNotNull(retrievedSubscription.getCreatedAt());
        assertNotNull(retrievedSubscription.getUpdatedAt());
    }

    @Test
    @WithMockUser(username = "Adam Jensen")
    void shouldUpdateSubscription() {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();

        request.setName("Netflix Pro");
        request.setDescription("Netflix and chill");
        request.setPrice(BigDecimal.valueOf(15.99));
        request.setCurrency("USD");
        request.setBillingCycle(BillingCycleType.MONTHLY);
        request.setNextPaymentDate(LocalDate.now().plusDays(30));
        request.setStatus(SubscriptionStatus.ACTIVE);

        Integer id = subscriptionService.createSubscription(request).getId();

        CreateSubscriptionRequest updateRequest = new CreateSubscriptionRequest();

        updateRequest.setName("Netflix Pro");
        updateRequest.setDescription("Netflix and chill");
        updateRequest.setPrice(BigDecimal.valueOf(150.99));
        updateRequest.setCurrency("Tögrög");
        updateRequest.setBillingCycle(BillingCycleType.MONTHLY);
        updateRequest.setNextPaymentDate(LocalDate.now().plusDays(30));
        updateRequest.setStatus(SubscriptionStatus.ACTIVE);

        SubscriptionDTO updatedSubscription = subscriptionService.updateSubscription(id, updateRequest);

        assertEquals(id, updatedSubscription.getId());
        assertEquals("Netflix Pro", updatedSubscription.getName());
        assertEquals("Netflix and chill", updatedSubscription.getDescription());
        assertEquals(BigDecimal.valueOf(150.99), updatedSubscription.getPrice());
        assertEquals("Tögrög", updatedSubscription.getCurrency());
        assertEquals(BillingCycleType.MONTHLY, updatedSubscription.getBillingCycle());
        assertEquals(LocalDate.now().plusDays(30), updatedSubscription.getNextPaymentDate());
        assertEquals(SubscriptionStatus.ACTIVE, updatedSubscription.getStatus());
        assertEquals(testUserId, updatedSubscription.getUserId());
        assertNotNull(updatedSubscription.getCreatedAt());
        assertNotNull(updatedSubscription.getUpdatedAt());
    }

    @Test
    @WithMockUser(username = "Adam Jensen")
    void shouldDeleteSubscription() {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();

        request.setName("Netflix Pro");
        request.setDescription("Netflix and chill");
        request.setPrice(BigDecimal.valueOf(15.99));
        request.setCurrency("USD");
        request.setBillingCycle(BillingCycleType.MONTHLY);
        request.setNextPaymentDate(LocalDate.now().plusDays(30));
        request.setStatus(SubscriptionStatus.ACTIVE);

        Integer id = subscriptionService.createSubscription(request).getId();

        subscriptionService.deleteSubscriptionById(id);

        assertThrows(SubscriptionNotFoundException.class, () -> subscriptionService.getSubscriptionById(id));
    }
}
