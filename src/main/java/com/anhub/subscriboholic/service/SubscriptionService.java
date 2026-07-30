package com.anhub.subscriboholic.service;

import com.anhub.subscriboholic.mapper.SubscriptionMapper;
import com.anhub.subscriboholic.model.dto.CreateSubscriptionRequest;
import com.anhub.subscriboholic.model.dto.SubscriptionDTO;
import com.anhub.subscriboholic.model.entity.Subscription;
import com.anhub.subscriboholic.model.entity.User;
import com.anhub.subscriboholic.repository.SubscriptionRepository;
import com.anhub.subscriboholic.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final AuthService authService;

    public SubscriptionDTO createSubscription(CreateSubscriptionRequest request) {
        Subscription subscription = subscriptionMapper.toEntity(request);

        User user = userRepository.findByUsername(authService.getCurrentUserUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        subscription.setUser(user);
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toDTO(savedSubscription);
    }

    public SubscriptionDTO getSubscriptionById(Integer id) {
        return subscriptionMapper.toDTO(getSubscription(id));
    }

    public SubscriptionDTO updateSubscription(Integer id, CreateSubscriptionRequest request) {

        Subscription subscription = getSubscription(id);

        subscriptionMapper.updateEntityFromDto(request, subscription);
        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toDTO(updatedSubscription);
    }

    public boolean deleteSubscriptionById(Integer id) {
        Subscription subscription = getSubscription(id);

        subscriptionRepository.delete(subscription);
        return true;
    }

    private Subscription getSubscription(Integer id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription with ID " + id + " not found"));

        if (!subscription.getUser().getUsername().equals(authService.getCurrentUserUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to update this subscription");
        }
        return subscription;
    }
}
