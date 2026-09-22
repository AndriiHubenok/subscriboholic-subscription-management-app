package com.anhub.subscriboholic.notification.dto.subscription;

import java.util.List;
import java.util.UUID;

public record ListSubscriptionPaymentsDueEvent(
        UUID eventId,
        String userEmail,
        List<SubscriptionPaymentDueEvent> subscriptions
) {
    public static ListSubscriptionPaymentsDueEvent of(String userEmail, List<SubscriptionPaymentDueEvent> subscriptions) {
        return new ListSubscriptionPaymentsDueEvent(
                UUID.randomUUID(), userEmail, subscriptions
        );
    }
}
