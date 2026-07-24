package com.anhub.subscriboholic.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//CREATE TABLE IF NOT EXISTS users (
//        id SERIAL PRIMARY KEY,
//        username VARCHAR(50) NOT NULL UNIQUE,
//        email VARCHAR(100) NOT NULL UNIQUE,
//        password_hash VARCHAR(255) NOT NULL,
//        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
//);

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
