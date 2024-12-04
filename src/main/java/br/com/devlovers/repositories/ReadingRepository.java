package br.com.devlovers.repositories;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import br.com.devlovers.domain.reading.Reading;
import br.com.devlovers.domain.reading.enums.ReadingType;
import reactor.core.publisher.Flux;

public interface ReadingRepository extends ReactiveCassandraRepository<Reading, Reading.DeviceReadingKey> {
    
    Flux<Reading> findAllByKey_DeviceIdAndKey_ReadingType(UUID sensorId, ReadingType readingType);

    @Query("SELECT * FROM tb_sensor_readings WHERE deviceid = :deviceId AND date = :date AND readingtype = :readingType AND timestamp >= :start AND timestamp <= :end")
    Flux<Reading> findByAllCriteria(UUID deviceId, LocalDate date, ReadingType readingType, Instant start, Instant end);
}