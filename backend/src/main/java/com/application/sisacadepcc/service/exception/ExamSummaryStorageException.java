package com.application.sisacadepcc.service.exception;

public class ExamSummaryStorageException extends RuntimeException {
    public ExamSummaryStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExamSummaryStorageException(String message) {
        super(message);
    }
}
