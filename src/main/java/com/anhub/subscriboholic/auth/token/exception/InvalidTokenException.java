package com.anhub.subscriboholic.auth.token.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super("Invalid verification token: " + message);
    }
}
