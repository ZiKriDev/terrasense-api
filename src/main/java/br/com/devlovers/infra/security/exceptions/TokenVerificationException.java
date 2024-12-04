package br.com.devlovers.infra.security.exceptions;

public class TokenVerificationException extends RuntimeException {
    public TokenVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
