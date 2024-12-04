package br.com.devlovers.infra.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import br.com.devlovers.domain.user.User;
import br.com.devlovers.infra.security.exceptions.TokenDecodeException;
import br.com.devlovers.infra.security.exceptions.TokenGenerationException;
import br.com.devlovers.infra.security.exceptions.TokenHasExpiredException;
import br.com.devlovers.infra.security.exceptions.TokenVerificationException;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    private static Set<String> blacklist = new HashSet<>();

    public void addTokenBlacklist(String token) {
        blacklist.add(token);
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklist.contains(token);
    }
    
    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(user.getUsername())
                    .withClaim("roles", user.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList()))
                    .withExpiresAt(generateExpirationDate())
                    .sign(algorithm);

            return token;
        } catch (JWTCreationException e) {
            throw new TokenGenerationException("Error while generating token", e.getCause());
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                      .withIssuer("auth-api")
                      .build()
                      .verify(token)
                      .getSubject();    
        } catch (TokenExpiredException e) {
            throw new TokenHasExpiredException("Expired token", e.getCause());
        } catch (JWTVerificationException e) {
            throw new TokenVerificationException("Invalid token", e.getCause());
        }
    }

    public Instant getTokenExpiration(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getExpiresAt().toInstant();
        } catch (JWTDecodeException e) {
            throw new TokenDecodeException("Error decoding token", e.getCause());
        }
    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusHours(3).toInstant(ZoneOffset.of("-03:00"));
    }
}
