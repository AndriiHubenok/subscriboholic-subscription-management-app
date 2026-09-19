package com.anhub.subscriboholic.subscription;

import com.anhub.subscriboholic.notification.dto.ListSubscriptionPaymentsDueEvent;
import com.anhub.subscriboholic.notification.dto.SubscriptionPaymentDueEvent;
import com.anhub.subscriboholic.notification.producer.NotificationEventProducer;
import com.anhub.subscriboholic.subscription.enumerated.SubscriptionStatus;
import com.anhub.subscriboholic.subscription.exception.SubscriptionNotFoundException;
import com.anhub.subscriboholic.user.exception.UnauthorizedSubscriptionAccessException;
import com.anhub.subscriboholic.subscription.dto.CreateSubscriptionRequest;
import com.anhub.subscriboholic.subscription.dto.SubscriptionDTO;
import com.anhub.subscriboholic.user.User;
import com.anhub.subscriboholic.user.UserRepository;
import com.anhub.subscriboholic.auth.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final AuthService authService;
    private final NotificationEventProducer notificationEventProducer;

    public SubscriptionDTO createSubscription(CreateSubscriptionRequest request) {
        Subscription subscription = subscriptionMapper.toEntity(request);

        User user = userRepository.findByUsername(authService.getCurrentUserUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        subscription.setUser(user);
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toDTO(savedSubscription);
    }

    public List<SubscriptionDTO> createListSubscriptions(List<CreateSubscriptionRequest> request) {
        User user = userRepository.findByUsername(authService.getCurrentUserUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Subscription> subscriptions = request.stream()
                .map(subscriptionMapper::toEntity)
                .toList();

        subscriptions.forEach(subscription -> subscription.setUser(user));

        List<Subscription> savedSubscriptions = subscriptionRepository.saveAll(subscriptions);
        return savedSubscriptions.stream()
                .map(subscriptionMapper::toDTO)
                .toList();
    }

    public List<SubscriptionDTO> getListSubscriptions() {
        Integer userId = authService.getCurrentUserId();
        List<Subscription> subscriptions = subscriptionRepository.findAllByUserId(userId);

        return subscriptions.stream()
                .map(subscriptionMapper::toDTO)
                .toList();
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

    @Scheduled(cron = "0 21 0 * * *") // Runs every day at midnight
    public void scheduleUpcomingSubscriptionAlerts(){
        System.out.println("Running scheduled task to send upcoming subscription alerts...");
        LocalDate threeDays = LocalDate.now().plusDays(3);

        subscriptionRepository.findByStatusAndNextPaymentDateBetween(SubscriptionStatus.ACTIVE, LocalDate.now(), threeDays)
                .stream()
                .collect(Collectors.groupingBy(subscription -> subscription.getUser().getEmail()))
                .forEach((email, userSubscriptions) -> {
                    notificationEventProducer.sendPaymentDueAlert(ListSubscriptionPaymentsDueEvent.of(
                            email, userSubscriptions.stream().map(subscriptionMapper::toSubscriptionPaymentDueEvent).toList()));
                });
    }
}
