package com.ruanpablo2.fleet_common.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedAccessException extends RuntimeException {
    private final String code;

    public UnauthorizedAccessException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return HttpStatus.FORBIDDEN; }
}