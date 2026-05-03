package com.ruanpablo2.fleet_quote_service.services;

import com.ruanpablo2.fleet_common.dtos.QuoteRequest;
import com.ruanpablo2.fleet_quote_service.entities.Quote;
import com.ruanpablo2.fleet_quote_service.entities.enums.QuoteStatus;
import com.ruanpablo2.fleet_quote_service.repositories.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuoteService {

    private final QuoteRepository repository;

    public QuoteService(QuoteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Quote createInitialQuote(QuoteRequest request) {
        Quote quote = new Quote();
        quote.setCustomerName(request.customerName());
        quote.setLicensePlate(request.licensePlate());
        quote.setFipeValue(request.fipeValue());
        quote.setStatus(QuoteStatus.PENDING);

        return repository.save(quote);
    }
}