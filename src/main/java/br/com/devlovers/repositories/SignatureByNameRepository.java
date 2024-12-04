package br.com.devlovers.repositories;

import java.util.UUID;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import br.com.devlovers.domain.signature.SignatureByName;
import br.com.devlovers.domain.signature.SignatureByName.SignatureByNameKey;
import reactor.core.publisher.Mono;

public interface SignatureByNameRepository extends ReactiveCassandraRepository<SignatureByName, SignatureByNameKey> {
    
    Mono<SignatureByName> findByKeyNameAndKeyUserId(String name, UUID userId);
}
