package br.com.devlovers.resources.exceptions;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import br.com.devlovers.services.exceptions.InvalidTimePeriodException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class ArgumentNotValidExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<StandardError>> handleWebExchangeBindException(WebExchangeBindException e,
            ServerWebExchange exchange) {
        List<FieldError> fieldErrors = e.getFieldErrors();
        List<DataErrorValidation> errorValidations = fieldErrors.stream()
                .map(DataErrorValidation::new)
                .collect(Collectors.toList());

        String errorMessage = errorValidations.stream()
                .map(error -> String.format("Field: %s %s", error.field(), error.message()))
                .collect(Collectors.joining(", "));

        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                "Invalid input",
                errorMessage,
                exchange.getRequest().getPath().toString());

        return Mono.just(ResponseEntity.status(status).body(err));
    }

    @ExceptionHandler(InvalidTimePeriodException.class)
    public Mono<ResponseEntity<StandardError>> handleReportGenerationException(InvalidTimePeriodException e,
            ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                "Invalid period",
                e.getMessage(),
                exchange.getRequest().getPath().toString());
        return Mono.just(ResponseEntity.status(status).body(err));
    }

    private record DataErrorValidation(String field, String message) {
        public DataErrorValidation(FieldError error) {
            this(error.getField(), error.getDefaultMessage());
        }
    }
}
