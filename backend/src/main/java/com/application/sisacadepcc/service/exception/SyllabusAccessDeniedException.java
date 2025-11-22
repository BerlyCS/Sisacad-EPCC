package com.application.sisacadepcc.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class SyllabusAccessDeniedException extends RuntimeException {
    public SyllabusAccessDeniedException(String message) {
        super(message);
    }
}
