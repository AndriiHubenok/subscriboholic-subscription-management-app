package com.anhub.subscriboholic.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
public class UserDTO {
    private Integer id;

    private String username;

    private String email;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
