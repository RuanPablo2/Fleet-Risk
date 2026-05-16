package com.ruanpablo2.fleet_quote_service.services;

import com.ruanpablo2.fleet_common.dtos.QuoteCreatedEventDTO;
import com.ruanpablo2.fleet_common.dtos.QuoteRequest;
import com.ruanpablo2.fleet_common.dtos.QuoteVehicleRequest;
import com.ruanpablo2.fleet_common.exceptions.BusinessRuleException;
import com.ruanpablo2.fleet_quote_service.clients.VehicleClient;
import com.ruanpablo2.fleet_quote_service.dtos.VehicleFipeResponseDTO;
import com.ruanpablo2.fleet_quote_service.entities.Quote;
import com.ruanpablo2.fleet_quote_service.entities.QuoteVehicle;
import com.ruanpablo2.fleet_quote_service.entities.enums.QuoteStatus;
import com.ruanpablo2.fleet_quote_service.repositories.QuoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private QuoteRepository repository;

    @Mock
    private VehicleClient vehicleClient;

    @InjectMocks
    private QuoteService quoteService;

    @Mock
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Captor
    private ArgumentCaptor<QuoteCreatedEventDTO> eventCaptor;

    @Test
    void createInitialQuote_ShouldCreateDraftAndFreezeFipeValue() {

        String brokerName = "Corretora Top Seguros";

        QuoteVehicleRequest vehicleReq = new QuoteVehicleRequest(
                "ABC-1234", "001004-9", "2020-1", new BigDecimal("50000")
        );

        QuoteRequest request = new QuoteRequest(
                "Viação Estrela", "12.345.678/0001-99", brokerName, List.of(vehicleReq)
        );

        VehicleFipeResponseDTO mockFipeResponse = new VehicleFipeResponseDTO("Fiat Uno", "R$ 35.500,00");
        when(vehicleClient.getVehicleDetails("001004-9", "2020-1")).thenReturn(mockFipeResponse);

        Quote mockSavedQuote = new Quote();
        mockSavedQuote.setId(1L);
        mockSavedQuote.setCustomerName("Viação Estrela");
        mockSavedQuote.setStatus(QuoteStatus.PENDING);

        when(repository.save(any(Quote.class))).thenReturn(mockSavedQuote);

        Quote result = quoteService.createInitialQuote(request, brokerName);

        assertNotNull(result);

        assertEquals(QuoteStatus.PENDING, result.getStatus());

        verify(repository, times(1)).save(any(Quote.class));

        verify(vehicleClient, times(1)).getVehicleDetails("001004-9", "2020-1");
    }

    @Test
    void createInitialQuote_ShouldFallbackToZeroWhenFipeIsDown() {

        String brokerName = "Corretora Top Seguros";
        QuoteVehicleRequest vehicleReq = new QuoteVehicleRequest("XYZ-9876", "004001-2", "2022-1", new BigDecimal("80000"));
        QuoteRequest request = new QuoteRequest("Transportes Rápidos", "98.765.432/0001-11", brokerName, List.of(vehicleReq));

        when(vehicleClient.getVehicleDetails(anyString(), anyString())).thenThrow(new RuntimeException("FIPE API Offline"));

        Quote mockSavedQuote = new Quote();
        mockSavedQuote.setStatus(QuoteStatus.PENDING);
        when(repository.save(any(Quote.class))).thenReturn(mockSavedQuote);

        Quote result = quoteService.createInitialQuote(request, brokerName);

        assertNotNull(result);

        verify(repository, times(1)).save(any(Quote.class));
    }

    @Test
    void calculateQuote_ShouldUpdateDraftAndSendEventToRabbitMQ() {
        Long quoteId = 1L;
        String brokerName = "Corretora Top Seguros";

        QuoteVehicleRequest vehicleReq = new QuoteVehicleRequest(
                "ABC-1234", "001004-9", "2020-1", new BigDecimal("50000")
        );
        QuoteRequest request = new QuoteRequest(
                "Viação Estrela", "12.345.678/0001-99", brokerName, List.of(vehicleReq)
        );

        Quote existingQuote = new Quote();
        existingQuote.setId(quoteId);
        existingQuote.setCustomerName("Viação Estrela");
        existingQuote.setBrokerName(brokerName);
        existingQuote.setStatus(QuoteStatus.PENDING);

        QuoteVehicle existingVehicle = new QuoteVehicle();
        existingVehicle.setId(100L);
        existingVehicle.setFipeCode("001004-9");
        existingVehicle.setYearId("2020-1");
        existingVehicle.setCoverageLimit(new BigDecimal("50000"));
        existingQuote.addVehicle(existingVehicle);

        when(repository.findById(quoteId)).thenReturn(Optional.of(existingQuote));

        when(repository.save(any(Quote.class))).thenReturn(existingQuote);

        VehicleFipeResponseDTO mockFipeResponse = new VehicleFipeResponseDTO("Fiat Uno", "R$ 35.500,00");
        when(vehicleClient.getVehicleDetails("001004-9", "2020-1")).thenReturn(mockFipeResponse);

        quoteService.calculateQuote(quoteId, request, brokerName);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("fleet.quote.events"),
                eq("quote.created.key"),
                eventCaptor.capture()
        );

        QuoteCreatedEventDTO capturedEvent = eventCaptor.getValue();

        assertEquals(quoteId, capturedEvent.quoteId());

        assertFalse(capturedEvent.vehicles().isEmpty());
        assertEquals("001004-9", capturedEvent.vehicles().get(0).fipeCode());
    }

    @Test
    void approveQuote_ShouldThrowException_WhenStatusIsNotCalculated() {

        Long quoteId = 1L;
        String brokerName = "Corretora Top Seguros";

        Quote pendingQuote = new Quote();
        pendingQuote.setId(quoteId);
        pendingQuote.setBrokerName(brokerName);
        pendingQuote.setStatus(QuoteStatus.PENDING);

        when(repository.findById(quoteId)).thenReturn(Optional.of(pendingQuote));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            quoteService.approveQuote(quoteId, brokerName);
        });

        assertEquals("Cannot approve a quote that is not in CALCULATED status.", exception.getMessage());

        verify(repository, never()).save(any(Quote.class));

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }
}