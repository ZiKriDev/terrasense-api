package br.com.devlovers.repositories;

import java.util.UUID;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import br.com.devlovers.domain.password.PasswordResetToken;

public interface PasswordResetTokenRepository extends ReactiveCassandraRepository<PasswordResetToken, UUID> {

}
