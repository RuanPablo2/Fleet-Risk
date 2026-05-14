package com.ruanpablo2.fleet_common.dtos;

public record DocumentGeneratedEventDTO(
        Long quoteId,
        String brokerEmail,
        String customerName,
        String pdfFilePath
) {}