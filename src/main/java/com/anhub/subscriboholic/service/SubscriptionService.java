package com.anhub.subscriboholic.service;

import com.anhub.subscriboholic.mapper.SubscriptionMapper;
import com.anhub.subscriboholic.model.dto.CreateSubscriptionRequest;
import com.anhub.subscriboholic.model.dto.SubscriptionDTO;
import com.anhub.subscriboholic.model.entity.Subscription;
import com.anhub.subscriboholic.repository.SubscriptionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;

    public SubscriptionDTO createSubscription(CreateSubscriptionRequest createSubscriptionRequest) {
        Subscription subscription = subscriptionRepository.save(subscriptionMapper.toEntity(createSubscriptionRequest));
        return subscriptionMapper.toDTO(subscription);
    }

    public SubscriptionDTO getSubscriptionById(Integer id) {
        return subscriptionMapper.toDTO(subscriptionRepository.findById(id).orElse(null));
    }
}
