package com.anhub.subscriboholic.notification.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SubscriptionPaymentDueEvent(
        UUID eventId,
        Integer userId,
        Integer subscriptionId,
        String subscriptionName,
        BigDecimal amount,
        String currency,
        LocalDate paymentDueDate
) {
    public static SubscriptionPaymentDueEvent of(Integer userId, Integer subscriptionId, String name,
                                                 BigDecimal amount, String currency, LocalDate dueDate) {
        return new SubscriptionPaymentDueEvent(
                UUID.randomUUID(), userId, subscriptionId, name, amount, currency, dueDate
        );
    }
}
