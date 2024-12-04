package br.com.devlovers.infra.security.exceptions;

public class TokenHasExpiredException extends RuntimeException {

    public TokenHasExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
