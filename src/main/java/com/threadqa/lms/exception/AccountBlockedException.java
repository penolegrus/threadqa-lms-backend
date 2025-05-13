package com.threadqa.lms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при попытке доступа к заблокированному аккаунту
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccountBlockedException extends RuntimeException {
    
    public AccountBlockedException(String message) {
        super(message);
    }
    
    public AccountBlockedException(String message, Throwable cause) {
        super(message, cause);
    }
}
