package com.anhub.subscriboholic.repository;

import com.anhub.subscriboholic.model.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
}
