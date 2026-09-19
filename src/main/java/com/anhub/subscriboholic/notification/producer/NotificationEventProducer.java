package com.anhub.subscriboholic.notification.producer;

import com.anhub.subscriboholic.notification.config.NotificationConfig;
import com.anhub.subscriboholic.notification.dto.ListSubscriptionPaymentsDueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendPaymentDueAlert(ListSubscriptionPaymentsDueEvent event) {
        log.info("Sending payment due event to queue: eventId={}, userEmail={}",
                event.eventId(), event.userEmail());

        rabbitTemplate.convertAndSend(
                NotificationConfig.NOTIFICATION_EXCHANGE,
                NotificationConfig.NOTIFICATION_ROUTING_KEY,
                event
        );
    }
}
