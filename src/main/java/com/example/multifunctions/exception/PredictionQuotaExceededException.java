package com.example.multifunctions.exception;

public class PredictionQuotaExceededException extends RuntimeException {
    public PredictionQuotaExceededException(String message) {
        super(message);
    }

}
