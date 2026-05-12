package com.RuanPablo2.fleet_document_service.services;


import com.RuanPablo2.fleet_document_service.dtos.QuoteApprovedEventDTO;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class DocumentService {

    private final TemplateEngine templateEngine;

    public DocumentService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void generatePdf(QuoteApprovedEventDTO event) {
        System.out.println("⚙️ [DOCUMENT] Iniciando geração de PDF para Cotação: " + event.quoteId());

        try {
            Context context = new Context();
            context.setVariable("quoteId", String.format("2026-%05d", event.quoteId()));
            context.setVariable("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")));
            context.setVariable("customerName", event.customerName());
            context.setVariable("customerCnpj", event.customerCnpj());
            context.setVariable("brokerName", event.brokerName());

            context.setVariable("totalPremium", event.totalPremium());
            context.setVariable("totalFipe", event.totalFipe());
            context.setVariable("vehicles", event.vehicles());

            String htmlContent = templateEngine.process("proposta", context);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();

            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);

            String fileName = "Cotacao" + event.quoteId() + ".pdf";
            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                fos.write(outputStream.toByteArray());
            }

            System.out.println("✅ [DOCUMENT] PDF successfully generated and saved locally: " + fileName);

        } catch (Exception e) {
            System.err.println("❌ [DOCUMENT] Error generating PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}