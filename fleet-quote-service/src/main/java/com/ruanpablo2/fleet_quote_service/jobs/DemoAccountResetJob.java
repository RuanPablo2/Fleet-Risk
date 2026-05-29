package com.ruanpablo2.fleet_quote_service.jobs;

import com.ruanpablo2.fleet_quote_service.dtos.QuoteApprovedEventDTO;
import com.ruanpablo2.fleet_quote_service.dtos.QuoteVehicleApprovedDTO;
import com.ruanpablo2.fleet_quote_service.entities.Quote;
import com.ruanpablo2.fleet_quote_service.entities.QuoteVehicle;
import com.ruanpablo2.fleet_quote_service.entities.enums.QuoteStatus;
import com.ruanpablo2.fleet_quote_service.repositories.QuoteRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class DemoAccountResetJob {

    private final QuoteRepository quoteRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final String DEMO_EMAIL = "demo@fleetrisk.com";
    private static final String DEMO_BROKER_NAME = "Fleet Risk Demonstração";

    public DemoAccountResetJob(QuoteRepository quoteRepository, RabbitTemplate rabbitTemplate) {
        this.quoteRepository = quoteRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(cron = "0 0 0,12 * * *", zone = "America/Sao_Paulo")
    @Transactional
    public void resetDemoAccount() {
        System.out.println("🔄 [CRON JOB] Iniciando restauração do cenário da conta Demo...");

        List<Quote> existingQuotes = quoteRepository.findByBrokerEmail(DEMO_EMAIL);
        if (!existingQuotes.isEmpty()) {
            quoteRepository.deleteAll(existingQuotes);
        }

        Quote pendingQuote = createPendingQuote();
        Quote calculatedQuote = createCalculatedQuote();
        Quote approvedQuote = createApprovedQuote();

        quoteRepository.saveAll(List.of(pendingQuote, calculatedQuote, approvedQuote));

        publishDocumentEvent(approvedQuote);

        System.out.println("✅ [CRON JOB] Vitrine restaurada com sucesso! Cotações originais do banco reinseridas e PDF engatilhado.");
    }

    private void publishDocumentEvent(Quote quote) {
        BigDecimal totalFipeCalculated = BigDecimal.ZERO;
        List<QuoteVehicleApprovedDTO> vehicleDTOs = new ArrayList<>();

        for (QuoteVehicle v : quote.getVehicles()) {
            BigDecimal fipeValue = v.getFipeValue() != null ? v.getFipeValue() : BigDecimal.ZERO;
            totalFipeCalculated = totalFipeCalculated.add(fipeValue);

            String displayName = v.getModelName() != null ? v.getModelName() : "Vehicle (" + v.getFipeCode() + ")";

            vehicleDTOs.add(new QuoteVehicleApprovedDTO(
                    displayName,
                    v.getYearId(),
                    v.getLicensePlate(),
                    fipeValue,
                    v.getCalculatedPremium()
            ));
        }

        QuoteApprovedEventDTO event = new QuoteApprovedEventDTO(
                quote.getId(),
                quote.getCustomerName(),
                quote.getCustomerCnpj(),
                quote.getBrokerName(),
                quote.getBrokerEmail(),
                quote.getTotalPremium(),
                totalFipeCalculated,
                vehicleDTOs
        );

        try {
            rabbitTemplate.convertAndSend("fleet.quote.events", "quote.approved.key", event);
            System.out.println("📬 [CRON JOB] Evento enviado pro RabbitMQ para gerar PDF da cotação: " + quote.getId());
        } catch (Exception e) {
            System.err.println("⚠️ [CRON JOB] Erro ao avisar o RabbitMQ: " + e.getMessage());
        }
    }

    private Quote createPendingQuote() {
        Quote quote = new Quote();
        quote.setCustomerName("Global transportadora LTDA");
        quote.setCustomerCnpj("08.787.717/0001-37");
        quote.setBrokerName(DEMO_BROKER_NAME);
        quote.setBrokerEmail(DEMO_EMAIL);
        quote.setStatus(QuoteStatus.PENDING);

        QuoteVehicle v1 = new QuoteVehicle();
        v1.setLicensePlate("ABC-1234");
        v1.setFipeCode("005040-7");
        v1.setYearId("1997-1");
        v1.setModelName("Gol GTi 2.0 (005040-7)");
        v1.setFipeValue(new BigDecimal("32201.00"));
        v1.setCoverageLimit(new BigDecimal("1000000.00"));

        QuoteVehicle v2 = new QuoteVehicle();
        v2.setLicensePlate("AAA-1234");
        v2.setFipeCode("001300-5");
        v2.setYearId("2011-5");
        v2.setModelName("Doblo  1.4 mpi Fire Flex  8V 4p (001300-5)");
        v2.setFipeValue(new BigDecimal("39914.00"));
        v2.setCoverageLimit(new BigDecimal("50000.00"));

        QuoteVehicle v3 = new QuoteVehicle();
        v3.setLicensePlate("BBB-1234");
        v3.setFipeCode("005259-0");
        v3.setYearId("2014-5");
        v3.setModelName("Golf Sportline 1.6 Mi Total Flex 8V 4p (005259-0)");
        v3.setFipeValue(new BigDecimal("65843.00"));
        v3.setCoverageLimit(new BigDecimal("50000.00"));

        QuoteVehicle v4 = new QuoteVehicle();
        v4.setLicensePlate("CCC-1234");
        v4.setFipeCode("005136-5");
        v4.setYearId("2006-1");
        v4.setModelName("Kombi Lotação 1.6 MPi (005136-5)");
        v4.setFipeValue(new BigDecimal("28323.00"));
        v4.setCoverageLimit(new BigDecimal("1000000.00"));

        quote.addVehicle(v1);
        quote.addVehicle(v2);
        quote.addVehicle(v3);
        quote.addVehicle(v4);

        return quote;
    }

    private Quote createCalculatedQuote() {
        Quote quote = new Quote();
        quote.setCustomerName("Global transportadora LTDA");
        quote.setCustomerCnpj("08.787.717/0001-37");
        quote.setBrokerName(DEMO_BROKER_NAME);
        quote.setBrokerEmail(DEMO_EMAIL);
        quote.setStatus(QuoteStatus.CALCULATED);
        quote.setTotalPremium(new BigDecimal("17732.42"));

        QuoteVehicle v1 = new QuoteVehicle();
        v1.setLicensePlate("FFF-1234");
        v1.setFipeCode("064004-2");
        v1.setYearId("2012-1");
        v1.setModelName("Cargo CD 1.0 8V 53cv (Pick-Up) (064004-2)");
        v1.setFipeValue(new BigDecimal("23152.00"));
        v1.setCoverageLimit(new BigDecimal("1000000.00"));
        v1.setCalculatedPremium(new BigDecimal("5963.04"));

        QuoteVehicle v2 = new QuoteVehicle();
        v2.setLicensePlate("EEE-1234");
        v2.setFipeCode("001029-4");
        v2.setYearId("1997-1");
        v2.setModelName("Fiorino Pick-Up 1.5 i.e./1.3/1.5/HD (001029-4)");
        v2.setFipeValue(new BigDecimal("12503.00"));
        v2.setCoverageLimit(new BigDecimal("25000.00"));
        v2.setCalculatedPremium(new BigDecimal("875.06"));

        QuoteVehicle v3 = new QuoteVehicle();
        v3.setLicensePlate("DDD-1234");
        v3.setFipeCode("002107-5");
        v3.setYearId("2021-1");
        v3.setModelName("Hilux SW4 SR 4x2 2.7/ 2.7 Flex 16V Aut. (002107-5)");
        v3.setFipeValue(new BigDecimal("211279.00"));
        v3.setCoverageLimit(new BigDecimal("1000000.00"));
        v3.setCalculatedPremium(new BigDecimal("9225.58"));

        QuoteVehicle v4 = new QuoteVehicle();
        v4.setLicensePlate("TTT-1234");
        v4.setFipeCode("001235-1");
        v4.setYearId("2021-1");
        v4.setModelName("Doblo Cargo 1.8 mpi Fire Flex 8V/16V 4p (001235-1)");
        v4.setFipeValue(new BigDecimal("70937.00"));
        v4.setCoverageLimit(new BigDecimal("50000.00"));
        v4.setCalculatedPremium(new BigDecimal("1668.74"));

        quote.addVehicle(v1);
        quote.addVehicle(v2);
        quote.addVehicle(v3);
        quote.addVehicle(v4);

        return quote;
    }

    private Quote createApprovedQuote() {
        Quote quote = new Quote();
        quote.setCustomerName("Transportadora dois irmãos");
        quote.setCustomerCnpj("53.238.419/0001-42");
        quote.setBrokerName(DEMO_BROKER_NAME);
        quote.setBrokerEmail(DEMO_EMAIL);
        quote.setStatus(QuoteStatus.APPROVED);
        quote.setTotalPremium(new BigDecimal("14480.42"));

        QuoteVehicle v1 = new QuoteVehicle();
        v1.setLicensePlate("VVV-1234");
        v1.setFipeCode("015156-4");
        v1.setYearId("2019-5");
        v1.setModelName("HB20 Unique 1.0 Flex 12V Mec. (015156-4)");
        v1.setFipeValue(new BigDecimal("53406.00"));
        v1.setCoverageLimit(new BigDecimal("1000000.00"));
        v1.setCalculatedPremium(new BigDecimal("6568.12"));

        QuoteVehicle v2 = new QuoteVehicle();
        v2.setLicensePlate("KKK-4444");
        v2.setFipeCode("025267-0");
        v2.setYearId("2026-5");
        v2.setModelName("KWID Intense 1.0 Flex 12V 5p Mec. (025267-0)");
        v2.setFipeValue(new BigDecimal("66428.00"));
        v2.setCoverageLimit(new BigDecimal("500000.00"));
        v2.setCalculatedPremium(new BigDecimal("3828.56"));

        QuoteVehicle v3 = new QuoteVehicle();
        v3.setLicensePlate("ZZZ-4321");
        v3.setFipeCode("001342-0");
        v3.setYearId("2017-5");
        v3.setModelName("Punto ESSENCE Dualogic 1.6 Flex 16V 5p (001342-0)");
        v3.setFipeValue(new BigDecimal("54187.00"));
        v3.setCoverageLimit(new BigDecimal("500000.00"));
        v3.setCalculatedPremium(new BigDecimal("4083.74"));

        quote.addVehicle(v1);
        quote.addVehicle(v2);
        quote.addVehicle(v3);

        return quote;
    }
}