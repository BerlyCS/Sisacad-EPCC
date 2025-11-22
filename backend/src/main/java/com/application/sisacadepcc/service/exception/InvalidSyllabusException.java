package com.application.sisacadepcc.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidSyllabusException extends RuntimeException {
    public InvalidSyllabusException(String message) {
        super(message);
    }
}
