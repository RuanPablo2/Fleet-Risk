package com.ruanpablo2.fleet_quote_service.repositories;

import com.ruanpablo2.fleet_quote_service.entities.Quote;
import com.ruanpablo2.fleet_quote_service.entities.enums.QuoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    Page<Quote> findByBrokerNameOrderByCreatedAtDesc(String brokerName, Pageable pageable);
    long countByBrokerNameAndStatus(String brokerName, QuoteStatus status);

    @Query("SELECT q FROM Quote q WHERE q.brokerName = :brokerName " +
            "AND (:status IS NULL OR q.status = :status) " +
            "AND (:term IS NULL OR LOWER(q.customerName) LIKE :term " +
            "OR q.customerCnpj LIKE :term) " +
            "ORDER BY q.createdAt DESC")
    Page<Quote> findQuotesWithFilters(
            @Param("brokerName") String brokerName,
            @Param("term") String term,
            @Param("status") QuoteStatus status,
            Pageable pageable);

    List<Quote> findByBrokerEmail(String brokerEmail);
}