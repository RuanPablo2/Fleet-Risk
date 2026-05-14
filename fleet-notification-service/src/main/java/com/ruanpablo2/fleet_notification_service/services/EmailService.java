package com.ruanpablo2.fleet_notification_service.services;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
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

            FileSystemResource file = new FileSystemResource(new File(filePath));
            helper.addAttachment("Proposta_FleetRisk_" + quoteId + ".pdf", file);

            mailSender.send(message);
            System.out.println("📧 Commercial Proposal email sent successfully via Thymeleaf to: " + to);

        } catch (MessagingException e) {
            System.err.println("🚨 Error sending email: " + e.getMessage());
        }
    }
}