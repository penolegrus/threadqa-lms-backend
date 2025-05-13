package com.threadqa.lms.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.Map;

@Getter
@Setter
public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> errors;

    public ValidationErrorResponse(int status, String message, ZonedDateTime timestamp, Map<String, String> errors) {
        super(status, message, timestamp);
        this.errors = errors;
    }
}