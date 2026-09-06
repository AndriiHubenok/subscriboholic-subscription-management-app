package com.anhub.subscriboholic.subscription.dto;

import com.anhub.subscriboholic.subscription.enumerated.BillingCycleType;
import com.anhub.subscriboholic.subscription.enumerated.SubscriptionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {
    private Integer id;

    @JsonProperty("user_id")
    private Integer userId;

    private String name;

    private String description;

    private BigDecimal price;

    private String currency;

    @JsonProperty("billing_cycle")
    private BillingCycleType billingCycle;

    @JsonProperty("next_payment_date")
    private LocalDate nextPaymentDate;

    private SubscriptionStatus status;

    @UpdateTimestamp
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @CreationTimestamp
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
