package com.anhub.subscriboholic.notification.service;


import com.anhub.subscriboholic.notification.dto.ListSubscriptionPaymentsDueEvent;
import com.anhub.subscriboholic.notification.dto.SubscriptionPaymentDueEvent;
import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final MailgunMessagesApi mailgunMessagesApi;

    private final Dotenv dotenv;

    @Value("${mailgun.from-name:Subscriboholic}")
    private String fromName;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public void sendUpcomingPaymentEmail(ListSubscriptionPaymentsDueEvent event) {
        if (event.subscriptions() == null || event.subscriptions().isEmpty()) {
            log.info("No subscriptions to notify for user: {}", event.userEmail());
            return;
        }

        log.info("Building upcoming payments email for {} (eventId: {})", event.userEmail(), event.eventId());

        BigDecimal totalAmount = event.subscriptions().stream()
                .map(SubscriptionPaymentDueEvent::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String currency = event.subscriptions().get(0).currency();

        String htmlBody = buildHtmlBody(event.subscriptions(), totalAmount, currency);
        String textBody = buildTextFallback(event.subscriptions(), totalAmount, currency);

        Message message = Message.builder()
                .from(String.format("%s <%s>", fromName, String.format("no-reply@%s", dotenv.get("MAILGUN_DOMAIN"))))
                .to(event.userEmail())
                .subject("💳 Reminder: Upcoming Subscription Charges")
                .text(textBody)
                .html(htmlBody)
                .build();

        try {
            MessageResponse response = mailgunMessagesApi.sendMessage(dotenv.get("MAILGUN_DOMAIN"), message);
            log.info("Successfully sent payment email to {}. Mailgun id: {}",
                    event.userEmail(), response.getId());
        } catch (Exception e) {
            log.error("Failed to send email to {}. Error: {}", event.userEmail(), e.getMessage(), e);
            throw e;
        }
    }

    private String buildHtmlBody(List<SubscriptionPaymentDueEvent> subscriptions, BigDecimal total, String currency) {
        StringBuilder rowsBuilder = new StringBuilder();

        for (SubscriptionPaymentDueEvent sub : subscriptions) {
            rowsBuilder.append(String.format("""
                <tr style="border-bottom: 1px solid #f1f5f9;">
                  <td style="padding: 12px 16px; font-size: 14px; font-weight: 500; color: #1e293b;">%s</td>
                  <td align="center" style="padding: 12px 16px; font-size: 13px; color: #64748b;">%s</td>
                  <td align="right" style="padding: 12px 16px; font-size: 14px; font-weight: 600; color: #0f172a;">%.2f %s</td>
                </tr>
            """,
                    sub.subscriptionName(),
                    sub.paymentDueDate().format(DATE_FORMATTER),
                    sub.price(),
                    sub.currency()));
        }

        String template = """
        <!DOCTYPE html>
        <html>
        <body style="margin: 0; padding: 0; background-color: #f4f5f7; font-family: sans-serif; color: #1e293b;">
          <table width="100%%" border="0" cellspacing="0" cellpadding="0" style="padding: 30px 10px;">
            <tr>
              <td align="center">
                <table width="100%%" style="max-width: 580px; background: #ffffff; border-radius: 10px; border: 1px solid #e2e8f0; overflow: hidden;">
                  <tr>
                    <td style="padding: 24px 30px; background: #0f172a; color: #ffffff;">
                      <h2 style="margin: 0; font-size: 20px;">💳 Subscriboholic</h2>
                      <p style="margin: 6px 0 0; font-size: 13px; color: #94a3b8;">Upcoming charges reminder</p>
                    </td>
                  </tr>
                  <tr>
                    <td style="padding: 24px 30px 10px;">
                      <p style="margin: 0; font-size: 14px; line-height: 20px;">
                        The following subscriptions are scheduled for payment within the next 3 days:
                      </p>
                    </td>
                  </tr>
                  <tr>
                    <td style="padding: 10px 30px;">
                      <table width="100%%" style="border-collapse: collapse; border: 1px solid #e2e8f0; border-radius: 6px;">
                        <thead>
                          <tr style="background: #f8fafc; font-size: 12px; color: #64748b; text-align: left;">
                            <th style="padding: 10px 14px;">Service</th>
                            <th style="padding: 10px 14px; text-align: center;">Due Date</th>
                            <th style="padding: 10px 14px; text-align: right;">Amount</th>
                          </tr>
                        </thead>
                        <tbody>
                          %s
                        </tbody>
                        <tfoot>
                          <tr style="background: #f8fafc; font-weight: bold; font-size: 14px;">
                            <td colspan="2" style="padding: 12px 14px; border-top: 1px solid #e2e8f0;">Total Due</td>
                            <td align="right" style="padding: 12px 14px; border-top: 1px solid #e2e8f0;">%.2f %s</td>
                          </tr>
                        </tfoot>
                      </table>
                    </td>
                  </tr>
                  <tr>
                    <td style="padding: 20px 30px 30px; text-align: center;">
                      <p style="margin: 0; font-size: 12px; color: #94a3b8;">
                        Keep track and manage your finances anytime in Subscriboholic.
                      </p>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </body>
        </html>
        """;

        return String.format(template, rowsBuilder.toString(), total, currency);
    }

    private String buildTextFallback(List<SubscriptionPaymentDueEvent> subscriptions, BigDecimal total, String currency) {
        StringBuilder sb = new StringBuilder("Upcoming Subscription Payments Reminder:\n\n");
        for (SubscriptionPaymentDueEvent sub : subscriptions) {
            sb.append(String.format("- %s: %.2f %s (Due: %s)\n",
                    sub.subscriptionName(),
                    sub.price(),
                    sub.currency(),
                    sub.paymentDueDate().format(DATE_FORMATTER)));
        }
        sb.append(String.format("\nTotal expected: %.2f %s\n\nManage your subscriptions in Subscriboholic.", total, currency));
        return sb.toString();
    }
}
