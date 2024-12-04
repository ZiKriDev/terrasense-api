package br.com.devlovers.repositories;

import java.util.UUID;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import br.com.devlovers.domain.register.UserRegisterToken;

public interface UserRegisterTokenRepository extends ReactiveCassandraRepository<UserRegisterToken, UUID> {

}
