package com.ruanpablo2.fleet_quote_service.controllers;

import com.ruanpablo2.fleet_common.dtos.QuoteRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    @PostMapping
    public ResponseEntity<String> createQuote(@Valid @RequestBody QuoteRequest request) {
        System.out.println("Receiving a quote for: " + request.customerName());
        System.out.println("Plate: " + request.licensePlate());

        return ResponseEntity.ok("Quote received for the license plate " + request.licensePlate());
    }
}