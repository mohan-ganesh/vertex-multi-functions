package com.example.multifunctions.exception;

/**
 *
 */
public class BrokerException extends RuntimeException {

    public BrokerException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     *
     * @param message
     */
    public BrokerException(String message) {
        super(message);
    }

    public BrokerException(Exception e) {
        super(e);
    }
}