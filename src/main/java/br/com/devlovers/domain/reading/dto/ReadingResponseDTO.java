package br.com.devlovers.domain.reading.dto;

import java.time.Instant;

import br.com.devlovers.domain.reading.Reading;

public record ReadingResponseDTO(

    Instant timestamp,
    Double value
    
) {

    public ReadingResponseDTO(Reading reading) {
        this(reading.getKey().getTimestamp(), reading.getValue());
    }

}
