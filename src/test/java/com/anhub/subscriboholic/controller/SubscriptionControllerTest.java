package com.anhub.subscriboholic.controller;

import com.anhub.subscriboholic.model.dto.CreateSubscriptionRequest;
import com.anhub.subscriboholic.model.dto.SubscriptionDTO;
import com.anhub.subscriboholic.model.enumerated.BillingCycleType;
import com.anhub.subscriboholic.model.enumerated.SubscriptionStatus;
import com.anhub.subscriboholic.security.JwtService;
import com.anhub.subscriboholic.service.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private SubscriptionService subscriptionService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "Artem", roles = {"USER"})
    void shouldCreateUserAndReturn201() throws Exception {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setName("Gemini Pro");
        request.setDescription("Gemini Pro is a premium subscription plan that offers exclusive features and benefits to our users.");
        request.setPrice(BigDecimal.valueOf(19.99));
        request.setCurrency("USD");
        request.setBillingCycle(BillingCycleType.MONTHLY);
        request.setStatus(SubscriptionStatus.ACTIVE);
        request.setNextPaymentDate(LocalDate.now().plusMonths(1));

        SubscriptionDTO mockResponse = new SubscriptionDTO();
        mockResponse.setId(1);
        mockResponse.setUserId(1);
        mockResponse.setName("Gemini Pro");
        mockResponse.setDescription("Gemini Pro is a premium subscription plan that offers exclusive features and benefits to our users.");
        mockResponse.setPrice(BigDecimal.valueOf(19.99));
        mockResponse.setCurrency("USD");
        mockResponse.setBillingCycle(BillingCycleType.MONTHLY);
        mockResponse.setStatus(SubscriptionStatus.ACTIVE);
        mockResponse.setNextPaymentDate(LocalDate.now().plusMonths(1));
        mockResponse.setCreatedAt(LocalDateTime.now());
        mockResponse.setUpdatedAt(LocalDateTime.now());

        Mockito.when(subscriptionService.createSubscription(any(CreateSubscriptionRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/subscriptions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())

                .andExpect(header().string("Location", "http://localhost/subscriptions/1"))

                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.user_id").value(1))
                .andExpect(jsonPath("$.name").value("Gemini Pro"))
                .andExpect(jsonPath("$.description").value("Gemini Pro is a premium subscription plan that offers exclusive features and benefits to our users."))
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.billing_cycle").value(BillingCycleType.MONTHLY.name()))
                .andExpect(jsonPath("$.status").value(SubscriptionStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.next_payment_date").exists())
                .andExpect(jsonPath("$.created_at").exists())
                .andExpect(jsonPath("$.updated_at").exists());
    }

    @Test
    @WithMockUser(username = "Artem", roles = {"USER"})
    void shouldGetSubscriptionAndReturn200() throws Exception {
        SubscriptionDTO mockResponse = new SubscriptionDTO();
        mockResponse.setId(1);
        mockResponse.setUserId(1);
        mockResponse.setName("Gemini Pro");
        mockResponse.setDescription("Gemini Pro is a premium subscription plan that offers exclusive features and benefits to our users.");
        mockResponse.setPrice(BigDecimal.valueOf(19.99));
        mockResponse.setCurrency("USD");
        mockResponse.setBillingCycle(BillingCycleType.MONTHLY);
        mockResponse.setStatus(SubscriptionStatus.ACTIVE);
        mockResponse.setNextPaymentDate(LocalDate.now().plusMonths(1));
        mockResponse.setCreatedAt(LocalDateTime.now());
        mockResponse.setUpdatedAt(LocalDateTime.now());

        Mockito.when(subscriptionService.getSubscriptionById(1)).thenReturn(mockResponse);

        mockMvc.perform(get("/subscriptions/{id}", 1)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.user_id").value(1))
                .andExpect(jsonPath("$.name").value("Gemini Pro"))
                .andExpect(jsonPath("$.description").value("Gemini Pro is a premium subscription plan that offers exclusive features and benefits to our users."))
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.billing_cycle").value(BillingCycleType.MONTHLY.name()))
                .andExpect(jsonPath("$.status").value(SubscriptionStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.next_payment_date").exists())
                .andExpect(jsonPath("$.created_at").exists())
                .andExpect(jsonPath("$.updated_at").exists());
    }

    @Test
    @WithMockUser(username = "Artem", roles = {"USER"})
    void shouldUpdateSubscriptionAndReturn200() throws Exception {
        CreateSubscriptionRequest putRequest = new CreateSubscriptionRequest();
        putRequest.setName("Gemini Pro");
        putRequest.setDescription("Gemini Pro is a premium subscription plan that offers exclusive features and benefits to our users.");
        putRequest.setPrice(BigDecimal.valueOf(190.99));
        putRequest.setCurrency("USD");
        putRequest.setBillingCycle(BillingCycleType.YEARLY);
        putRequest.setStatus(SubscriptionStatus.ACTIVE);
        putRequest.setNextPaymentDate(LocalDate.now().plusYears(1));

        SubscriptionDTO mockPutResponse = new SubscriptionDTO();
        mockPutResponse.setId(1);
        mockPutResponse.setUserId(1);
        mockPutResponse.setName("Gemini Pro");
        mockPutResponse.setDescription("Gemini Pro is a premium subscription plan that offers exclusive features and benefits to our users.");
        mockPutResponse.setPrice(BigDecimal.valueOf(190.99));
        mockPutResponse.setCurrency("USD");
        mockPutResponse.setBillingCycle(BillingCycleType.YEARLY);
        mockPutResponse.setStatus(SubscriptionStatus.ACTIVE);
        mockPutResponse.setNextPaymentDate(LocalDate.now().plusYears(1));
        mockPutResponse.setCreatedAt(LocalDateTime.now());
        mockPutResponse.setUpdatedAt(LocalDateTime.now());

        Mockito.when(subscriptionService.updateSubscription(Mockito.eq(1), any(CreateSubscriptionRequest.class)))
                .thenReturn(mockPutResponse);

        mockMvc.perform(put("/subscriptions/{id}", 1)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.user_id").value(1))
                .andExpect(jsonPath("$.name").value("Gemini Pro"))
                .andExpect(jsonPath("$.description").value("Gemini Pro is a premium subscription plan that offers exclusive features and benefits to our users."))
                .andExpect(jsonPath("$.price").value(190.99))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.billing_cycle").value(BillingCycleType.YEARLY.name()))
                .andExpect(jsonPath("$.status").value(SubscriptionStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.next_payment_date").exists())
                .andExpect(jsonPath("$.created_at").exists())
                .andExpect(jsonPath("$.updated_at").exists());
    }

    @Test
    @WithMockUser(username = "Artem", roles = {"USER"})
    void shouldDeleteSubscriptionAndReturn204() throws Exception {
        boolean mockDeleteResponse = true;
        Mockito.when(subscriptionService.deleteSubscriptionById(Mockito.eq(1)))
                .thenReturn(mockDeleteResponse);

        mockMvc.perform(delete("/subscriptions/{id}", 1)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithAnonymousUser
    void shouldReturn401WhenGettingSubscriptionWithoutAuth() throws Exception {
        mockMvc.perform(get("/subscriptions/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    void shouldReturn401WhenDeletingSubscriptionWithoutAuth() throws Exception {
        mockMvc.perform(delete("/subscriptions/1").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "Artem", roles = {"USER"})
    void shouldReturn200WhenGettingSubscriptionWithValidAuth() throws Exception {
        Mockito.when(subscriptionService.getSubscriptionById(1))
                .thenReturn(new SubscriptionDTO());

        mockMvc.perform(get("/subscriptions/1"))
                .andExpect(status().isOk());
    }
}
