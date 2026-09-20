package com.anhub.subscriboholic.auth.token;

import com.anhub.subscriboholic.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "email_verification_tokens", indexes = {
        @Index(name = "idx_token_value", columnList = "token", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant createdAt;

    public VerificationToken(String token, User user, long validityHours) {
        this.token = token;
        this.user = user;
        this.createdAt = Instant.now();
        this.expiresAt = this.createdAt.plusSeconds(validityHours * 3600);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }
}
