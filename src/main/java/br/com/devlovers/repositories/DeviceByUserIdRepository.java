package br.com.devlovers.repositories;

import java.util.UUID;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import br.com.devlovers.domain.device.DeviceByUserId;
import br.com.devlovers.domain.device.DeviceByUserId.DeviceByUserIdKey;
import reactor.core.publisher.Flux;

public interface DeviceByUserIdRepository extends ReactiveCassandraRepository<DeviceByUserId, DeviceByUserIdKey>{

    Flux<DeviceByUserId> findByKeyOwnerId(UUID ownerId);
}
