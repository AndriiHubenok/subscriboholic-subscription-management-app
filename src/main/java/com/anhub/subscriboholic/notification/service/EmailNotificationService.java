package com.anhub.subscriboholic.email.service;

import com.anhub.subscriboholic.notification.dto.ListSubscriptionPaymentsDueEvent;
import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.model.message.MailgunMimeMessage;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailNotificationService {
    public void sendUpcomingPaymentEmail(ListSubscriptionPaymentsDueEvent event) {
        Dotenv dotenv = Dotenv.load();

        Message message = Message.builder()
                .from(String.format("Subscriboholic Inc. <%s>", dotenv.get("MAILGUN_DOMAIN")))
                .to(event.userEmail())
                .subject("Upcoming Subscription Payment Reminder")
                .text(String.format("Hello %s,\n\nThis is a friendly reminder that your..."))
                .build();

        MailgunMessagesApi mailgunMessagesApi = MailgunMessagesApi.create(dotenv.get("MAILGUN_API_KEY"));
        mailgunMessagesApi.sendMessage(dotenv.get("MAILGUN_DOMAIN"), message);
    }
}
