package com.anhub.subscriboholic.service;

import com.anhub.subscriboholic.mapper.SubscriptionMapper;
import com.anhub.subscriboholic.model.dto.CreateSubscriptionRequest;
import com.anhub.subscriboholic.model.dto.SubscriptionDTO;
import com.anhub.subscriboholic.model.entity.Subscription;
import com.anhub.subscriboholic.model.entity.User;
import com.anhub.subscriboholic.model.enumerated.BillingCycleType;
import com.anhub.subscriboholic.model.enumerated.SubscriptionStatus;
import com.anhub.subscriboholic.repository.SubscriptionRepository;
import com.anhub.subscriboholic.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void shouldCreateSubscription() {

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setName("Netflix Pro");
        request.setDescription("Netflix and chill");
        request.setPrice(BigDecimal.valueOf(15.99));
        request.setCurrency("USD");
        request.setBillingCycle(BillingCycleType.MONTHLY);
        request.setNextPaymentDate(LocalDate.now().plusDays(30));
        request.setStatus(SubscriptionStatus.ACTIVE);

        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("Adam Jensen");

        Subscription mockSubscription = new Subscription();
        mockSubscription.setId(1);
        mockSubscription.setUser(mockUser);

        SubscriptionDTO mockDto = new SubscriptionDTO();
        mockDto.setId(1);
        mockDto.setUserId(1);
        mockDto.setName("Netflix Pro");
        mockDto.setDescription("Netflix and chill");
        mockDto.setPrice(BigDecimal.valueOf(15.99));
        mockDto.setCurrency("USD");
        mockDto.setBillingCycle(BillingCycleType.MONTHLY);
        mockDto.setNextPaymentDate(LocalDate.now().plusDays(30));
        mockDto.setStatus(SubscriptionStatus.ACTIVE);
        mockDto.setCreatedAt(LocalDateTime.now());
        mockDto.setUpdatedAt(LocalDateTime.now());


        Mockito.when(subscriptionMapper.toEntity(request)).thenReturn(mockSubscription);

        Mockito.when(authService.getCurrentUserUsername()).thenReturn("Adam Jensen");
        Mockito.when(userRepository.findByUsername("Adam Jensen")).thenReturn(Optional.of(mockUser));

        Mockito.when(subscriptionRepository.save(any(Subscription.class))).thenReturn(mockSubscription);

        Mockito.when(subscriptionMapper.toDTO(mockSubscription)).thenReturn(mockDto);

        SubscriptionDTO createdSubscription = subscriptionService.createSubscription(request);

        assertNotNull(createdSubscription);
        assertEquals("Netflix Pro", createdSubscription.getName());
        assertEquals("Netflix and chill", createdSubscription.getDescription());
        assertEquals(BigDecimal.valueOf(15.99), createdSubscription.getPrice());
        assertEquals("USD", createdSubscription.getCurrency());
        assertEquals(BillingCycleType.MONTHLY, createdSubscription.getBillingCycle());
        assertEquals(SubscriptionStatus.ACTIVE, createdSubscription.getStatus());
        assertEquals(1, createdSubscription.getUserId());
        assertNotNull(createdSubscription.getId());
    }

    @Test
    void shouldGetSubscription() {

        Integer subscriptionId = 1;
        String currentUsername = "Adam Jensen";

        User mockUser = new User();
        mockUser.setUsername(currentUsername);

        Subscription mockSubscription = new Subscription();
        mockSubscription.setId(subscriptionId);
        mockSubscription.setName("Netflix Pro");
        mockSubscription.setPrice(BigDecimal.valueOf(15.99));
        mockSubscription.setUser(mockUser);

        SubscriptionDTO mockDto = new SubscriptionDTO();
        mockDto.setId(subscriptionId);
        mockDto.setName("Netflix Pro");
        mockDto.setPrice(BigDecimal.valueOf(15.99));
        mockDto.setUserId(1);

        Mockito.when(subscriptionRepository.findById(subscriptionId))
                .thenReturn(Optional.of(mockSubscription));

        Mockito.when(authService.getCurrentUserUsername())
                .thenReturn(currentUsername);

        Mockito.when(subscriptionMapper.toDTO(mockSubscription))
                .thenReturn(mockDto);

        SubscriptionDTO retrievedSubscription = subscriptionService.getSubscriptionById(subscriptionId);

        assertNotNull(retrievedSubscription);
        assertEquals(subscriptionId, retrievedSubscription.getId());
        assertEquals("Netflix Pro", retrievedSubscription.getName());
        assertEquals(BigDecimal.valueOf(15.99), retrievedSubscription.getPrice());
        Mockito.verify(subscriptionRepository, Mockito.times(1)).findById(subscriptionId);
        Mockito.verify(authService, Mockito.times(1)).getCurrentUserUsername();
    }

    @Test
    void shouldUpdateSubscription() {

        Integer subscriptionId = 1;
        String currentUsername = "Adam Jensen";

        CreateSubscriptionRequest updateRequest = new CreateSubscriptionRequest();
        updateRequest.setName("Netflix Pro");
        updateRequest.setDescription("Netflix and chill");
        updateRequest.setPrice(BigDecimal.valueOf(150.99));
        updateRequest.setCurrency("Tögrög");
        updateRequest.setBillingCycle(BillingCycleType.MONTHLY);
        updateRequest.setNextPaymentDate(LocalDate.now().plusDays(30));
        updateRequest.setStatus(SubscriptionStatus.ACTIVE);

        User mockUser = new User();
        mockUser.setUsername(currentUsername);

        Subscription existingSubscription = new Subscription();
        existingSubscription.setId(subscriptionId);
        existingSubscription.setUser(mockUser);

        SubscriptionDTO mockUpdatedDto = new SubscriptionDTO();
        mockUpdatedDto.setId(subscriptionId);
        mockUpdatedDto.setName("Netflix Pro");
        mockUpdatedDto.setPrice(BigDecimal.valueOf(150.99));
        mockUpdatedDto.setCurrency("Tögrög");
        mockUpdatedDto.setBillingCycle(BillingCycleType.MONTHLY);
        mockUpdatedDto.setStatus(SubscriptionStatus.ACTIVE);
        mockUpdatedDto.setUserId(1);

        Mockito.when(subscriptionRepository.findById(subscriptionId))
                .thenReturn(Optional.of(existingSubscription));

        Mockito.when(authService.getCurrentUserUsername())
                .thenReturn(currentUsername);

        Mockito.when(subscriptionRepository.save(existingSubscription))
                .thenReturn(existingSubscription);

        Mockito.when(subscriptionMapper.toDTO(existingSubscription))
                .thenReturn(mockUpdatedDto);

        SubscriptionDTO updatedSubscription = subscriptionService.updateSubscription(subscriptionId, updateRequest);

        assertNotNull(updatedSubscription);
        assertEquals(subscriptionId, updatedSubscription.getId());
        assertEquals("Netflix Pro", updatedSubscription.getName());
        assertEquals(BigDecimal.valueOf(150.99), updatedSubscription.getPrice());
        assertEquals("Tögrög", updatedSubscription.getCurrency());

        Mockito.verify(subscriptionRepository, Mockito.times(1)).findById(subscriptionId);
        Mockito.verify(subscriptionRepository, Mockito.times(1)).save(existingSubscription);
    }

    @Test
    void shouldDeleteSubscription() {

        Integer subscriptionId = 1;
        String currentUsername = "Adam Jensen";

        User mockUser = new User();
        mockUser.setUsername(currentUsername);

        Subscription mockSubscription = new Subscription();
        mockSubscription.setId(subscriptionId);
        mockSubscription.setUser(mockUser);

        Mockito.when(subscriptionRepository.findById(subscriptionId))
                .thenReturn(Optional.of(mockSubscription));

        Mockito.when(authService.getCurrentUserUsername())
                .thenReturn(currentUsername);

        boolean isDeleted = subscriptionService.deleteSubscriptionById(subscriptionId);

        assertTrue(isDeleted);

        Mockito.verify(subscriptionRepository, Mockito.times(1)).delete(mockSubscription);
    }
}
