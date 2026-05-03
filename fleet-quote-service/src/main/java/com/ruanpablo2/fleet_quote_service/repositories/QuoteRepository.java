package com.ruanpablo2.fleet_quote_service.repositories;

import com.ruanpablo2.fleet_quote_service.entities.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
}