package com.anhub.subscriboholic.notification.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisteredEvent {
    private UUID eventId;
    private Integer userId;
    private String userEmail;
    private String username;
    private String verificationToken;

    public UserRegisteredEvent(Integer userId, String userEmail, String username) {
        this.eventId = UUID.randomUUID();
        this.userId = userId;
        this.userEmail = userEmail;
        this.username = username;
    }
}
