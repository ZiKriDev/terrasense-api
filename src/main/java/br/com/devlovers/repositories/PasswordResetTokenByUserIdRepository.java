package br.com.devlovers.repositories;

import java.util.UUID;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import br.com.devlovers.domain.password.PasswordResetTokenByUserId;
import br.com.devlovers.domain.password.PasswordResetTokenByUserId.PasswordResetTokenByUserIdKey;
import reactor.core.publisher.Mono;

public interface PasswordResetTokenByUserIdRepository extends ReactiveCassandraRepository<PasswordResetTokenByUserId, PasswordResetTokenByUserIdKey> {

    Mono<PasswordResetTokenByUserId> findByKeyUserId(UUID userId);
}
