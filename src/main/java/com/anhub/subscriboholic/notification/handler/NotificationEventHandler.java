package com.anhub.subscriboholic.notification.handler;

import com.anhub.subscriboholic.notification.config.NotificationRabbitConfig;
import com.anhub.subscriboholic.notification.dto.SubscriptionPaymentDueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

//    private final EmailNotificationService emailNotificationService;

    @RabbitListener(queues = NotificationRabbitConfig.NOTIFICATION_QUEUE)
    public void handlePaymentDueEvent(SubscriptionPaymentDueEvent event) {
        log.info("Received notification event: eventId={}, subName={}",
                event.eventId(), event.subscriptionName());

        try {
            System.out.println(event.subscriptionName());
            System.out.println(event.subscriptionId());
            System.out.println(event.eventId());
            System.out.println(event.paymentDueDate());
            System.out.println(event.amount());
//            emailNotificationService.sendUpcomingPaymentEmail(event);
        } catch (Exception e) {

            log.error("Failed to process notification for subId={}, eventId={}. Error: {}",
                    event.subscriptionId(), event.eventId(), e.getMessage());
            throw e;
        }
    }
}
