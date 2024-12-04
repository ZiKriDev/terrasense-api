package br.com.devlovers.repositories;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import br.com.devlovers.domain.password.PasswordResetTokenByToken;
import br.com.devlovers.domain.password.PasswordResetTokenByToken.PasswordResetTokenByTokenKey;
import reactor.core.publisher.Mono;

public interface PasswordResetTokenByTokenRepository extends ReactiveCassandraRepository<PasswordResetTokenByToken, PasswordResetTokenByTokenKey> {

    Mono<PasswordResetTokenByToken> findByKeyToken(String token);
}
