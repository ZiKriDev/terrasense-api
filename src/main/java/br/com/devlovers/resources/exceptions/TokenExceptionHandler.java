package br.com.devlovers.resources.exceptions;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;

import br.com.devlovers.infra.security.exceptions.TokenDecodeException;
import br.com.devlovers.infra.security.exceptions.TokenGenerationException;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class TokenExceptionHandler {

    @ExceptionHandler(TokenGenerationException.class)
    public Mono<ResponseEntity<StandardError>> handleTokenGenerationException(TokenGenerationException e,
            ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                "Token generation error",
                e.getMessage(),
                exchange.getRequest().getPath().toString());
        return Mono.just(ResponseEntity.status(status).body(err));
    }

    @ExceptionHandler(TokenDecodeException.class)
    public Mono<ResponseEntity<StandardError>> handleTokenDecodeException(TokenDecodeException e,
            ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                "Token decoding error",
                e.getMessage(),
                exchange.getRequest().getPath().toString());
        return Mono.just(ResponseEntity.status(status).body(err));
    }
}
