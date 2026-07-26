package com.anhub.subscriboholic.model.dto;

import com.anhub.subscriboholic.model.enumerated.BillingCycleType;
import com.anhub.subscriboholic.model.enumerated.SubscriptionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

//CREATE TABLE IF NOT EXISTS subscriptions (
//        id SERIAL PRIMARY KEY,
//        user_id INT NOT NULL,
//        name VARCHAR(200) NOT NULL,
//        description TEXT,
//        price DECIMAL(10, 2) NOT NULL,
//        currency VARCHAR(10) NOT NULL,
//        billing_cycle BILLING_CYCLE_TYPE NOT NULL,
//        next_payment_date DATE NOT NULL,
//        status SUBSCRIPTION_STATUS NOT NULL,
//        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
//
//        CONSTRAINT fk_subscription_user
//                FOREIGN KEY (user_id)
//                REFERENCES users(id)
//                ON DELETE CASCADE
//);

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
