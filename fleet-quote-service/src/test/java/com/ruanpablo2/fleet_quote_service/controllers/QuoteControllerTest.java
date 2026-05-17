package com.ruanpablo2.fleet_quote_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.ruanpablo2.fleet_common.dtos.QuoteRequest;
import com.ruanpablo2.fleet_common.dtos.QuoteVehicleRequest;
import com.ruanpablo2.fleet_quote_service.entities.Quote;
import com.ruanpablo2.fleet_quote_service.services.QuoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuoteController.class)
class QuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private QuoteService quoteService;

    @Test
    void createQuote_ShouldReturn200_WhenRequestIsValid() throws Exception {

        String brokerName = "Corretora Top Seguros";

        QuoteVehicleRequest vehicleReq = new QuoteVehicleRequest(
                "ABC-1234", "001004-9", "2020-1", new BigDecimal("50000")
        );
        QuoteRequest request = new QuoteRequest(
                "Viação Estrela", "32.508.514/0001-49", brokerName, List.of(vehicleReq)
        );

        Quote mockQuote = new Quote();
        mockQuote.setId(1L);
        mockQuote.setCustomerName("Viação Estrela");

        when(quoteService.createInitialQuote(any(QuoteRequest.class), eq(brokerName)))
                .thenReturn(mockQuote);

        mockMvc.perform(post("/api/v1/quotes")
                        .header("X-Broker-Name", brokerName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.customerName").value("Viação Estrela"));
    }
}