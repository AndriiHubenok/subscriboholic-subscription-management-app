package com.anhub.subscriboholic.model.dto;

import com.anhub.subscriboholic.model.enumerated.BillingCycleType;
import com.anhub.subscriboholic.model.enumerated.SubscriptionStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class CreateSubscriptionRequest {
    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotBlank(message = "Subscription name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 2000, message = "Description is too long")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be strictly greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format")
    private BigDecimal price;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 10, message = "Currency must be between 3 and 10 characters")
    private String currency;

    @NotNull(message = "Billing cycle is required")
    private BillingCycleType billingCycle;

    @NotNull(message = "Next payment date is required")
    @FutureOrPresent(message = "Payment date cannot be in the past")
    private LocalDate nextPaymentDate;

    @NotNull(message = "Subscription status is required")
    private SubscriptionStatus status;
}
