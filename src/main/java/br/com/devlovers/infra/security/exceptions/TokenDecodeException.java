package br.com.devlovers.infra.security.exceptions;

public class TokenDecodeException extends RuntimeException {
    public TokenDecodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
