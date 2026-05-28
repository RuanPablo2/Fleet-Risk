package com.ruanpablo2.fleet_notification_service.services;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final RestTemplate restTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.restTemplate = new RestTemplate();
    }

    public void sendProposalEmail(String to, String customerName, Long quoteId, String filePath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context thymeleafContext = new Context();
            thymeleafContext.setVariable("customerName", customerName);
            thymeleafContext.setVariable("quoteId", quoteId);

            String htmlBody = templateEngine.process("email-proposal", thymeleafContext);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("📄 Proposta Comercial Disponível - FleetRisk Seguros");
            helper.setText(htmlBody, true);

            String documentServiceUrl = "http://fleet-document-service:8084/api/v1/documents/quotes/" + quoteId + "/download";

            byte[] pdfBytes = restTemplate.getForObject(documentServiceUrl, byte[].class);

            if (pdfBytes != null) {
                ByteArrayResource pdfAttachment = new ByteArrayResource(pdfBytes);
                helper.addAttachment("Proposta_FleetRisk_" + quoteId + ".pdf", pdfAttachment);
            } else {
                System.err.println("🚨 Warning: Could not fetch PDF bytes from Document Service.");
            }

            mailSender.send(message);
            System.out.println("📧 Commercial Proposal email sent successfully via Thymeleaf to: " + to);

        } catch (Exception e) {
            System.err.println("🚨 Error sending email: " + e.getMessage());
        }
    }
}