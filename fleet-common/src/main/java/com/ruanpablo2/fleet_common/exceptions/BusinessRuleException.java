package com.ruanpablo2.fleet_common.exceptions;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends RuntimeException {
    private final String code;

    public BusinessRuleException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return HttpStatus.UNPROCESSABLE_ENTITY; }
}