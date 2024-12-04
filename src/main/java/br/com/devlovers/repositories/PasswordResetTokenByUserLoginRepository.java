package br.com.devlovers.repositories;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import br.com.devlovers.domain.password.PasswordResetTokenByUserLogin;
import br.com.devlovers.domain.password.PasswordResetTokenByUserLogin.PasswordResetTokenByUserLoginKey;
import reactor.core.publisher.Mono;

public interface PasswordResetTokenByUserLoginRepository extends ReactiveCassandraRepository<PasswordResetTokenByUserLogin, PasswordResetTokenByUserLoginKey> {

    Mono<PasswordResetTokenByUserLogin> findByKeyLogin(String login);
}
