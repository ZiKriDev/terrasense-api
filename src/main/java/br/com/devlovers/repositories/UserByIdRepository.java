package br.com.devlovers.repositories;

import java.util.UUID;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import br.com.devlovers.domain.user.UserById;

public interface UserByIdRepository extends ReactiveCassandraRepository<UserById, UUID> {
    
}
