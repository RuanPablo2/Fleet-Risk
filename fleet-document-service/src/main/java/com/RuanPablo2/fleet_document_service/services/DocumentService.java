package com.RuanPablo2.fleet_document_service.services;


import com.RuanPablo2.fleet_document_service.dtos.QuoteApprovedEventDTO;
import com.lowagie.text.pdf.BaseFont;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class DocumentService {

    private final TemplateEngine templateEngine;

    public DocumentService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
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