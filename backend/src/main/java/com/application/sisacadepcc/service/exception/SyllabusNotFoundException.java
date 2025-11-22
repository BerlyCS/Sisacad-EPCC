package com.application.sisacadepcc.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SyllabusNotFoundException extends RuntimeException {
    public SyllabusNotFoundException(String message) {
        super(message);
    }
}
