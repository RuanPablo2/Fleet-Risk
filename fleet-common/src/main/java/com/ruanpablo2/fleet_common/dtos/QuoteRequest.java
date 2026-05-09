package com.ruanpablo2.fleet_common.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.br.CNPJ;

import java.util.List;

public record QuoteRequest(
        @NotBlank(message = "Customer name is required")
        String customerName,

        @NotBlank(message = "CNPJ is required")
        @CNPJ(message = "CNPJ inválido")
        String customerCnpj,

        @NotBlank(message = "The broker's name is required.")
        String brokerName,

        @NotEmpty(message = "A fleet must have at least one vehicle")
        @Valid
        List<QuoteVehicleRequest> vehicles
) {}