package com.threadqa.lms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при превышении максимального количества активных сессий
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class TooManySessionsException extends RuntimeException {
    
    public TooManySessionsException(String message) {
        super(message);
    }
    
    public TooManySessionsException(String message, Throwable cause) {
        super(message, cause);
    }
}
