package com.RuanPablo2.fleet_auth_service.dtos;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "E-mail is required")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {}