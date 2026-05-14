package com.RuanPablo2.fleet_document_service.services;


import com.RuanPablo2.fleet_document_service.dtos.QuoteApprovedEventDTO;
import com.lowagie.text.pdf.BaseFont;
import com.ruanpablo2.fleet_common.dtos.DocumentGeneratedEventDTO;
import com.ruanpablo2.fleet_common.exceptions.ResourceNotFoundException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class DocumentService {

    private final TemplateEngine templateEngine;
    private final RabbitTemplate rabbitTemplate;

    public DocumentService(TemplateEngine templateEngine, RabbitTemplate rabbitTemplate) {
        this.templateEngine = templateEngine;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void generatePdf(QuoteApprovedEventDTO event) {
        System.out.println("⚙️ [DOCUMENT] Starting PDF generation for quotation: " + event.quoteId());

        try {
            NumberFormat currencyFormatter = NumberFormat.getInstance(new Locale("pt", "BR"));
            currencyFormatter.setMinimumFractionDigits(2);
            currencyFormatter.setMaximumFractionDigits(2);

            Context context = new Context();
            context.setVariable("quoteId", String.format("2026-%05d", event.quoteId()));
            context.setVariable("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")));
            context.setVariable("customerName", event.customerName());
            context.setVariable("customerCnpj", event.customerCnpj());
            context.setVariable("brokerName", event.brokerName());

            context.setVariable("totalPremium", currencyFormatter.format(event.totalPremium()));
            context.setVariable("totalFipe", currencyFormatter.format(event.totalFipe()));

            context.setVariable("vehicles", event.vehicles().stream().map(v -> new java.util.HashMap<String, String>() {{
                put("modelName", v.modelName());
                put("year", v.year());
                put("licensePlate", v.licensePlate());
                put("fipeValue", currencyFormatter.format(v.fipeValue()));
                put("calculatedPremium", currencyFormatter.format(v.calculatedPremium()));
            }}).toList());

            String htmlContent = templateEngine.process("proposta", context);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();

            String fontPath = new ClassPathResource("fonts/arial.ttf").getURL().toString();
            renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);

            String fileName = "Proposta" + event.quoteId() + ".pdf";
            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                fos.write(outputStream.toByteArray());
            }

            System.out.println("✅ [DOCUMENT] PDF successfully generated and saved locally: " + fileName);

            String absolutePath = new File(fileName).getAbsolutePath();

            DocumentGeneratedEventDTO notificationEvent = new DocumentGeneratedEventDTO(
                    event.quoteId(),
                    "ruanpablo2.dev@gmail.com",
                    event.customerName(),
                    absolutePath
            );

            rabbitTemplate.convertAndSend("fleet.document.events", "document.generated.key", notificationEvent);
            System.out.println("📤 [DOCUMENT] Event sent to Notification Service.");

        } catch (Exception e) {
            System.err.println("❌ [DOCUMENT] Error generating PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Resource getProposalResource(Long quoteId) {
        String fileName = "Proposta" + quoteId + ".pdf";
        File pdfFile = new File(fileName);

        if (!pdfFile.exists()) {
            System.err.println("🚨 [DOCUMENT] Download failed: File not found for Quote ID " + quoteId);
            throw new ResourceNotFoundException("Proposal file not found for Quote ID: " + quoteId, "DOC_404");
        }

        return new FileSystemResource(pdfFile);
    }
}