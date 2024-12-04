package br.com.devlovers.repositories;

import java.util.UUID;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import br.com.devlovers.domain.device.DeviceById;
import reactor.core.publisher.Mono;

public interface DeviceByIdRepository extends ReactiveCassandraRepository<DeviceById, UUID> {

    Mono<DeviceById> findByApiKey(String apiKey);
}
