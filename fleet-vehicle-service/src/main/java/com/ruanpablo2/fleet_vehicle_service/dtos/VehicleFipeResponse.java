package com.ruanpablo2.fleet_vehicle_service.dtos;

public record VehicleFipeResponse(
        String valor,
        String marca,
        String modelo,
        Integer anoModelo,
        String combustivel,
        String codigoFipe,
        String mesReferencia,
        Integer tipoVeiculo,
        String siglaCombustivel
) {}