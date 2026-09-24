package com.ruanpablo2.fleet_quote_service.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruanpablo2.fleet_quote_service.entities.Quote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BrokerMessageService {

    private final RestClient restClient;
    private final String apiKey;

    public BrokerMessageService(@Value("${GEMINI_API_KEY}") String apiKey) {
        // Fixamos a URL do Google direto na construção do cliente
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
        this.apiKey = apiKey;
    }

    public String generateWhatsAppMessage(Quote quote) {
        String prompt = String.format(
            "Atue como um assistente comercial de seguros. Crie uma mensagem curta, persuasiva e amigável para o corretor enviar via WhatsApp ao cliente final. " +
            "A mensagem deve informar que a cotação da frota foi aprovada. " +
            "Dados: Cliente %s, Valor Total R$ %.2f, Validade da Proposta: 7 dias. " +
            "Não use formatações complexas, use emojis adequados para negócios.",
            quote.getCustomerName(), quote.getTotalPremium()
        );

        String requestBody = """
            {
              "contents": [{
                "parts": [{"text": "%s"}]
              }]
            }
            """.formatted(prompt);

        JsonNode response = restClient.post()
                .uri("/v1beta/models/gemini-3-flash-preview:generateContent?key={key}", apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(JsonNode.class);

        if (response != null && response.has("candidates")) {
            return response.get("candidates").get(0)
                           .get("content").get("parts").get(0)
                           .get("text").asText();
        }

        return "Olá! Sua cotação de frota está pronta. Entre em contato para mais detalhes.";
    }
}
