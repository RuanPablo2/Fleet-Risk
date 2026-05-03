package com.ruanpablo2.fleet_quote_service.controllers;

import com.ruanpablo2.fleet_common.dtos.QuoteRequest;
import com.ruanpablo2.fleet_quote_service.entities.Quote;
import com.ruanpablo2.fleet_quote_service.services.QuoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteService service;

    public QuoteController(QuoteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Quote> create(@Valid @RequestBody QuoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createInitialQuote(request));
    }
}