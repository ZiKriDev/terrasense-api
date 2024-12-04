package br.com.devlovers.services.exceptions;

public class InvalidTimePeriodException extends RuntimeException {

    public InvalidTimePeriodException(String message) {
        super(message);
    }
}
