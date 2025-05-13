package com.threadqa.lms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при обнаружении подозрительной активности
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class SuspiciousActivityException extends RuntimeException {
    
    public SuspiciousActivityException(String message) {
        super(message);
    }
    
    public SuspiciousActivityException(String message, Throwable cause) {
        super(message, cause);
    }
}
