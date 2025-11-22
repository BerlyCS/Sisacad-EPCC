package com.application.sisacadepcc.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SyllabusStorageException extends RuntimeException {
    public SyllabusStorageException(String message) {
        super(message);
    }

    public SyllabusStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
