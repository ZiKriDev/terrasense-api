package br.com.devlovers.services.exceptions;

public class SignatureAlreadyExistsException extends RuntimeException {

    public SignatureAlreadyExistsException(String message) {
        super(message);
    }
}
