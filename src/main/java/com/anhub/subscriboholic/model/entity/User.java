package com.anhub.subscriboholic.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//CREATE TABLE IF NOT EXISTS users (
//        id SERIAL PRIMARY KEY,
//        username VARCHAR(50) NOT NULL UNIQUE,
//        email VARCHAR(100) NOT NULL UNIQUE,
//        password_hash VARCHAR(255) NOT NULL,
//        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
//);

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscription> subscriptions = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
