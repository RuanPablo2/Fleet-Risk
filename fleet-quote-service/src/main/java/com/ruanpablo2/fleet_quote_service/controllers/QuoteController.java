package com.ruanpablo2.fleet_quote_service.controllers;

import com.ruanpablo2.fleet_common.dtos.QuoteRequest;
import com.ruanpablo2.fleet_quote_service.dtos.QuoteResponse;
import com.ruanpablo2.fleet_quote_service.entities.Quote;
import com.ruanpablo2.fleet_quote_service.services.QuoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService service) {
        this.quoteService = service;
    }

    @PostMapping
    public ResponseEntity<Quote> create(
            @Valid @RequestBody QuoteRequest request,
            @RequestHeader("X-Broker-Name") String encodedBrokerName) {

        String brokerName = decodeHeader(encodedBrokerName);
        return ResponseEntity.status(HttpStatus.CREATED).body(quoteService.createInitialQuote(request, brokerName));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quote> getQuote(
            @PathVariable Long id,
            @RequestHeader("X-Broker-Name") String encodedBrokerName) {

        String brokerName = decodeHeader(encodedBrokerName);
        Quote quote = quoteService.getQuoteById(id, brokerName);
        return ResponseEntity.ok(quote);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuoteResponse> saveDraft(
            @PathVariable Long id,
            @Valid @RequestBody QuoteRequest request,
            @RequestHeader("X-Broker-Name") String encodedBrokerName) {

        String brokerName = decodeHeader(encodedBrokerName);
        QuoteResponse response = quoteService.updateQuote(id, request, brokerName);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/calculate")
    public ResponseEntity<Void> calculate(
            @PathVariable Long id,
            @Valid @RequestBody QuoteRequest request,
            @RequestHeader("X-Broker-Name") String encodedBrokerName) {

        String brokerName = decodeHeader(encodedBrokerName);
        quoteService.calculateQuote(id, request, brokerName);
        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approveQuote(
            @PathVariable Long id,
            @RequestHeader("X-Broker-Name") String encodedBrokerName) {

        String brokerName = decodeHeader(encodedBrokerName);
        quoteService.approveQuote(id, brokerName);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/resend-document")
    public ResponseEntity<Void> resendDocument(
            @PathVariable Long id,
            @RequestHeader("X-Broker-Name") String encodedBrokerName) {

        String brokerName = decodeHeader(encodedBrokerName);
        quoteService.resendDocument(id, brokerName);

        return ResponseEntity.accepted().build();
    }

    private String decodeHeader(String encodedValue) {
        if (encodedValue == null) return null;
        return URLDecoder.decode(encodedValue, StandardCharsets.UTF_8);
    }
}