package com.anhub.subscriboholic.auth.token.exception;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message) {
        super("Verification token is expired: " + message);
    }
}
