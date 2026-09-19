package com.anhub.subscriboholic.notification.handler;

import com.anhub.subscriboholic.notification.service.EmailNotificationService;
import com.anhub.subscriboholic.notification.config.NotificationConfig;
import com.anhub.subscriboholic.notification.dto.ListSubscriptionPaymentsDueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final EmailNotificationService emailNotificationService;

    @RabbitListener(queues = NotificationConfig.NOTIFICATION_QUEUE)
    public void handlePaymentDueEvent(ListSubscriptionPaymentsDueEvent event) {
        log.info("Received notification event: eventId={}, userEmail={}",
                event.eventId(), event.userEmail());

        try {
            emailNotificationService.sendUpcomingPaymentEmail(event);

        } catch (Exception e) {

            log.error("Failed to process notification for userEmail={}, eventId={}. Error: {}",
                    event.userEmail(), event.eventId(), e.getMessage());
            throw e;
        }
    }
}
