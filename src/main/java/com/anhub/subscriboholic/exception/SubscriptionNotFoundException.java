package com.anhub.subscriboholic.exception;

public class SubscriptionNotFoundException extends RuntimeException {
    public SubscriptionNotFoundException(Integer id) {
        super("Subscription with " + id + " not found");
    }
}
