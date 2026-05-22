package com.ruanpablo2.fleet_quote_service.repositories;

import com.ruanpablo2.fleet_quote_service.entities.Quote;
import com.ruanpablo2.fleet_quote_service.entities.enums.QuoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    Page<Quote> findByBrokerNameOrderByCreatedAtDesc(String brokerName, Pageable pageable);
    long countByBrokerNameAndStatus(String brokerName, QuoteStatus status);
}