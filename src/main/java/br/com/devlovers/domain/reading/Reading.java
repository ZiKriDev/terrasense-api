package br.com.devlovers.domain.reading;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import br.com.devlovers.domain.reading.enums.ReadingType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table("tb_sensor_readings")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "key")
public class Reading implements Serializable {

    private static final long serialVersionUID = 1L;

    @PrimaryKeyClass
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class DeviceReadingKey {
        
        @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
        private UUID deviceId;

        @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
        private LocalDate date;

        @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED)
        private ReadingType readingType;

        @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
        private Instant timestamp;
    }

    @PrimaryKey
    private DeviceReadingKey key;

    private Double value;

    public Instant getTimestamp() {
        return getKey().getTimestamp();
    }
}
