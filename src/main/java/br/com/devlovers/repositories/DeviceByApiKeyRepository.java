package br.com.devlovers.repositories;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import br.com.devlovers.domain.device.DeviceByApiKey;
import br.com.devlovers.domain.device.DeviceByApiKey.DeviceByApiKeyKey;
import reactor.core.publisher.Mono;

public interface DeviceByApiKeyRepository extends ReactiveCassandraRepository<DeviceByApiKey, DeviceByApiKeyKey>{
    
    Mono<DeviceByApiKey> findByKeyApiKey(String apiKey);
}
