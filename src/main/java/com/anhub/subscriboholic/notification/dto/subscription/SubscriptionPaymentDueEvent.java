package com.anhub.subscriboholic.notification.dto.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionPaymentDueEvent(
        Integer subscriptionId,
        String subscriptionName,
        BigDecimal price,
        String currency,
        LocalDate paymentDueDate
) {
}
