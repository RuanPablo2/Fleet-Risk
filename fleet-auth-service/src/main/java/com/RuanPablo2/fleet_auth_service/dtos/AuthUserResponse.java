package com.RuanPablo2.fleet_auth_service.dtos;

public record AuthUserResponse(
        Long id,
        String email,
        String brokerName,
        String cnpj
) {}