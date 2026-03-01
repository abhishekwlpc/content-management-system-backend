package org.example.content_upload_system.exception;

import org.springframework.http.HttpStatus;

public class GlobalExceptionHandler extends RuntimeException {

    private HttpStatus status;

    public GlobalExceptionHandler(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}