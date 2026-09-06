package com.anhub.subscriboholic.subscription.exception;

public class SubscriptionNotFoundException extends RuntimeException {
    public SubscriptionNotFoundException(Integer id) {
        super("Subscription with " + id + " not found");
    }
}
