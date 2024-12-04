package br.com.devlovers.repositories;

import java.util.UUID;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import br.com.devlovers.domain.signature.Signature;
import br.com.devlovers.domain.signature.Signature.SignatureKey;
import reactor.core.publisher.Flux;

public interface SignatureRepository extends ReactiveCassandraRepository<Signature, SignatureKey> {
    
    Flux<Signature> findByKeyUserId(UUID userId);
}
