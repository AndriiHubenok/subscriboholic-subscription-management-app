package com.anhub.subscriboholic.model.entity;

//CREATE TABLE IF NOT EXISTS subscriptions (
//        id SERIAL PRIMARY KEY,
//        user_id INT NOT NULL,
//        name VARCHAR(200) NOT NULL,
//        description TEXT,
//        price DECIMAL(10, 2) NOT NULL,
//        currency VARCHAR(10) NOT NULL,
//        billing_cycle BILLING_CYCLE_TYPE NOT NULL,
//        next_payment_date DATE NOT NULL,
//        status SUBSCRIPTION_STATUS NOT NULL,
//        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
//
//        CONSTRAINT fk_subscription_user
//                FOREIGN KEY (user_id)
//                REFERENCES users(id)
//                ON DELETE CASCADE
//);

import com.anhub.subscriboholic.model.enumerated.BillingCycleType;
import com.anhub.subscriboholic.model.enumerated.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "billing_cycle", nullable = false)
    private BillingCycleType billingCycle;

    @Column(name = "next_payment_date", nullable = false)
    private LocalDate nextPaymentDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;
}
