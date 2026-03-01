package org.example.content_upload_system.exception;

import org.springframework.http.HttpStatus;

public class CannotExtractInstructorIDFromToken extends GlobalExceptionHandler{

    public CannotExtractInstructorIDFromToken(String message, HttpStatus status) {
        super(message, status);
    }
}
