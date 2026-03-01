package org.example.content_upload_system.exception;

import org.springframework.http.HttpStatus;

public class InvalidInstrctorIDException extends GlobalExceptionHandler{

    public InvalidInstrctorIDException(String message, HttpStatus status) {
        super(message, status);
    }

}
