package br.com.devlovers.resources.exceptions;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;

import br.com.devlovers.services.exceptions.DeviceAlreadyExistsException;
import br.com.devlovers.services.exceptions.EmailSendingException;
import br.com.devlovers.services.exceptions.FileException;
import br.com.devlovers.services.exceptions.ReportGenerationException;
import br.com.devlovers.services.exceptions.ResourceNotFoundException;
import br.com.devlovers.services.exceptions.SignatureAlreadyExistsException;
import br.com.devlovers.services.exceptions.UserAlreadyExistsException;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class ResourceExceptionHandler {

        @ExceptionHandler(DeviceAlreadyExistsException.class)
        public Mono<ResponseEntity<StandardError>> handleSensorAlreadyExists(DeviceAlreadyExistsException e,
                        ServerWebExchange exchange) {
                HttpStatus status = HttpStatus.CONFLICT;
                StandardError err = new StandardError(
                                Instant.now(),
                                status.value(),
                                "Device already exists",
                                e.getMessage(),
                                exchange.getRequest().getPath().toString());
                return Mono.just(ResponseEntity.status(status).body(err));
        }

        @ExceptionHandler(UserAlreadyExistsException.class)
        public Mono<ResponseEntity<StandardError>> handleUserAlreadyExists(UserAlreadyExistsException e,
                        ServerWebExchange exchange) {
                HttpStatus status = HttpStatus.CONFLICT;
                StandardError err = new StandardError(
                                Instant.now(),
                                status.value(),
                                "User already exists",
                                e.getMessage(),
                                exchange.getRequest().getPath().toString());
                return Mono.just(ResponseEntity.status(status).body(err));
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public Mono<ResponseEntity<StandardError>> handleResourceNotFoundException(ResourceNotFoundException e,
                        ServerWebExchange exchange) {
                HttpStatus status = HttpStatus.NOT_FOUND;
                StandardError err = new StandardError(
                                Instant.now(),
                                status.value(),
                                "Resource not found",
                                e.getMessage(),
                                exchange.getRequest().getPath().toString());
                return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(err));
        }

        @ExceptionHandler(UsernameNotFoundException.class)
        public Mono<ResponseEntity<StandardError>> handleUsernameNotFoundException(UsernameNotFoundException e,
                        ServerWebExchange exchange) {
                HttpStatus status = HttpStatus.NOT_FOUND;
                StandardError err = new StandardError(
                                Instant.now(),
                                status.value(),
                                "Invalid credentials",
                                e.getMessage(),
                                exchange.getRequest().getPath().toString());
                return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(err));
        }

        @ExceptionHandler(BadCredentialsException.class)
        public Mono<ResponseEntity<StandardError>> handleBadCredentialsException(BadCredentialsException e,
                        ServerWebExchange exchange) {
                HttpStatus status = HttpStatus.NOT_FOUND;
                StandardError err = new StandardError(
                                Instant.now(),
                                status.value(),
                                "Invalid credentials",
                                "O usuário ou senha fornecidos estão incorretos",
                                exchange.getRequest().getPath().toString());
                return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(err));
        }

        @ExceptionHandler(EmailSendingException.class)
        public Mono<ResponseEntity<StandardError>> handleEmailSendingException(EmailSendingException e,
                        ServerWebExchange exchange) {
                HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                StandardError err = new StandardError(
                                Instant.now(),
                                status.value(),
                                "Email sending failed",
                                e.getMessage(),
                                exchange.getRequest().getPath().toString());
                return Mono.just(ResponseEntity.status(status).body(err));
        }

        @ExceptionHandler(ReportGenerationException.class)
        public Mono<ResponseEntity<StandardError>> handleReportGenerationException(ReportGenerationException e,
                        ServerWebExchange exchange) {
                HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                StandardError err = new StandardError(
                                Instant.now(),
                                status.value(),
                                "Report generation failed",
                                e.getMessage(),
                                exchange.getRequest().getPath().toString());
                return Mono.just(ResponseEntity.status(status).body(err));
        }

        @ExceptionHandler(FileException.class)
        public Mono<ResponseEntity<StandardError>> handleFileException(FileException e,
                        ServerWebExchange exchange) {
                HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                StandardError err = new StandardError(
                                Instant.now(),
                                status.value(),
                                "File error",
                                e.getMessage(),
                                exchange.getRequest().getPath().toString());
                return Mono.just(ResponseEntity.status(status).body(err));
        }

        @ExceptionHandler(SignatureAlreadyExistsException.class)
        public Mono<ResponseEntity<StandardError>> handleUserAlreadyExists(SignatureAlreadyExistsException e,
                        ServerWebExchange exchange) {
                HttpStatus status = HttpStatus.CONFLICT;
                StandardError err = new StandardError(
                                Instant.now(),
                                status.value(),
                                "Signature already exists",
                                e.getMessage(),
                                exchange.getRequest().getPath().toString());
                return Mono.just(ResponseEntity.status(status).body(err));
        }

        @ExceptionHandler(Exception.class)
        public Mono<ResponseEntity<StandardError>> handleGenericException(Exception e, ServerWebExchange exchange) {
                HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                StandardError err = new StandardError(
                                Instant.now(),
                                status.value(),
                                "Internal server error",
                                e.getMessage(),
                                exchange.getRequest().getPath().toString());
                return Mono.just(ResponseEntity.status(status).body(err));
        }
}
