package com.anhub.subscriboholic.exception;

public class UnauthorizedSubscriptionAccessException extends RuntimeException {
    public UnauthorizedSubscriptionAccessException() {
        super("You have no permission to access this subscription");
    }
}
