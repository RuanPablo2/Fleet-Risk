package com.ruanpablo2.fleet_quote_service.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruanpablo2.fleet_quote_service.entities.Quote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BrokerMessageService {

    private final RestClient restClient;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public BrokerMessageService(@Value("${GEMINI_API_KEY}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
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

        try {
            String jsonString = restClient.post()
                    .uri("/v1beta/models/gemini-3-flash-preview:generateContent?key={key}", apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode response = objectMapper.readTree(jsonString);

            if (response != null && response.has("candidates")) {
                return response.get("candidates").get(0)
                        .get("content").get("parts").get(0)
                        .get("text").asText();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao chamar o Gemini: " + e.getMessage());
        }

        return "Olá! Sua cotação de frota está pronta. Entre em contato para mais detalhes.";
    }
}