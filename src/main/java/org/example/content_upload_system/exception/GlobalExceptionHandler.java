package org.example.content_upload_system.exception;

import org.springframework.http.HttpStatus;

public class GlobalExceptionHandler extends RuntimeException {

    public GlobalExceptionHandler(String message, HttpStatus status) {
        super(message);
    }
}
