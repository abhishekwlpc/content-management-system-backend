package org.example.content_upload_system.exception;

import org.springframework.http.HttpStatus;

public class FileSizeLimitExceedException extends GlobalExceptionHandler {

    public FileSizeLimitExceedException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
