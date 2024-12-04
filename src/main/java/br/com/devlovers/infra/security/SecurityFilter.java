package br.com.devlovers.infra.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.devlovers.infra.security.exceptions.TokenHasExpiredException;
import br.com.devlovers.infra.security.exceptions.TokenVerificationException;
import br.com.devlovers.repositories.DeviceByApiKeyRepository;
import br.com.devlovers.repositories.UserRepository;
import br.com.devlovers.resources.exceptions.StandardError;
import reactor.core.publisher.Mono;

@Component
public class SecurityFilter implements WebFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceByApiKeyRepository deviceByApiKeyRepository;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String apiKey = headers.getFirst(API_KEY_HEADER);
        String token = recoverToken(exchange.getRequest());

        if (apiKey != null) {
            return authenticateWithApiKey(apiKey, exchange, chain);
        } else if (token != null) {
            return authenticateWithToken(token, exchange, chain);
        }

        return chain.filter(exchange);
    }

    private Mono<Void> authenticateWithApiKey(String apiKey, ServerWebExchange exchange, WebFilterChain chain) {
        return deviceByApiKeyRepository.findByKeyApiKey(apiKey)
                .flatMap(device -> chain.filter(exchange))
                .switchIfEmpty(handleException(exchange, HttpStatus.UNAUTHORIZED, "Access prohibited",
                        "Chave de API inválida"));
    }

    private Mono<Void> authenticateWithToken(String token, ServerWebExchange exchange, WebFilterChain chain) {
        try {
            String username = tokenService.validateToken(token);
            if (!username.isEmpty()) {
                return userRepository.findByKey_Login(username)
                        .flatMap(user -> {
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                    user, null, user.getAuthorities());
                            SecurityContextImpl securityContext = new SecurityContextImpl(auth);

                            return chain.filter(exchange)
                                    .contextWrite(ReactiveSecurityContextHolder
                                            .withSecurityContext(Mono.just(securityContext)));
                        })
                        .switchIfEmpty(handleException(exchange, HttpStatus.UNAUTHORIZED, "Access prohibited",
                                "Usuário não encontrado"));
            }
        } catch (TokenVerificationException e) {
            return handleException(exchange, HttpStatus.FORBIDDEN, "Token verification error", "Token inválido");
        } catch (TokenHasExpiredException e) {
            return handleException(exchange, HttpStatus.UNAUTHORIZED, "Token expired error",
                    "Faça login novamente para prosseguir");
        }

        return handleException(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized", "Token não fornecido ou inválido");
    }

    private Mono<Void> handleException(ServerWebExchange exchange, HttpStatus status, String error, String message) {
        StandardError standardError = new StandardError(
                Instant.now(),
                status.value(),
                error,
                message,
                exchange.getRequest().getPath().toString());

        byte[] bytes;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            bytes = objectMapper.writeValueAsBytes(standardError);
        } catch (JsonProcessingException e) {
            bytes = ("{\"message\": \"Error serializing response\"}").getBytes(StandardCharsets.UTF_8);
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(dataBuffer));
    }

    private String recoverToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
    }
}
