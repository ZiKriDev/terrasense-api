package br.com.devlovers.repositories;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import br.com.devlovers.domain.user.User;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCassandraRepository<User, User.UserKey> {
    Mono<User> findByKey_Login(String login);
}
