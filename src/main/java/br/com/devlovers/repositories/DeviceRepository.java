package br.com.devlovers.repositories;

import java.util.UUID;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import br.com.devlovers.domain.device.Device;
import reactor.core.publisher.Mono;

public interface DeviceRepository extends ReactiveCassandraRepository<Device, Device.DeviceKey> {
    
    Mono<Device> findByKey_Name(String name);
    Mono<Device> findByKey_Id(UUID id);
}
