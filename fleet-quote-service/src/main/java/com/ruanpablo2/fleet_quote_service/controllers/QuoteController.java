package com.ruanpablo2.fleet_quote_service.controllers;

import com.ruanpablo2.fleet_common.dtos.QuoteRequest;
import com.ruanpablo2.fleet_quote_service.dtos.QuoteResponse;
import com.ruanpablo2.fleet_quote_service.entities.Quote;
import com.ruanpablo2.fleet_quote_service.services.QuoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService service) {
        this.quoteService = service;
    }

    @PostMapping
    public ResponseEntity<Quote> create(@Valid @RequestBody QuoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quoteService.createInitialQuote(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quote> getQuote(@PathVariable Long id) {
        Quote quote = quoteService.getQuoteById(id);
        return ResponseEntity.ok(quote);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuoteResponse> updateQuote(
            @PathVariable Long id,
            @Valid @RequestBody QuoteRequest request) {

        QuoteResponse response = quoteService.updateQuote(id, request);
        return ResponseEntity.ok(response);
    }
}