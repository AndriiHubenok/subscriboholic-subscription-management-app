package com.anhub.subscriboholic.subscription;

import com.anhub.subscriboholic.subscription.enumerated.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {

    List<Subscription> findAllByUserId(Integer userId);

    List<Subscription> findByStatusAndNextPaymentDateBetween(
            SubscriptionStatus status,
            LocalDate startDate,
            LocalDate endDate
    );
}
