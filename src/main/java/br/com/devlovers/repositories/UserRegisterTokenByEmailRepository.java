package br.com.devlovers.repositories;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import br.com.devlovers.domain.register.UserRegisterTokenByEmail;
import br.com.devlovers.domain.register.UserRegisterTokenByEmail.UserRegisterTokenByEmailKey;
import reactor.core.publisher.Mono;

public interface UserRegisterTokenByEmailRepository extends ReactiveCassandraRepository<UserRegisterTokenByEmail, UserRegisterTokenByEmailKey> {

    Mono<UserRegisterTokenByEmail> findByKeyEmail(String email);
}