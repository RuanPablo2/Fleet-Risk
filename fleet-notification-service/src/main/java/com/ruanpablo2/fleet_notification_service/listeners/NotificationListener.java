package com.ruanpablo2.fleet_notification_service.listeners;

import com.ruanpablo2.fleet_common.dtos.DocumentGeneratedEventDTO;
import com.ruanpablo2.fleet_notification_service.services.EmailService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private final EmailService emailService;

    public NotificationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "fleet.notification.email.queue", durable = "true"),
            exchange = @Exchange(value = "fleet.document.events", type = "direct"),
            key = "document.generated.key"
    ))
    public void processDocumentGenerated(DocumentGeneratedEventDTO event) {
        System.out.println("📥 [NOTIFICATION] Event received! Preparing email for Quote: " + event.quoteId());

        emailService.sendProposalEmail(
                event.brokerEmail(),
                event.customerName(),
                event.quoteId(),
                event.pdfFilePath()
        );
    }
}