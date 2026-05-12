package com.RuanPablo2.fleet_document_service.listeners;

import com.RuanPablo2.fleet_document_service.config.RabbitMQConfig;
import com.RuanPablo2.fleet_document_service.dtos.QuoteApprovedEventDTO;
import com.RuanPablo2.fleet_document_service.services.DocumentService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DocumentListener {

    private final DocumentService documentService;

    public DocumentListener(DocumentService documentService) {
        this.documentService = documentService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DOCUMENT_GENERATE)
    public void processApprovedQuote(QuoteApprovedEventDTO event) {
        System.out.println("📄 [RABBITMQ] Approval received from Quotation ID: " + event.quoteId());

        documentService.generatePdf(event);
    }
}