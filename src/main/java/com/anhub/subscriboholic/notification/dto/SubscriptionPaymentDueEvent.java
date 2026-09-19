package com.anhub.subscriboholic.notification.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SubscriptionPaymentDueEvent(
        Integer subscriptionId,
        String subscriptionName,
        BigDecimal price,
        String currency,
        LocalDate paymentDueDate
) {
}
