package com.threadqa.lms.exception;

public class JwtProcessingException extends RuntimeException {
    
    public JwtProcessingException(String message) {
        super(message);
    }
    
    public JwtProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
