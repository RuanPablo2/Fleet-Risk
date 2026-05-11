package com.ruanpablo2.fleet_common.exceptions;

import org.springframework.http.HttpStatus;

public class IntegrationException extends RuntimeException {
    private final String code;

    public IntegrationException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return HttpStatus.BAD_GATEWAY; }
}