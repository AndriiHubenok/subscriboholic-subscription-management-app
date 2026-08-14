package com.anhub.subscriboholic.validation;

import com.anhub.subscriboholic.model.dto.CreateUserRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateUserRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldPassValidationWhenAllFieldsAreValid() {

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Adam Jensen");
        request.setEmail("neveraskedforthis@gmail.com");
        request.setPassword("SecurePass123!");

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenEmailIsInvalid() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Adam Jensen");
        request.setPassword("SecurePass123!");

        request.setEmail("invalid-email-format");

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());

        ConstraintViolation<CreateUserRequest> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
    }

    @Test
    void shouldFailValidationWhenUsernameIsBlank() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("");
        request.setEmail("adam@test.com");
        request.setPassword("SecurePass123!");

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("username", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFailValidationWhenPasswordIsTooShort() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Adam Jensen");
        request.setEmail("adam@test.com");
        request.setPassword("123");

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("password", violations.iterator().next().getPropertyPath().toString());
    }
}
