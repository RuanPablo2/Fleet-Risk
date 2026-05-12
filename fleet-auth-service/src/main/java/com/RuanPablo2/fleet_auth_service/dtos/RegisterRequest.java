package com.RuanPablo2.fleet_auth_service.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "E-mail is required")
        @Email(message = "Invalid e-mail format")
        String email,

        @NotBlank(message = "Password is required")
        String password,

        @NotBlank(message = "Broker name is required")
        String brokerName,

        @NotBlank(message = "CNPJ is required")
        String cnpj
) {}