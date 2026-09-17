package com.anhub.subscriboholic.notification.producer;

import com.anhub.subscriboholic.notification.config.NotificationRabbitConfig;
import com.anhub.subscriboholic.notification.dto.SubscriptionPaymentDueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendPaymentDueAlert(SubscriptionPaymentDueEvent event) {
        log.info("Sending payment due event to queue: eventId={}, subscriptionId={}",
                event.eventId(), event.subscriptionId());

        rabbitTemplate.convertAndSend(
                NotificationRabbitConfig.NOTIFICATION_EXCHANGE,
                NotificationRabbitConfig.NOTIFICATION_ROUTING_KEY,
                event
        );
    }
}
