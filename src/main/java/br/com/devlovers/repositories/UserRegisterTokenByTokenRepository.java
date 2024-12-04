package br.com.devlovers.repositories;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import br.com.devlovers.domain.register.UserRegisterTokenByToken;
import br.com.devlovers.domain.register.UserRegisterTokenByToken.UserRegisterTokenByTokenKey;
import reactor.core.publisher.Mono;

public interface UserRegisterTokenByTokenRepository extends ReactiveCassandraRepository<UserRegisterTokenByToken, UserRegisterTokenByTokenKey> {

    Mono<UserRegisterTokenByToken> findByKeyToken(String token);
}