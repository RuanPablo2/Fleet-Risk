package com.ruanpablo2.fleet_quote_service.controllers;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    @GetMapping("/test")
    public String test() {
        return "Quote Service is live at door 8081!";
    }
}