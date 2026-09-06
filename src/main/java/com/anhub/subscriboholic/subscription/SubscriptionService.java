package com.anhub.subscriboholic.subscription;

import com.anhub.subscriboholic.subscription.exception.SubscriptionNotFoundException;
import com.anhub.subscriboholic.user.exception.UnauthorizedSubscriptionAccessException;
import com.anhub.subscriboholic.subscription.dto.CreateSubscriptionRequest;
import com.anhub.subscriboholic.subscription.dto.SubscriptionDTO;
import com.anhub.subscriboholic.user.User;
import com.anhub.subscriboholic.user.UserRepository;
import com.anhub.subscriboholic.auth.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
class SubscriptionService {
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
                .orElseThrow(() -> new SubscriptionNotFoundException(id));

        if (!subscription.getUser().getUsername().equals(authService.getCurrentUserUsername())) {
            throw new UnauthorizedSubscriptionAccessException();
        }
        return subscription;
    }
}
